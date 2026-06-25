package com.neuroflow.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.sql.*;
import java.time.LocalDate;

@Component
public class DatabaseManager {

    @Value("${app.db.path}")
    private String dbPath;

    private Connection connection;

    @PostConstruct
    public void init() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            connection.createStatement().execute("PRAGMA foreign_keys = ON");
            createTables();
            seedDataIfEmpty();
            System.out.println("[DB] Connected to " + dbPath);
        } catch (Exception e) {
            System.err.println("[DB] Init error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Connection conn() { return connection; }

    private void createTables() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("""
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                role TEXT NOT NULL,
                pin TEXT DEFAULT NULL,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP
            )""");
        st.execute("""
            CREATE TABLE IF NOT EXISTS students (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER REFERENCES users(id),
                parent_id INTEGER REFERENCES users(id),
                educator_id INTEGER REFERENCES users(id),
                name TEXT NOT NULL,
                initials TEXT NOT NULL,
                streak_days INTEGER DEFAULT 0,
                current_letter TEXT DEFAULT 'b',
                weekly_progress INTEGER DEFAULT 0,
                primary_issue TEXT DEFAULT '',
                trend TEXT DEFAULT '→'
            )""");
        st.execute("""
            CREATE TABLE IF NOT EXISTS practice_sessions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                student_id INTEGER REFERENCES students(id),
                target_letter TEXT NOT NULL,
                detected_letter TEXT,
                is_correct INTEGER DEFAULT 0,
                confidence REAL DEFAULT 0.0,
                attempts INTEGER DEFAULT 0,
                duration_seconds INTEGER DEFAULT 0,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP
            )""");
        st.execute("""
            CREATE TABLE IF NOT EXISTS error_patterns (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                student_id INTEGER REFERENCES students(id),
                error_type TEXT NOT NULL,
                week_start TEXT NOT NULL,
                count INTEGER DEFAULT 0
            )""");
        st.execute("""
            CREATE TABLE IF NOT EXISTS practice_activities (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                educator_id INTEGER REFERENCES users(id),
                title TEXT NOT NULL,
                description TEXT,
                completed INTEGER DEFAULT 0,
                week_date TEXT NOT NULL,
                activity_type TEXT DEFAULT 'classroom_focus',
                shared_with_parents INTEGER DEFAULT 0
            )""");
        st.execute("""
            CREATE TABLE IF NOT EXISTS app_settings (
                key TEXT PRIMARY KEY,
                value TEXT NOT NULL
            )""");
        // ML integration tables
        st.execute("""
            CREATE TABLE IF NOT EXISTS digit_attempts (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                student_id INTEGER REFERENCES students(id),
                target_digit TEXT NOT NULL,
                recognized_digit TEXT,
                is_correct INTEGER DEFAULT 0,
                confidence REAL DEFAULT 0.0,
                session_date TEXT DEFAULT CURRENT_TIMESTAMP,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP
            )""");
        st.execute("""
            CREATE TABLE IF NOT EXISTS none_events (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                student_id INTEGER REFERENCES students(id),
                event_date TEXT NOT NULL,
                count INTEGER DEFAULT 1,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP
            )""");
        st.execute("""
            CREATE TABLE IF NOT EXISTS level_progress (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                student_id INTEGER REFERENCES students(id),
                level_id INTEGER NOT NULL,
                completed INTEGER DEFAULT 0,
                unlocked INTEGER DEFAULT 0,
                completed_at TEXT,
                UNIQUE(student_id, level_id)
            )""");
        st.close();
    }

    private void seedDataIfEmpty() throws SQLException {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users");
        int count = rs.getInt(1);
        rs.close(); st.close();
        if (count > 0) return;

        System.out.println("[DB] Seeding initial data...");

        String[][] users = {
            {"Aarav M.", "child", null},
            {"Diya R.", "child", null},
            {"Ishaan P.", "child", null},
            {"Meera S.", "child", null},
            {"Rohan K.", "child", null},
            {"Parent User", "parent", "1234"},
            {"Ms. Sharma", "educator", "5678"}
        };
        PreparedStatement ps = connection.prepareStatement(
            "INSERT INTO users(name,role,pin) VALUES(?,?,?)");
        for (String[] u : users) {
            ps.setString(1, u[0]); ps.setString(2, u[1]); ps.setString(3, u[2]);
            ps.executeUpdate();
        }
        ps.close();

        String[][] students = {
            {"1","6","7","Aarav M.","AM","4","b","80","b/d reversal","↑"},
            {"2","6","7","Diya R.","DR","2","d","65","stroke order","↑"},
            {"3","6","7","Ishaan P.","IP","1","b","45","b/d reversal","→"},
            {"4","6","7","Meera S.","MS","6","p","90","","↑"},
            {"5","6","7","Rohan K.","RK","0","b","30","b/d reversal","↓"}
        };
        ps = connection.prepareStatement(
            "INSERT INTO students(user_id,parent_id,educator_id,name,initials,streak_days," +
            "current_letter,weekly_progress,primary_issue,trend) VALUES(?,?,?,?,?,?,?,?,?,?)");
        for (String[] s : students) {
            for (int i = 0; i < s.length; i++) ps.setString(i + 1, s[i]);
            ps.executeUpdate();
        }
        ps.close();

        String[][] sessions = {
            {"1","b","b","1","0.98","5","720"},
            {"1","b","d","0","0.94","3","480"},
            {"1","d","d","1","0.97","4","600"},
            {"1","p","q","0","0.89","6","900"},
            {"2","b","b","1","0.95","4","660"},
            {"3","b","d","0","0.91","7","840"},
            {"4","b","b","1","0.99","3","540"},
            {"5","b","d","0","0.85","8","1020"}
        };
        ps = connection.prepareStatement(
            "INSERT INTO practice_sessions(student_id,target_letter,detected_letter," +
            "is_correct,confidence,attempts,duration_seconds) VALUES(?,?,?,?,?,?,?)");
        for (String[] s : sessions) {
            for (int i = 0; i < s.length; i++) ps.setString(i + 1, s[i]);
            ps.executeUpdate();
        }
        ps.close();

        LocalDate monday = LocalDate.now();
        while (monday.getDayOfWeek().getValue() != 1) monday = monday.minusDays(1);
        String today = monday.toString();
        String[][] errors = {
            {"1","b/d reversal",today,"24"}, {"2","b/d reversal",today,"14"},
            {"3","stroke direction",today,"12"}, {"4","b/d reversal",today,"0"},
            {"5","starting point",today,"9"},  {"1","p/q reversal",today,"7"},
            {"3","b/d reversal",today,"11"},   {"5","b/d reversal",today,"3"}
        };
        ps = connection.prepareStatement(
            "INSERT INTO error_patterns(student_id,error_type,week_start,count) VALUES(?,?,?,?)");
        for (String[] e : errors) {
            for (int i = 0; i < e.length; i++) ps.setString(i + 1, e[i]);
            ps.executeUpdate();
        }
        ps.close();

        String[][] activities = {
            {"7","Letters b & d","Visual discrimination — bump direction awareness","1",today,"classroom_focus","1"},
            {"7","Letters p & q","Below-the-line letter shapes","0",today,"classroom_focus","0"},
            {"7","Numbers 1–5","Number formation and counting","0",today,"classroom_focus","0"},
            {"7","Sequencing patterns","What comes next — shapes and colours","0",today,"classroom_focus","0"}
        };
        ps = connection.prepareStatement(
            "INSERT INTO practice_activities(educator_id,title,description,completed," +
            "week_date,activity_type,shared_with_parents) VALUES(?,?,?,?,?,?,?)");
        for (String[] a : activities) {
            for (int i = 0; i < a.length; i++) ps.setString(i + 1, a[i]);
            ps.executeUpdate();
        }
        ps.close();

        System.out.println("[DB] Seed complete.");
    }

    @PreDestroy
    public void close() {
        try { if (connection != null) connection.close(); }
        catch (SQLException e) { e.printStackTrace(); }
    }
}
