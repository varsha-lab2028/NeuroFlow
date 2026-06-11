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
    public PracticeSession(int sessionId, int studentId, String targetLetter,
                           String detectedLetter, boolean correct,
                           double confidence, int attempts,
                           int durationSeconds, String createdAt) {
        this.sessionId = sessionId;
        this.studentId = studentId;
        this.targetLetter = targetLetter;
        this.detectedLetter = detectedLetter;
        this.correct = correct;
        this.confidence = confidence;
        this.attempts = attempts;
        this.durationSeconds = durationSeconds;
        this.createdAt = createdAt;
    }

    public int getSessionId(){ return sessionId; }
    public void setSessionId(int id){ this.sessionId = sessionId; }

    public int getStudentId(){ return studentId; }
    public void setStudentId(int studentId){ this.studentId = studentId; }

    public String getTargetLetter(){ return targetLetter; }
    public void setTargetLetter(String targetLetter){ this.targetLetter = targetLetter; }

    public String getDetectedLetter(){ return detectedLetter; }
    public void setDetectedLetter(String detectedLetter){ this.detectedLetter = detectedLetter; }

    public boolean isCorrect(){ return correct; }
    public void setCorrect(boolean correct){ this.correct = correct; }

    public double getConfidence(){ return confidence; }
    public void setConfidence(double confidence){ this.confidence = confidence; }

    public int getAttempts(){ return attempts; }
    public void setAttempts(int attempts){ this.attempts = attempts; }

    public int getDurationSeconds(){ return durationSeconds; }
    public void setDurationSeconds(int durationSeconds){ this.durationSeconds = durationSeconds; }

    public String getCreatedAt(){ return createdAt; }
    public void setCreatedAt(String createdAt){ this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "PracticeSession{id=" + sessionId + ", studentId=" + studentId
                + ", target='" + targetLetter + "', detected='" + detectedLetter
                + "', correct=" + correct + ", confidence=" + confidence + "}";
    }
}