package com.neuroflow.dao;

import com.neuroflow.config.DatabaseManager;
import com.neuroflow.model.NoneEvent;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

@Repository
public class NoneEventDao {

    private final DatabaseManager db;
    public NoneEventDao(DatabaseManager db) { this.db = db; }

    private Connection conn() { return db.conn(); }

    /** Upsert: increment today's count or insert new row */
    public void incrementToday(int studentId, String today) {
        try {
            PreparedStatement check = conn().prepareStatement(
                "SELECT id FROM none_events WHERE student_id=? AND event_date=?");
            check.setInt(1, studentId);
            check.setString(2, today);
            ResultSet rs = check.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id");
                PreparedStatement upd = conn().prepareStatement(
                    "UPDATE none_events SET count=count+1 WHERE id=?");
                upd.setInt(1, id);
                upd.executeUpdate();
            } else {
                PreparedStatement ins = conn().prepareStatement(
                    "INSERT INTO none_events(student_id, event_date, count) VALUES(?,?,1)");
                ins.setInt(1, studentId);
                ins.setString(2, today);
                ins.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("[NoneEventDao] incrementToday error: " + e.getMessage());
        }
    }

    /** Returns last N days of none_events for a student */
    public List<NoneEvent> findLastNDays(int studentId, int days) {
        String since = LocalDate.now().minusDays(days - 1).toString();
        List<NoneEvent> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM none_events WHERE student_id=? AND event_date>=? ORDER BY event_date ASC")) {
            ps.setInt(1, studentId);
            ps.setString(2, since);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                NoneEvent e = new NoneEvent();
                e.setId(rs.getInt("id"));
                e.setStudentId(rs.getInt("student_id"));
                e.setEventDate(rs.getString("event_date"));
                e.setCount(rs.getInt("count"));
                list.add(e);
            }
        } catch (SQLException e) {
            System.err.println("[NoneEventDao] findLastNDays error: " + e.getMessage());
        }
        return list;
    }
}
