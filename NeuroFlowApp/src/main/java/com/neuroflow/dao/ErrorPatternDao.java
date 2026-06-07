package main.java.com.neuroflow.dao;

import com.neuroflow.model.ErrorPattern;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class ErrorPatternDAO {
    private final Connection conn = DatabaseManager.get().conn();

    private ErrorPattern map(ResultSet rs) throws SQLException {
        ErrorPattern ep = new ErrorPattern();
        ep.setId(rs.getInt("id")); ep.setStudentId(rs.getInt("student_id"));
        ep.setErrorType(rs.getString("error_type"));
        String ws = rs.getString("week_start");
        if (ws != null) try { ep.setWeekStart(LocalDate.parse(ws)); } catch (Exception ignored) {}
        ep.setCount(rs.getInt("count"));
        return ep;
    }

    public List<ErrorPattern> findByStudentAndWeek(int studentId, LocalDate weekStart) {
        List<ErrorPattern> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM error_patterns WHERE student_id=? AND week_start=?")) {
            ps.setInt(1, studentId); ps.setString(2, weekStart.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /** Returns aggregated error counts for ALL students for the current week, keyed by error_type. */
    public Map<String, Integer> weeklyTotals(LocalDate weekStart) {
        Map<String, Integer> totals = new LinkedHashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT error_type, SUM(count) as total FROM error_patterns WHERE week_start=? GROUP BY error_type ORDER BY total DESC")) {
            ps.setString(1, weekStart.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) totals.put(rs.getString("error_type"), rs.getInt("total"));
        } catch (SQLException e) { e.printStackTrace(); }
        return totals;
    }

    public void upsert(ErrorPattern ep) {
        try {
            PreparedStatement check = conn.prepareStatement(
                "SELECT id FROM error_patterns WHERE student_id=? AND error_type=? AND week_start=?");
            check.setInt(1, ep.getStudentId()); check.setString(2, ep.getErrorType());
            check.setString(3, ep.getWeekStart().toString());
            ResultSet rs = check.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id");
                PreparedStatement upd = conn.prepareStatement("UPDATE error_patterns SET count=count+? WHERE id=?");
                upd.setInt(1, ep.getCount()); upd.setInt(2, id); upd.executeUpdate();
            } else {
                PreparedStatement ins = conn.prepareStatement(
                    "INSERT INTO error_patterns(student_id,error_type,week_start,count) VALUES(?,?,?,?)");
                ins.setInt(1, ep.getStudentId()); ins.setString(2, ep.getErrorType());
                ins.setString(3, ep.getWeekStart().toString()); ins.setInt(4, ep.getCount());
                ins.executeUpdate();
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
}