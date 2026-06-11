package com.neuroflow;

import com.neuroflow.model.Student;
import com.neuroflow.model.User;

public class AppState {
    private static AppState instance;

    // Current logged-in user and role
    private User currentUser;
    private String currentRole = "child";

    // Current child being tracked
    private Student currentStudent;

    // Currently selected letter in the practice flow
    private String selectedLetter = "b";

    // Gripper hardware connection state
    private boolean gripperConnected = false;

    // Session tracking (within a single Try attempt)
    private int sessionAttempts = 0;
    private int sessionCorrect  = 0;
    private long sessionStartMs = 0;

    private AppState() {}

    public static AppState get() {
        if (instance == null) instance = new AppState();
        return instance;
    }

    // ─ User ─────────────────────────────────────────────────────
    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User user) { this.currentUser = user; }

    public String getCurrentRole() { return currentRole; }
    public void setCurrentRole(String role) { this.currentRole = role; }

    // ── Student ──────────────────────────────────────────────────
    public Student getCurrentStudent() { return currentStudent; }
    public void setCurrentStudent(Student student) { this.currentStudent = student; }

    // ── Selected letter ──────────────────────────────────────────
    public String getSelectedLetter() { return selectedLetter; }
    public void setSelectedLetter(String letter) { this.selectedLetter = letter; }

    // ── Gripper ──────────────────────────────────────────────────
    public boolean isGripperConnected() { return gripperConnected; }
    public void setGripperConnected(boolean connected) { this.gripperConnected = connected; }

    // ── Session tracking ─────────────────────────────────────────
    public void startSession() {
        sessionAttempts = 0;
        sessionCorrect  = 0;
        sessionStartMs  = System.currentTimeMillis();
    }

    public void incrementAttempts() { sessionAttempts++; }
    public void incrementCorrect()  { sessionCorrect++;  }

    public int getSessionAttempts() { return sessionAttempts; }
    public int getSessionCorrect()  { return sessionCorrect;  }

    public int getSessionDurationSeconds() {
        if (sessionStartMs == 0) return 0;
        return (int) ((System.currentTimeMillis() - sessionStartMs) / 1000);
    }

    // ── Reset ────────────────────────────────────────────────────
    public void resetSession() {
        sessionAttempts = 0;
        sessionCorrect  = 0;
        sessionStartMs  = 0;
    }
}
