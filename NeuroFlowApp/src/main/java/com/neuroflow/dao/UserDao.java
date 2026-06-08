package com.neuroflow.dao;

import com.neuroflow.config.DatabaseManager;
import com.neuroflow.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDao{
    private final Connection conn = DatabaseManager.get().conn();

    public User findById(int id) {
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<User> findByRole(String role) {
        List<User> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE role=?")) {
            ps.setString(1, role);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public User findFirstByRole(String role) {
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE role=? LIMIT 1")) {
            ps.setString(1, role);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM users ORDER BY id")) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public User authenticate(String role, String pin) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM users WHERE role=? AND pin=? LIMIT 1")) {
            ps.setString(1, role);
            ps.setString(2, pin);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public int insert(User u) {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO users(name, role, pin) VALUES(?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getName());
            ps.setString(2, u.getRole());
            ps.setString(3, u.getPin());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    public void update(User u) {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE users SET name=?, role=?, pin=? WHERE id=?")) {
            ps.setString(1, u.getName());
            ps.setString(2, u.getRole());
            ps.setString(3, u.getPin());
            ps.setInt(4, u.getUserId());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void delete(int id) {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("id"));
        u.setName(rs.getString("name"));
        u.setRole(rs.getString("role"));
        u.setPin(rs.getString("pin"));
        u.setCreatedAt(rs.getString("created_at"));
        return u;
    }
}