package com.neuroflow.dao;

import com.neuroflow.config.DatabaseManager;
import com.neuroflow.model.PracticeActivity;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PracticeActivityDao {
    private final Connection conn = DatabaseManager.get().conn();

    private PracticeActivity map(ResultSet rs) throws SQLException {
        PracticeActivity a = new PracticeActivity();
        a.setId(rs.getInt("id"));
        a.setEducatorId(rs.getInt("educator_id"));
        a.setTitle(rs.getString("title"));
        a.setDescription(rs.getString("description"));
        a.setCompleted(rs.getInt("completed") == 1);
        a.setWeekDate(rs.getString("week_date"));
        a.setActivityType(rs.getString("activity_type"));
        a.setSharedWithParents(rs.getInt("shared_with_parents") == 1);
        return a;
    }

    public int insert(PracticeActivity a) {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO practice_activities(educator_id, title, description, completed, " +
                        "week_date, activity_type, shared_with_parents) VALUES(?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, a.getEducatorId());
            ps.setString(2, a.getTitle());
            ps.setString(3, a.getDescription());
            ps.setInt(4, a.isCompleted() ? 1 : 0);
            ps.setString(5, a.getWeekDate());
            ps.setString(6, a.getActivityType());
            ps.setInt(7, a.isSharedWithParents() ? 1 : 0);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    public PracticeActivity findById(int id) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM practice_activities WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<PracticeActivity> findByEducatorId(int educatorId) {
        List<PracticeActivity> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM practice_activities WHERE educator_id=? ORDER BY week_date DESC")) {
            ps.setInt(1, educatorId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<PracticeActivity> findByWeekDate(String weekDate) {
        List<PracticeActivity> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM practice_activities WHERE week_date=? ORDER BY id")) {
            ps.setString(1, weekDate);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<PracticeActivity> findSharedWithParents() {
        List<PracticeActivity> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT * FROM practice_activities WHERE shared_with_parents=1 ORDER BY week_date DESC")) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<PracticeActivity> findAll() {
        List<PracticeActivity> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT * FROM practice_activities ORDER BY week_date DESC")) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public void markCompleted(int id, boolean completed) {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE practice_activities SET completed=? WHERE id=?")) {
            ps.setInt(1, completed ? 1 : 0);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void shareWithParents(int id, boolean shared) {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE practice_activities SET shared_with_parents=? WHERE id=?")) {
            ps.setInt(1, shared ? 1 : 0);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void delete(int id) {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM practice_activities WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}