package com.neuroflow.dao;

import com.neuroflow.config.DatabaseManager;
import com.neuroflow.model.Student;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class StudentDao {

    private final DatabaseManager db;
    public StudentDao(DatabaseManager db) { this.db = db; }

    private Connection conn() { return db.conn(); }

    private Student map(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setStudentId(rs.getInt("id"));
        s.setUserId(rs.getInt("user_id"));
        s.setParentId(rs.getInt("parent_id"));
        s.setEducatorId(rs.getInt("educator_id"));
        s.setName(rs.getString("name"));
        s.setInitials(rs.getString("initials"));
        s.setStreakDays(rs.getInt("streak_days"));
        s.setCurrentLetter(rs.getString("current_letter"));
        s.setWeeklyProgress(rs.getInt("weekly_progress"));
        s.setPrimaryIssue(rs.getString("primary_issue"));
        s.setTrend(rs.getString("trend"));
        return s;
    }

    public Student findById(int id) {
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM students WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public Student findByUserId(int userId) {
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM students WHERE user_id=?")) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<Student> findByEducatorId(int educatorId) {
        List<Student> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM students WHERE educator_id=? ORDER BY name")) {
            ps.setInt(1, educatorId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Student> findByParentId(int parentId) {
        List<Student> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM students WHERE parent_id=? ORDER BY name")) {
            ps.setInt(1, parentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Student> findAll() {
        List<Student> list = new ArrayList<>();
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM students ORDER BY name")) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public void update(Student s) {
        try (PreparedStatement ps = conn().prepareStatement(
                "UPDATE students SET streak_days=?, current_letter=?, weekly_progress=?," +
                " primary_issue=?, trend=? WHERE id=?")) {
            ps.setInt(1, s.getStreakDays());
            ps.setString(2, s.getCurrentLetter());
            ps.setInt(3, s.getWeeklyProgress());
            ps.setString(4, s.getPrimaryIssue());
            ps.setString(5, s.getTrend());
            ps.setInt(6, s.getStudentId());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public int countActive() {
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM students")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int countPracticedToday() {
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT COUNT(DISTINCT student_id) FROM practice_sessions " +
                     "WHERE date(created_at) = date('now')")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }
}
