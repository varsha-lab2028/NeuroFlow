package com.neuroflow.api;

import com.neuroflow.dao.PracticeSessionDao;
import com.neuroflow.dao.StudentDao;
import com.neuroflow.model.Student;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentDao studentDao;
    private final PracticeSessionDao sessionDao;

    public StudentController(StudentDao studentDao, PracticeSessionDao sessionDao) {
        this.studentDao = studentDao;
        this.sessionDao = sessionDao;
    }

    /** GET /api/students - all students */
    @GetMapping
    public List<Student> getAll() {
        return studentDao.findAll();
    }

    /** GET /api/students/{id} - single student */
    @GetMapping("/{id}")
    public ResponseEntity<Student> getById(@PathVariable int id) {
        Student s = studentDao.findById(id);
        return s != null ? ResponseEntity.ok(s) : ResponseEntity.notFound().build();
    }

    /** GET /api/students/by-educator/{educatorId} */
    @GetMapping("/by-educator/{educatorId}")
    public List<Student> getByEducator(@PathVariable int educatorId) {
        return studentDao.findByEducatorId(educatorId);
    }

    /** GET /api/students/by-parent/{parentId} */
    @GetMapping("/by-parent/{parentId}")
    public List<Student> getByParent(@PathVariable int parentId) {
        return studentDao.findByParentId(parentId);
    }

    /** GET /api/students/{id}/home-data - everything the home screen needs */
    @GetMapping("/{id}/home-data")
    public ResponseEntity<Map<String, Object>> getHomeData(@PathVariable int id) {
        Student s = studentDao.findById(id);
        if (s == null) return ResponseEntity.notFound().build();

        Map<String, Object> data = new HashMap<>();
        data.put("student", s);
        data.put("todayDuration", sessionDao.sumDurationToday(id));
        data.put("todayAttempts", sessionDao.countTodayAttempts(id));
        data.put("recentSessions", sessionDao.findRecentByStudentId(id, 5));
        data.put("weeklyPracticeDays", sessionDao.weeklyPracticeDays(id));
        return ResponseEntity.ok(data);
    }

    /** PATCH /api/students/{id}/letter - update current letter */
    @PatchMapping("/{id}/letter")
    public ResponseEntity<String> updateLetter(@PathVariable int id,
                                                @RequestBody Map<String, String> body) {
        Student s = studentDao.findById(id);
        if (s == null) return ResponseEntity.notFound().build();
        s.setCurrentLetter(body.get("letter"));
        studentDao.update(s);
        return ResponseEntity.ok("updated");
    }

    /** PATCH /api/students/{id}/streak - increment streak */
    @PatchMapping("/{id}/streak")
    public ResponseEntity<Map<String, Integer>> incrementStreak(@PathVariable int id) {
        Student s = studentDao.findById(id);
        if (s == null) return ResponseEntity.notFound().build();
        s.setStreakDays(s.getStreakDays() + 1);
        studentDao.update(s);
        return ResponseEntity.ok(Map.of("streakDays", s.getStreakDays()));
    }

    /** GET /api/students/stats - overview counts for educator dashboard */
    @GetMapping("/stats/overview")
    public Map<String, Integer> getOverviewStats() {
        return Map.of(
            "activeStudents", studentDao.countActive(),
            "practicedToday", studentDao.countPracticedToday()
        );
    }
}
