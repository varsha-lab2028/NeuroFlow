package com.neuroflow.api;

import com.neuroflow.model.NoneEvent;
import com.neuroflow.model.Student;
import com.neuroflow.model.PracticeSession;
import com.neuroflow.service.*;
import java.util.List;
import static spark.Spark.*;

public class ApiServer {

    private static final int PORT = 8080;

    public static void start() {
        port(PORT);
        // Allow React frontend to call this API
        before((req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods",
                    "GET, POST, PUT, DELETE, OPTIONS");
            res.header("Access-Control-Allow-Headers",
                    "Content-Type, Authorization");
            res.type("application/json");
        });

        options("/*", (req, res) -> {
            res.status(200);
            return "OK";
        });

        // ── NONE event endpoints ──────────────────────────────────

        // Python calls this when NONE is detected
        // POST /api/none-event
        // Body: { "studentId": 1 }
        post("/api/none-event", (req, res) -> {
            try {
                String body  = req.body();
                int studentId = parseIntField(body, "studentId");
                NoneEventService.get().recordNoneEvent(studentId);
                res.status(200);
                return "{\"status\":\"ok\"}";
            } catch (Exception e) {
                res.status(400);
                return "{\"error\":\"" + e.getMessage() + "\"}";
            }
        });

        // React calls this to get trend data for the graph
        // GET /api/none-trend/:studentId
        get("/api/none-trend/:studentId", (req, res) -> {
            int studentId = Integer.parseInt(req.params("studentId"));
            List<NoneEvent> trend =
                    NoneEventService.get().getTrend(studentId);
            String direction =
                    NoneEventService.get().getTrendDirection(studentId);
            return toNoneTrendJson(trend, direction);
        });

        // ── Student endpoints ─────────────────────────────────────
        get("/api/students", (req, res) -> {
            List<Student> students = StudentService.get().getAllStudents();
            return toStudentsJson(students);
        });

        get("/api/students/:id", (req, res) -> {
            int id = Integer.parseInt(req.params("id"));
            Student s = StudentService.get().getById(id);
            if (s == null) { res.status(404); return "{\"error\":\"not found\"}"; }
            return toStudentJson(s);
        });

        // ── Practice session endpoints ────────────────────────────

        get("/api/sessions/:studentId", (req, res) -> {
            int studentId = Integer.parseInt(req.params("studentId"));
            List<PracticeSession> sessions =
                    PracticeSessionService.get().getTodaySessions(studentId);
            return toSessionsJson(sessions);
        });

        get("/api/analytics/:studentId", (req, res) -> {
            int studentId = Integer.parseInt(req.params("studentId"));
            int dur = PracticeSessionService.get().sumDurationToday(studentId);
            int att = PracticeSessionService.get().countTodayAttempts(studentId);
            int streak = 0;
            Student s = StudentService.get().getById(studentId);
            if (s != null) streak = s.getStreakDays();
            return "{\"duration\":" + dur
                    + ",\"attempts\":" + att
                    + ",\"streak\":" + streak + "}";
        });

        System.out.println("[API] Server running on port " + PORT);
    }

    // ── JSON builders — no external library needed ────────────────

    private static String toNoneTrendJson(
            List<NoneEvent> events, String direction) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"direction\":\"").append(direction).append("\",");
        sb.append("\"data\":[");
        for (int i = 0; i < events.size(); i++) {
            NoneEvent e = events.get(i);
            sb.append("{\"date\":\"").append(e.getEventDate()).append("\",");
            sb.append("\"count\":").append(e.getCount()).append("}");
            if (i < events.size() - 1) sb.append(",");
        }
        sb.append("]}");
        return sb.toString();
    }

    private static String toStudentsJson(List<Student> students) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < students.size(); i++) {
            sb.append(toStudentJson(students.get(i)));
            if (i < students.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private static String toStudentJson(Student s) {
        return "{\"id\":"           + s.getStudentId()
                + ",\"name\":\""       + s.getName()           + "\""
                + ",\"initials\":\""   + s.getInitials()        + "\""
                + ",\"progress\":"     + s.getWeeklyProgress()
                + ",\"streak\":"       + s.getStreakDays()
                + ",\"letter\":\""     + s.getCurrentLetter()   + "\""
                + ",\"issue\":\""      + s.getPrimaryIssue()    + "\""
                + ",\"trend\":\""      + s.getTrend()           + "\""
                + "}";
    }

    private static String toSessionsJson(List<PracticeSession> sessions) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < sessions.size(); i++) {
            PracticeSession ps = sessions.get(i);
            sb.append("{\"id\":").append(ps.getSessionId())
                    .append(",\"target\":\"").append(ps.getTargetLetter()).append("\"")
                    .append(",\"detected\":\"").append(ps.getDetectedLetter()).append("\"")
                    .append(",\"correct\":").append(ps.isCorrect())
                    .append(",\"confidence\":").append(ps.getConfidence())
                    .append("}");
            if (i < sessions.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private static int parseIntField(String json, String field) {
        String search = "\"" + field + "\":";
        int start = json.indexOf(search);
        if (start == -1) throw new RuntimeException("Missing field: " + field);
        start += search.length();
        int end = start;
        while (end < json.length()
                && Character.isDigit(json.charAt(end))) end++;
        return Integer.parseInt(json.substring(start, end));
    }
}