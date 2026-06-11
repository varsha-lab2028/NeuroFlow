package com.neuroflow.api;

import com.neuroflow.dao.ErrorPatternDao;
import com.neuroflow.dao.PracticeSessionDao;
import com.neuroflow.dao.StudentDao;
import com.neuroflow.model.ErrorPattern;
import com.neuroflow.model.PracticeSession;
import com.neuroflow.model.Student;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/practice")
public class PracticeController {

    private final PracticeSessionDao sessionDao;
    private final ErrorPatternDao errorDao;
    private final StudentDao studentDao;

    public PracticeController(PracticeSessionDao sessionDao,
                               ErrorPatternDao errorDao,
                               StudentDao studentDao) {
        this.sessionDao = sessionDao;
        this.errorDao = errorDao;
        this.studentDao = studentDao;
    }

    /**
     * POST /api/practice/classify
     * Simulates ML classification of a letter stroke.
     * Body: { "targetLetter": "b", "studentId": 1 }
     * Returns: { "detectedLetter", "isCorrect", "confidence", "buzz", "simulated" }
     */
    @PostMapping("/classify")
    public ResponseEntity<Map<String, Object>> classify(@RequestBody Map<String, Object> body) {
        String target = (String) body.getOrDefault("targetLetter", "b");
        Map<String, Object> result = simulateClassify(target);
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/practice/session
     * Save a completed practice session and update error patterns.
     * Body: { studentId, targetLetter, detectedLetter, isCorrect, confidence, attempts, durationSeconds }
     */
    @PostMapping("/session")
    public ResponseEntity<Map<String, Object>> saveSession(@RequestBody Map<String, Object> body) {
        PracticeSession s = new PracticeSession();
        s.setStudentId(Integer.parseInt(String.valueOf(body.get("studentId"))));
        s.setTargetLetter((String) body.get("targetLetter"));
        s.setDetectedLetter((String) body.get("detectedLetter"));
        s.setCorrect(Boolean.parseBoolean(String.valueOf(body.get("isCorrect"))));
        s.setConfidence(Double.parseDouble(String.valueOf(body.getOrDefault("confidence", 0.0))));
        s.setAttempts(Integer.parseInt(String.valueOf(body.getOrDefault("attempts", 1))));
        s.setDurationSeconds(Integer.parseInt(String.valueOf(body.getOrDefault("durationSeconds", 0))));

        int id = sessionDao.insert(s);

        // Record error pattern if incorrect
        if (!s.isCorrect() && s.getDetectedLetter() != null) {
            String errType = resolveErrorType(s.getTargetLetter(), s.getDetectedLetter());
            ErrorPattern ep = new ErrorPattern(0, s.getStudentId(), errType,
                    LocalDate.now().toString(), 1);
            errorDao.upsert(ep);
        }

        // Update student weekly progress based on sessions this week
        Student stu = studentDao.findById(s.getStudentId());
        if (stu != null) {
            List<PracticeSession> todaySessions = sessionDao.findTodayByStudentId(s.getStudentId());
            long correct = todaySessions.stream().filter(PracticeSession::isCorrect).count();
            long total   = todaySessions.size();
            if (total > 0) {
                stu.setWeeklyProgress((int) Math.round((double) correct / total * 100));
                // Update trend: if progress > 70% set ↑, < 40% set ↓, else →
                if (stu.getWeeklyProgress() >= 70) stu.setTrend("↑");
                else if (stu.getWeeklyProgress() < 40) stu.setTrend("↓");
                else stu.setTrend("→");
                studentDao.update(stu);
            }
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("sessionId", id);
        resp.put("ok", id > 0);
        return ResponseEntity.ok(resp);
    }

    /** GET /api/practice/today/{studentId} */
    @GetMapping("/today/{studentId}")
    public ResponseEntity<Map<String, Object>> getTodayStats(@PathVariable int studentId) {
        var sessions = sessionDao.findTodayByStudentId(studentId);
        int duration  = sessionDao.sumDurationToday(studentId);
        int attempts  = sessionDao.countTodayAttempts(studentId);
        long correct  = sessions.stream().filter(PracticeSession::isCorrect).count();

        // Find practised letters today
        Set<String> letters = new LinkedHashSet<>();
        sessions.forEach(s -> letters.add(s.getTargetLetter()));

        Map<String, Object> resp = new HashMap<>();
        resp.put("sessionCount", sessions.size());
        resp.put("durationSeconds", duration);
        resp.put("attempts", attempts);
        resp.put("correct", correct);
        resp.put("accuracy", sessions.isEmpty() ? 0 : (int) Math.round((double) correct / sessions.size() * 100));
        resp.put("practicedLetters", letters);
        return ResponseEntity.ok(resp);
    }

    /** GET /api/practice/recent/{studentId}?limit=10 */
    @GetMapping("/recent/{studentId}")
    public List<PracticeSession> getRecent(@PathVariable int studentId,
                                            @RequestParam(defaultValue = "10") int limit) {
        return sessionDao.findRecentByStudentId(studentId, limit);
    }

    /** GET /api/practice/weekly-days/{studentId} - bar chart data */
    @GetMapping("/weekly-days/{studentId}")
    public Map<String, Object> getWeeklyDays(@PathVariable int studentId) {
        int[] days = sessionDao.weeklyPracticeDays(studentId);
        // Re-index to Mon–Sun for display (SQLite %w: 0=Sun ... 6=Sat)
        String[] labels = {"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            result.add(Map.of("day", labels[i], "count", days[i]));
        }
        return Map.of("days", result);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Map<String, Object> simulateClassify(String targetLetter) {
        Random rnd = new Random();
        String detected;
        double confidence;
        if (rnd.nextDouble() < 0.72) {
            detected   = targetLetter;
            confidence = 0.88 + rnd.nextDouble() * 0.12;
        } else {
            Map<String, String> confusions = Map.of("b","d","d","b","p","q","q","p");
            detected   = confusions.getOrDefault(targetLetter, "b");
            confidence = 0.78 + rnd.nextDouble() * 0.18;
        }
        boolean correct = detected.equals(targetLetter);
        Map<String, Object> r = new HashMap<>();
        r.put("detectedLetter", detected);
        r.put("isCorrect", correct);
        r.put("confidence", Math.round(confidence * 100.0) / 100.0);
        r.put("buzz", !correct && confidence > 0.60);
        r.put("simulated", true);
        return r;
    }

    private String resolveErrorType(String target, String detected) {
        boolean group1 = (target.equals("b") || target.equals("d"))
                      && (detected.equals("b") || detected.equals("d"));
        boolean group2 = (target.equals("p") || target.equals("q"))
                      && (detected.equals("p") || detected.equals("q"));
        if (group1) return "b/d reversal";
        if (group2) return "p/q reversal";
        return "stroke direction";
    }
}
