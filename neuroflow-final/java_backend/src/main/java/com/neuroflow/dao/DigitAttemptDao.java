package com.neuroflow.dao;

import com.neuroflow.config.DatabaseManager;
import com.neuroflow.model.DigitAttempt;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

@Repository
public class DigitAttemptDao {

    private final DatabaseManager db;
    public DigitAttemptDao(DatabaseManager db) { this.db = db; }

    private Connection conn() { return db.conn(); }

    public int insert(DigitAttempt attempt) {
        try (PreparedStatement ps = conn().prepareStatement(
                "INSERT INTO digit_attempts(student_id, target_digit, recognized_digit, " +
                "is_correct, confidence) VALUES(?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, attempt.getStudentId());
            ps.setString(2, attempt.getTargetDigit());
            ps.setString(3, attempt.getRecognizedDigit());
            ps.setInt(4, attempt.isCorrect() ? 1 : 0);
            ps.setDouble(5, attempt.getConfidence());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            return keys.next() ? keys.getInt(1) : -1;
        } catch (SQLException e) {
            System.err.println("[DigitAttemptDao] insert error: " + e.getMessage());
            return -1;
        }
    }

    /** Returns the latest attempt for this student, or null */
    public DigitAttempt findLatest(int studentId) {
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM digit_attempts WHERE student_id=? ORDER BY id DESC LIMIT 1")) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) {
            System.err.println("[DigitAttemptDao] findLatest error: " + e.getMessage());
        }
        return null;
    }

    /** Returns distinct correct digits written today */
    public List<String> correctDigitsToday(int studentId) {
        String today = LocalDate.now().toString();
        List<String> digits = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT DISTINCT target_digit FROM digit_attempts " +
                "WHERE student_id=? AND is_correct=1 AND date(session_date)=?")) {
            ps.setInt(1, studentId);
            ps.setString(2, today);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) digits.add(rs.getString("target_digit"));
        } catch (SQLException e) {
            System.err.println("[DigitAttemptDao] correctDigitsToday error: " + e.getMessage());
        }
        return digits;
    }

    /** Returns true if all 9 digits (1–9) have been correctly written today */
    public boolean allDigitsCompletedToday(int studentId) {
        List<String> done = correctDigitsToday(studentId);
        Set<String> needed = new HashSet<>(Arrays.asList("1","2","3","4","5","6","7","8","9"));
        return done.containsAll(needed);
    }

    private DigitAttempt map(ResultSet rs) throws SQLException {
        DigitAttempt a = new DigitAttempt();
        a.setId(rs.getInt("id"));
        a.setStudentId(rs.getInt("student_id"));
        a.setTargetDigit(rs.getString("target_digit"));
        a.setRecognizedDigit(rs.getString("recognized_digit"));
        a.setCorrect(rs.getInt("is_correct") == 1);
        a.setConfidence(rs.getDouble("confidence"));
        a.setSessionDate(rs.getString("session_date"));
        a.setCreatedAt(rs.getString("created_at"));
        return a;
    }
}
