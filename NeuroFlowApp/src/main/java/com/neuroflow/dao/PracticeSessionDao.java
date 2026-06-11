package com.neuroflow.dao;

import com.neuroflow.config.DatabaseManager;
import com.neuroflow.model.PracticeSession;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PracticeSessionDao {
    private final Connection conn = DatabaseManager.get().conn();

    private PracticeSession map(ResultSet rs) throws SQLException {
        PracticeSession s = new PracticeSession();
        s.setSessionId(rs.getInt("id"));
        s.setStudentId(rs.getInt("student_id"));
        s.setTargetLetter(rs.getString("target_letter"));
        s.setDetectedLetter(rs.getString("detected_letter"));
        s.setCorrect(rs.getInt("is_correct") == 1);
        s.setConfidence(rs.getDouble("confidence"));
        s.setAttempts(rs.getInt("attempts"));
        s.setDurationSeconds(rs.getInt("duration_seconds"));
        s.setCreatedAt(rs.getString("created_at"));
        return s;
    }

    public int insert(PracticeSession session) {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO practice_sessions(student_id, target_letter, detected_letter, " +
                        "is_correct, confidence, attempts, duration_seconds) VALUES(?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, session.getStudentId());
            ps.setString(2, session.getTargetLetter());
            ps.setString(3, session.getDetectedLetter());
            ps.setInt(4, session.isCorrect() ? 1 : 0);
            ps.setDouble(5, session.getConfidence());
            ps.setInt(6, session.getAttempts());
            ps.setInt(7, session.getDurationSeconds());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    public List<PracticeSession> findByStudentId(int studentId) {
        List<PracticeSession> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM practice_sessions WHERE student_id=? ORDER BY created_at DESC")) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<PracticeSession> findTodayByStudentId(int studentId) {
        List<PracticeSession> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM practice_sessions WHERE student_id=? " +
                        "AND date(created_at) = date('now') ORDER BY created_at DESC")) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<PracticeSession> findRecentByStudentId(int studentId, int limit) {
        List<PracticeSession> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM practice_sessions WHERE student_id=? " +
                        "ORDER BY created_at DESC LIMIT ?")) {
            ps.setInt(1, studentId);
            ps.setInt(2, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public int sumDurationToday(int studentId) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT SUM(duration_seconds) FROM practice_sessions " +
                        "WHERE student_id=? AND date(created_at) = date('now')")) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int countTodayAttempts(int studentId) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT SUM(attempts) FROM practice_sessions " +
                        "WHERE student_id=? AND date(created_at) = date('now')")) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int countThisWeek(int studentId) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM practice_sessions " +
                        "WHERE student_id=? AND date(created_at) >= date('now', 'weekday 0', '-6 days')")) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int[] weeklyPracticeDays(int studentId) {
        int[] days = new int[7];
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT strftime('%w', created_at) as dow, COUNT(*) FROM practice_sessions " +
                        "WHERE student_id=? AND date(created_at) >= date('now', 'weekday 0', '-6 days') " +
                        "GROUP BY dow")) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int dow = rs.getInt("dow");
                if (dow >= 0 && dow < 7) days[dow] = rs.getInt(2);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return days;
    }
}