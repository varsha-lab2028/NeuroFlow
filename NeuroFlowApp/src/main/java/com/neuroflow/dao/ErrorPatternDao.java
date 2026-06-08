package com.neuroflow.dao;

import com.neuroflow.config.DatabaseManager;
import com.neuroflow.model.ErrorPattern;
import java.sql.*;
import java.util.*;

public class ErrorPatternDao{
    private final Connection conn = DatabaseManager.get().conn();

    private ErrorPattern map(ResultSet rs) throws SQLException {
        ErrorPattern ep = new ErrorPattern();
        ep.setId(rs.getInt("id"));
        ep.setStudentId(rs.getInt("student_id"));
        ep.setErrorType(rs.getString("error_type"));
        ep.setWeekStart(rs.getString("week_start"));
        ep.setCount(rs.getInt("count"));
        return ep;
    }

    public List<ErrorPattern> findByStudentId(int studentId) {
        List<ErrorPattern> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM error_patterns WHERE student_id=? ORDER BY week_start DESC")) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<ErrorPattern> findByStudentAndWeek(int studentId, String weekStart) {
        List<ErrorPattern> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM error_patterns WHERE student_id=? AND week_start=?")) {
            ps.setInt(1, studentId);
            ps.setString(2, weekStart);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Map<String, Integer> weeklyTotals(String weekStart) {
        Map<String, Integer> totals = new LinkedHashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT error_type, SUM(count) as total FROM error_patterns " +
                        "WHERE week_start=? GROUP BY error_type ORDER BY total DESC")) {
            ps.setString(1, weekStart);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) totals.put(rs.getString("error_type"), rs.getInt("total"));
        } catch (SQLException e) { e.printStackTrace(); }
        return totals;
    }

    public Map<String, Integer> allTimeTotals() {
        Map<String, Integer> totals = new LinkedHashMap<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT error_type, SUM(count) as total FROM error_patterns " +
                             "GROUP BY error_type ORDER BY total DESC")) {
            while (rs.next()) totals.put(rs.getString("error_type"), rs.getInt("total"));
        } catch (SQLException e) { e.printStackTrace(); }
        return totals;
    }

    public void upsert(ErrorPattern ep) {
        try (PreparedStatement check = conn.prepareStatement(
                "SELECT id FROM error_patterns WHERE student_id=? AND error_type=? AND week_start=?")) {
            check.setInt(1, ep.getStudentId());
            check.setString(2, ep.getErrorType());
            check.setString(3, ep.getWeekStart());
            ResultSet rs = check.executeQuery();
            if (rs.next()) {
                int existingId = rs.getInt("id");
                try (PreparedStatement upd = conn.prepareStatement(
                        "UPDATE error_patterns SET count=count+? WHERE id=?")) {
                    upd.setInt(1, ep.getCount());
                    upd.setInt(2, existingId);
                    upd.executeUpdate();
                }
            } else {
                try (PreparedStatement ins = conn.prepareStatement(
                        "INSERT INTO error_patterns(student_id, error_type, week_start, count) " +
                                "VALUES(?, ?, ?, ?)")) {
                    ins.setInt(1, ep.getStudentId());
                    ins.setString(2, ep.getErrorType());
                    ins.setString(3, ep.getWeekStart());
                    ins.setInt(4, ep.getCount());
                    ins.executeUpdate();
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void insert(ErrorPattern ep) {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO error_patterns(student_id, error_type, week_start, count) " +
                        "VALUES(?, ?, ?, ?)")) {
            ps.setInt(1, ep.getStudentId());
            ps.setString(2, ep.getErrorType());
            ps.setString(3, ep.getWeekStart());
            ps.setInt(4, ep.getCount());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}