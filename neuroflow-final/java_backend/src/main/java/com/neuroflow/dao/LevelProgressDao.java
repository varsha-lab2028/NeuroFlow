package com.neuroflow.dao;

import com.neuroflow.config.DatabaseManager;
import com.neuroflow.model.LevelProgress;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

@Repository
public class LevelProgressDao {

    private final DatabaseManager db;
    public LevelProgressDao(DatabaseManager db) { this.db = db; }

    private Connection conn() { return db.conn(); }

    public void upsert(int studentId, int levelId, boolean completed, boolean unlocked) {
        try {
            PreparedStatement check = conn().prepareStatement(
                "SELECT id FROM level_progress WHERE student_id=? AND level_id=?");
            check.setInt(1, studentId);
            check.setInt(2, levelId);
            ResultSet rs = check.executeQuery();
            if (rs.next()) {
                PreparedStatement upd = conn().prepareStatement(
                    "UPDATE level_progress SET completed=?, unlocked=?, completed_at=? WHERE student_id=? AND level_id=?");
                upd.setInt(1, completed ? 1 : 0);
                upd.setInt(2, unlocked ? 1 : 0);
                upd.setString(3, completed ? LocalDateTime.now().toString() : null);
                upd.setInt(4, studentId);
                upd.setInt(5, levelId);
                upd.executeUpdate();
            } else {
                PreparedStatement ins = conn().prepareStatement(
                    "INSERT INTO level_progress(student_id, level_id, completed, unlocked, completed_at) VALUES(?,?,?,?,?)");
                ins.setInt(1, studentId);
                ins.setInt(2, levelId);
                ins.setInt(3, completed ? 1 : 0);
                ins.setInt(4, unlocked ? 1 : 0);
                ins.setString(5, completed ? LocalDateTime.now().toString() : null);
                ins.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("[LevelProgressDao] upsert error: " + e.getMessage());
        }
    }

    public List<LevelProgress> findByStudent(int studentId) {
        List<LevelProgress> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM level_progress WHERE student_id=? ORDER BY level_id ASC")) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                LevelProgress lp = new LevelProgress();
                lp.setId(rs.getInt("id"));
                lp.setStudentId(rs.getInt("student_id"));
                lp.setLevelId(rs.getInt("level_id"));
                lp.setCompleted(rs.getInt("completed") == 1);
                lp.setUnlocked(rs.getInt("unlocked") == 1);
                lp.setCompletedAt(rs.getString("completed_at"));
                list.add(lp);
            }
        } catch (SQLException e) {
            System.err.println("[LevelProgressDao] findByStudent error: " + e.getMessage());
        }
        return list;
    }
}
