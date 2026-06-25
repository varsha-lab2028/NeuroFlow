package com.neuroflow.api;

import com.neuroflow.dao.ErrorPatternDao;
import com.neuroflow.dao.PracticeActivityDao;
import com.neuroflow.dao.PracticeSessionDao;
import com.neuroflow.dao.StudentDao;
import com.neuroflow.model.PracticeActivity;
import com.neuroflow.model.Student;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final ErrorPatternDao errorDao;
    private final PracticeActivityDao activityDao;
    private final StudentDao studentDao;
    private final PracticeSessionDao sessionDao;

    public AnalyticsController(ErrorPatternDao errorDao, PracticeActivityDao activityDao,
                                StudentDao studentDao, PracticeSessionDao sessionDao) {
        this.errorDao    = errorDao;
        this.activityDao = activityDao;
        this.studentDao  = studentDao;
        this.sessionDao  = sessionDao;
    }

    /** GET /api/analytics/errors/weekly  - this week's error totals */
    @GetMapping("/errors/weekly")
    public Map<String, Integer> getWeeklyErrors() {
        return errorDao.weeklyTotals(getMonday());
    }

    /** GET /api/analytics/errors/all-time */
    @GetMapping("/errors/all-time")
    public Map<String, Integer> getAllTimeErrors() {
        return errorDao.allTimeTotals();
    }

    /** GET /api/analytics/activities?educatorId=7 */
    @GetMapping("/activities")
    public List<PracticeActivity> getActivities(@RequestParam(defaultValue = "7") int educatorId) {
        return activityDao.findByEducatorId(educatorId);
    }

    /** GET /api/analytics/activities/week  - this week's activities */
    @GetMapping("/activities/week")
    public List<PracticeActivity> getWeekActivities() {
        return activityDao.findByWeekDate(getMonday());
    }

    /** GET /api/analytics/activities/shared - activities shared with parents */
    @GetMapping("/activities/shared")
    public List<PracticeActivity> getSharedActivities() {
        return activityDao.findSharedWithParents();
    }

    /** POST /api/analytics/activities - create new activity */
    @PostMapping("/activities")
    public ResponseEntity<Map<String, Object>> createActivity(@RequestBody PracticeActivity a) {
        if (a.getWeekDate() == null || a.getWeekDate().isBlank()) a.setWeekDate(getMonday());
        if (a.getActivityType() == null || a.getActivityType().isBlank()) a.setActivityType("classroom_focus");
        int id = activityDao.insert(a);
        return ResponseEntity.ok(Map.of("id", id, "ok", id > 0));
    }

    /** PATCH /api/analytics/activities/{id}/complete */
    @PatchMapping("/activities/{id}/complete")
    public ResponseEntity<String> markComplete(@PathVariable int id,
                                                @RequestBody Map<String, Boolean> body) {
        activityDao.markCompleted(id, body.getOrDefault("completed", true));
        return ResponseEntity.ok("updated");
    }

    /** PATCH /api/analytics/activities/{id}/share */
    @PatchMapping("/activities/{id}/share")
    public ResponseEntity<String> shareActivity(@PathVariable int id,
                                                  @RequestBody Map<String, Boolean> body) {
        activityDao.shareWithParents(id, body.getOrDefault("shared", true));
        return ResponseEntity.ok("updated");
    }

    /** DELETE /api/analytics/activities/{id} */
    @DeleteMapping("/activities/{id}")
    public ResponseEntity<String> deleteActivity(@PathVariable int id) {
        activityDao.delete(id);
        return ResponseEntity.ok("deleted");
    }

    /** GET /api/analytics/export/csv - download CSV report */
    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsv() {
        List<Student> students = studentDao.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("Student,Progress%,Streak,Primary Issue,Trend,Sessions Today,Duration(s)\n");
        for (Student s : students) {
            int dur  = sessionDao.sumDurationToday(s.getStudentId());
            int sess = sessionDao.findTodayByStudentId(s.getStudentId()).size();
            sb.append(String.format("%s,%d,%d,%s,%s,%d,%d\n",
                s.getName(), s.getWeeklyProgress(), s.getStreakDays(),
                s.getPrimaryIssue(), s.getTrend(), sess, dur));
        }
        byte[] csv = sb.toString().getBytes();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=NeuroFlow_Report_" + LocalDate.now() + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    /** GET /api/analytics/parent-summary/{studentId} */
    @GetMapping("/parent-summary/{studentId}")
    public ResponseEntity<Map<String, Object>> getParentSummary(@PathVariable int studentId) {
        Student s = studentDao.findById(studentId);
        if (s == null) return ResponseEntity.notFound().build();

        int durationSec = sessionDao.sumDurationToday(studentId);
        int attempts    = sessionDao.countTodayAttempts(studentId);
        var todaySessions = sessionDao.findTodayByStudentId(studentId);
        Set<String> letters = new LinkedHashSet<>();
        todaySessions.forEach(ps -> letters.add(ps.getTargetLetter()));

        Map<String, Object> resp = new HashMap<>();
        resp.put("student", s);
        resp.put("todayDurationMinutes", durationSec / 60);
        resp.put("todayAttempts", attempts);
        resp.put("practicedLetters", letters);
        resp.put("weeklyErrors", errorDao.weeklyTotals(getMonday()));
        resp.put("sharedActivities", activityDao.findSharedWithParents());
        return ResponseEntity.ok(resp);
    }

    // ─────────────────────────────────────────────────────────────────────────
    private String getMonday() {
        LocalDate d = LocalDate.now();
        while (d.getDayOfWeek().getValue() != 1) d = d.minusDays(1);
        return d.toString();
    }
}
