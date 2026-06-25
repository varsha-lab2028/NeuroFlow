package com.neuroflow.api;

import com.neuroflow.dao.StudentDao;
import com.neuroflow.dao.UserDao;
import com.neuroflow.model.Student;
import com.neuroflow.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserDao userDao;
    private final StudentDao studentDao;

    public AuthController(UserDao userDao, StudentDao studentDao) {
        this.userDao = userDao;
        this.studentDao = studentDao;
    }

    /**
     * POST /api/auth/login
     * Body: { "role": "child|parent|educator", "pin": "1234", "studentId": 1 }
     * - child login: no PIN needed, just picks studentId
     * - parent/educator: verifies PIN
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, Object> body) {
        String role = (String) body.get("role");
        String pin  = (String) body.get("pin");
        Map<String, Object> resp = new HashMap<>();

        if ("child".equals(role)) {
            // Child picks their name from a list — no PIN
            int studentId = Integer.parseInt(String.valueOf(body.getOrDefault("studentId", "1")));
            Student s = studentDao.findById(studentId);
            if (s == null) {
                resp.put("ok", false);
                resp.put("error", "Student not found");
                return ResponseEntity.badRequest().body(resp);
            }
            User u = userDao.findById(s.getUserId());
            resp.put("ok", true);
            resp.put("user", u);
            resp.put("student", s);
            resp.put("role", "child");
            return ResponseEntity.ok(resp);
        }

        // Parent or educator — authenticate by PIN
        User u = userDao.authenticate(role, pin);
        if (u == null) {
            resp.put("ok", false);
            resp.put("error", "Incorrect PIN");
            return ResponseEntity.status(401).body(resp);
        }
        resp.put("ok", true);
        resp.put("user", u);
        resp.put("role", role);
        return ResponseEntity.ok(resp);
    }

    /**
     * GET /api/auth/children
     * Returns list of child users with their student records (for child picker on login screen)
     */
    @GetMapping("/children")
    public ResponseEntity<Object> getChildren() {
        var children = userDao.findByRole("child");
        var result = children.stream().map(u -> {
            Student s = studentDao.findByUserId(u.getUserId());
            Map<String, Object> m = new HashMap<>();
            m.put("userId", u.getUserId());
            m.put("name", u.getName());
            m.put("studentId", s != null ? s.getStudentId() : null);
            m.put("initials", s != null ? s.getInitials() : "?");
            m.put("currentLetter", s != null ? s.getCurrentLetter() : "b");
            m.put("streakDays", s != null ? s.getStreakDays() : 0);
            return m;
        }).toList();
        return ResponseEntity.ok(result);
    }
}
