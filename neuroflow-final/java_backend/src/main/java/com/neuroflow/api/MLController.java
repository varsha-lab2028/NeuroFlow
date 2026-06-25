package com.neuroflow.api;

import com.neuroflow.dao.*;
import com.neuroflow.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MLController — all endpoints used by the Python ML script and React frontend
 * for the digit and letter ML integration.
 *
 * Python POSTs to:  /api/practice-result  /api/digit-result  /api/none-event
 * React GETs from:  /api/letter-latest    /api/digit-latest  /api/digit-session
 *                   /api/none-trend       /api/levels        /api/current-target
 */
@RestController
@RequestMapping("/api")
public class MLController {

    private final PracticeSessionDao sessionDao;
    private final ErrorPatternDao    errorDao;
    private final DigitAttemptDao    digitDao;
    private final NoneEventDao       noneDao;
    private final LevelProgressDao   levelDao;

    public MLController(PracticeSessionDao sessionDao,
                        ErrorPatternDao errorDao,
                        DigitAttemptDao digitDao,
                        NoneEventDao noneDao,
                        LevelProgressDao levelDao) {
        this.sessionDao = sessionDao;
        this.errorDao   = errorDao;
        this.digitDao   = digitDao;
        this.noneDao    = noneDao;
        this.levelDao   = levelDao;
    }

    // ── Python POST endpoints ─────────────────────────────────────────────────

    /**
     * POST /api/practice-result
     * Python script posts letter classification result after Flask inference.
     * Saves a PracticeSession and updates ErrorPattern aggregates.
     */
    @PostMapping("/practice-result")
    public ResponseEntity<Map<String, Object>> practiceResult(@RequestBody Map<String, Object> body) {
        PracticeSession s = new PracticeSession();
        s.setStudentId(intVal(body, "studentId", 1));
        s.setTargetLetter(strVal(body, "targetLetter", "b"));
        s.setDetectedLetter(strVal(body, "detectedLetter", null));
        s.setCorrect(boolVal(body, "isCorrect", false));
        s.setConfidence(dblVal(body, "confidence", 0.0));
        s.setAttempts(1);
        s.setDurationSeconds(intVal(body, "durationSeconds", 0));

        int id = sessionDao.insert(s);

        if (!s.isCorrect() && s.getDetectedLetter() != null) {
            String errType = resolveLetterError(s.getTargetLetter(), s.getDetectedLetter());
            errorDao.upsert(new ErrorPattern(0, s.getStudentId(), errType,
                    LocalDate.now().toString(), 1));
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("status", "ok");
        resp.put("sessionId", id);
        return ResponseEntity.ok(resp);
    }

    /**
     * POST /api/digit-result
     * Python posts digit recognition result (Model 2). Saves DigitAttempt and
     * checks if all 9 digits are complete — if so, unlocks Level 2 (Letters).
     */
    @PostMapping("/digit-result")
    public ResponseEntity<Map<String, Object>> digitResult(@RequestBody Map<String, Object> body) {
        int studentId        = intVal(body, "studentId", 1);
        String targetDigit   = strVal(body, "targetDigit", "1");
        String recognized    = strVal(body, "recognizedDigit", "NONE");
        double confidence    = dblVal(body, "confidence", 0.0);
        boolean correct      = targetDigit.equals(recognized);

        DigitAttempt attempt = new DigitAttempt(studentId, targetDigit, recognized, correct, confidence);
        int id = digitDao.insert(attempt);

        // Level unlock: all 9 digits done today → level 1 complete, level 2 unlocked
        if (correct && digitDao.allDigitsCompletedToday(studentId)) {
            levelDao.upsert(studentId, 1, true, true);
            levelDao.upsert(studentId, 2, false, true);
            System.out.println("[Level] Letters level unlocked for student " + studentId);
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("correct", correct);
        resp.put("confidence", confidence);
        resp.put("id", id);
        return ResponseEntity.ok(resp);
    }

    /**
     * POST /api/none-event
     * Python posts when digit model outputs NONE (or confidence < 70%).
     * Increments today's none_events count for trend tracking.
     */
    @PostMapping("/none-event")
    public ResponseEntity<Map<String, Object>> noneEvent(@RequestBody Map<String, Object> body) {
        int studentId = intVal(body, "studentId", 1);
        noneDao.incrementToday(studentId, LocalDate.now().toString());
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    // ── React GET endpoints ───────────────────────────────────────────────────

    /**
     * GET /api/letter-latest/{studentId}
     * React polls this every 1.5s on the letter TryScreen to get the latest result.
     */
    @GetMapping("/letter-latest/{studentId}")
    public ResponseEntity<Map<String, Object>> letterLatest(@PathVariable int studentId) {
        PracticeSession s = sessionDao.findLatest(studentId);
        if (s == null) {
            return ResponseEntity.ok(Map.of("empty", true));
        }
        Map<String, Object> resp = new HashMap<>();
        resp.put("empty", false);
        resp.put("id", s.getSessionId());
        resp.put("targetLetter", s.getTargetLetter());
        resp.put("detectedLetter", s.getDetectedLetter());
        resp.put("correct", s.isCorrect());
        resp.put("confidence", s.getConfidence());
        resp.put("feedbackMessage", buildLetterFeedback(s.getTargetLetter(), s.getDetectedLetter(), s.isCorrect()));
        return ResponseEntity.ok(resp);
    }

    /**
     * GET /api/digit-latest/{studentId}
     * React polls this on the digit practice screen.
     */
    @GetMapping("/digit-latest/{studentId}")
    public ResponseEntity<Map<String, Object>> digitLatest(@PathVariable int studentId) {
        DigitAttempt a = digitDao.findLatest(studentId);
        if (a == null) {
            return ResponseEntity.ok(Map.of("empty", true));
        }
        Map<String, Object> resp = new HashMap<>();
        resp.put("empty", false);
        resp.put("id", a.getId());
        resp.put("targetDigit", a.getTargetDigit());
        resp.put("recognizedDigit", a.getRecognizedDigit());
        resp.put("correct", a.isCorrect());
        resp.put("confidence", a.getConfidence());
        return ResponseEntity.ok(resp);
    }

    /**
     * GET /api/digit-session/{studentId}
     * Returns list of distinct digits correctly written today.
     */
    @GetMapping("/digit-session/{studentId}")
    public ResponseEntity<List<String>> digitSession(@PathVariable int studentId) {
        return ResponseEntity.ok(digitDao.correctDigitsToday(studentId));
    }

    /**
     * GET /api/none-trend/{studentId}
     * Returns last 14 days of none_events with trend direction.
     * Used by NoneTrendChart on Parent and Educator dashboards.
     */
    @GetMapping("/none-trend/{studentId}")
    public ResponseEntity<Map<String, Object>> noneTrend(@PathVariable int studentId) {
        List<NoneEvent> events = noneDao.findLastNDays(studentId, 14);

        List<Map<String, Object>> data = new ArrayList<>();
        for (NoneEvent e : events) {
            data.add(Map.of("date", e.getEventDate(), "count", e.getCount()));
        }

        String direction = computeTrend(events);

        Map<String, Object> resp = new HashMap<>();
        resp.put("direction", direction);
        resp.put("data", data);
        return ResponseEntity.ok(resp);
    }

    /**
     * GET /api/levels/{studentId}
     * Returns level unlock/completion status. Level 1 (Numbers) is always unlocked.
     */
    @GetMapping("/levels/{studentId}")
    public ResponseEntity<List<Map<String, Object>>> levels(@PathVariable int studentId) {
        List<LevelProgress> saved = levelDao.findByStudent(studentId);
        Map<Integer, LevelProgress> byLevel = new HashMap<>();
        for (LevelProgress lp : saved) byLevel.put(lp.getLevelId(), lp);

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            LevelProgress lp = byLevel.get(i);
            boolean unlocked  = (i == 1) || (lp != null && lp.isUnlocked());
            boolean completed = lp != null && lp.isCompleted();
            result.add(Map.of("levelId", i, "completed", completed, "unlocked", unlocked));
        }
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/current-target/{studentId}
     * Returns the current target digit or letter for this student.
     * For now: returns the next digit not yet correctly written today.
     */
    @GetMapping("/current-target/{studentId}")
    public ResponseEntity<Map<String, Object>> currentTarget(@PathVariable int studentId) {
        List<String> done = digitDao.correctDigitsToday(studentId);
        String target = "1";
        for (int i = 1; i <= 9; i++) {
            if (!done.contains(String.valueOf(i))) {
                target = String.valueOf(i);
                break;
            }
        }
        return ResponseEntity.ok(Map.of("target", target, "type", "digit"));
    }

    // ── Active-target store (in-memory, set by React when child starts a digit) ─
    //
    // Problem this solves:
    //   /api/current-target returns the NEXT incomplete digit from the DB,
    //   NOT what is on screen. If digits 1-3 are done, it returns "4" even when
    //   the child is on /numtry/1.  Python was using that → always "correct".
    //
    // Solution:
    //   React calls POST /api/set-target when the child presses Start.
    //   Python calls GET  /api/active-target to read exactly what is on screen.

    private static final Map<Integer, String> activeTargets = new ConcurrentHashMap<>();

    /**
     * POST /api/set-target
     * React calls this when the child presses "Start" on the Try screen.
     * Body: { "studentId": 1, "target": "3" }
     */
    @PostMapping("/set-target")
    public ResponseEntity<Map<String, Object>> setTarget(@RequestBody Map<String, Object> body) {
        int    studentId = intVal(body, "studentId", 1);
        String target    = strVal(body, "target", "1");
        activeTargets.put(studentId, target);
        System.out.println("[Target] Student " + studentId + " is now writing: " + target);
        return ResponseEntity.ok(Map.of("status", "ok", "target", target));
    }

    /**
     * GET /api/active-target/{studentId}
     * Python calls this before posting a digit-result so it knows what digit
     * the child was actually asked to write (not what the DB thinks is next).
     * Falls back to the DB-computed next digit if React never called set-target.
     */
    @GetMapping("/active-target/{studentId}")
    public ResponseEntity<Map<String, Object>> activeTarget(@PathVariable int studentId) {
        String target = activeTargets.get(studentId);
        if (target == null) {
            // Fallback: same logic as current-target
            List<String> done = digitDao.correctDigitsToday(studentId);
            target = "1";
            for (int i = 1; i <= 9; i++) {
                if (!done.contains(String.valueOf(i))) { target = String.valueOf(i); break; }
            }
        }
        return ResponseEntity.ok(Map.of("target", target, "type", "digit"));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private String computeTrend(List<NoneEvent> events) {
        if (events.size() < 4) return "stable";
        // Compare average of first half vs second half
        int mid = events.size() / 2;
        double firstHalf = events.subList(0, mid).stream()
                .mapToInt(NoneEvent::getCount).average().orElse(0);
        double secondHalf = events.subList(mid, events.size()).stream()
                .mapToInt(NoneEvent::getCount).average().orElse(0);
        if (secondHalf < firstHalf * 0.85) return "improving";
        if (secondHalf > firstHalf * 1.15) return "needs_attention";
        return "stable";
    }

    private String resolveLetterError(String target, String detected) {
        boolean bd = Set.of("b","d").containsAll(Set.of(target, detected));
        boolean pq = Set.of("p","q").containsAll(Set.of(target, detected));
        if (bd) return "b/d reversal";
        if (pq) return "p/q reversal";
        return "stroke direction";
    }

    private String buildLetterFeedback(String target, String detected, boolean correct) {
        if (correct) return "Great job!";
        if (target == null || detected == null) return "Watch the stroke direction carefully";
        Map<String, String> msgs = Map.of(
            "b|d", "The bump should go to the RIGHT",
            "d|b", "The bump should go to the LEFT",
            "p|q", "The tail should go to the RIGHT",
            "q|p", "The tail should go to the LEFT"
        );
        return msgs.getOrDefault(target + "|" + detected, "Watch the stroke direction carefully");
    }

    // ── type coercion helpers ─────────────────────────────────────────────────
    private int intVal(Map<String, Object> m, String k, int def) {
        Object v = m.get(k);
        if (v == null) return def;
        try { return Integer.parseInt(String.valueOf(v)); } catch (Exception e) { return def; }
    }
    private String strVal(Map<String, Object> m, String k, String def) {
        Object v = m.get(k);
        return v == null ? def : String.valueOf(v);
    }
    private boolean boolVal(Map<String, Object> m, String k, boolean def) {
        Object v = m.get(k);
        if (v == null) return def;
        return Boolean.parseBoolean(String.valueOf(v));
    }
    private double dblVal(Map<String, Object> m, String k, double def) {
        Object v = m.get(k);
        if (v == null) return def;
        try { return Double.parseDouble(String.valueOf(v)); } catch (Exception e) { return def; }
    }
}
