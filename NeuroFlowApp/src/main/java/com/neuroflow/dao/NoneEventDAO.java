package com.neuroflow.dao;

import com.neuroflow.config.DatabaseManager;
import com.neuroflow.model.NoneEvent;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NoneEventDAO {
    private final Connection conn = DatabaseManager.get().conn();

    private NoneEvent map(ResultSet rs) throws SQLException {
        NoneEvent e = new NoneEvent();
        e.setId(rs.getInt("id"));
        e.setStudentId(rs.getInt("student_id"));
        e.setEventDate(rs.getString("event_date"));
        e.setCount(rs.getInt("count"));
        e.setCreatedAt(rs.getString("created_at"));
        return e;
    }

    /**
     * Called every time NONE fires for a student.
     * If a row already exists for today, increments count.
     * If not, inserts a new row with count = 1.
     */
    public void incrementToday(int studentId) {
        String today = java.time.LocalDate.now().toString();
        try (PreparedStatement check = conn.prepareStatement(
                "SELECT id FROM none_events " +
                        "WHERE student_id=? AND event_date=?")) {
            check.setInt(1, studentId);
            check.setString(2, today);
            ResultSet rs = check.executeQuery();
            if (rs.next()) {
                int existingId = rs.getInt("id");
                try (PreparedStatement upd = conn.prepareStatement(
                        "UPDATE none_events SET count=count+1 WHERE id=?")) {
                    upd.setInt(1, existingId);
                    upd.executeUpdate();
                }
            } else {
                try (PreparedStatement ins = conn.prepareStatement(
                        "INSERT INTO none_events(student_id, event_date, count) " +
                                "VALUES(?, ?, 1)")) {
                    ins.setInt(1, studentId);
                    ins.setString(2, today);
                    ins.executeUpdate();
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    /**
     * Returns daily NONE counts for a student
     * for the last N days, ordered oldest to newest.
     */
    public List<NoneEvent> getRecentDays(int studentId, int days) {
        List<NoneEvent> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM none_events " +
                        "WHERE student_id=? " +
                        "AND event_date >= date('now', '-' || ? || ' days') " +
                        "ORDER BY event_date ASC")) {
            ps.setInt(1, studentId);
            ps.setInt(2, days);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /**
     * Returns all NONE events for a student, oldest first.
     */
    public List<NoneEvent> getAllForStudent(int studentId) {
        List<NoneEvent> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM none_events WHERE student_id=? " +
                        "ORDER BY event_date ASC")) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}
