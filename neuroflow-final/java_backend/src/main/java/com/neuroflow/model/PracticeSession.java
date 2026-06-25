package com.neuroflow.model;

public class PracticeSession {
    private int sessionId;
    private int studentId;
    private String targetLetter;
    private String detectedLetter;
    private boolean correct;
    private double confidence;
    private int attempts;
    private int durationSeconds;
    private String createdAt;

    public PracticeSession() {}

    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public String getTargetLetter() { return targetLetter; }
    public void setTargetLetter(String targetLetter) { this.targetLetter = targetLetter; }
    public String getDetectedLetter() { return detectedLetter; }
    public void setDetectedLetter(String detectedLetter) { this.detectedLetter = detectedLetter; }
    public boolean isCorrect() { return correct; }
    public void setCorrect(boolean correct) { this.correct = correct; }
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }
    public int getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(int durationSeconds) { this.durationSeconds = durationSeconds; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
