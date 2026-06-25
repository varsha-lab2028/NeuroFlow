package com.neuroflow.model;

public class DigitAttempt {
    private int id;
    private int studentId;
    private String targetDigit;
    private String recognizedDigit;
    private boolean correct;
    private double confidence;
    private String sessionDate;
    private String createdAt;

    public DigitAttempt() {}

    public DigitAttempt(int studentId, String targetDigit, String recognizedDigit,
                        boolean correct, double confidence) {
        this.studentId = studentId;
        this.targetDigit = targetDigit;
        this.recognizedDigit = recognizedDigit;
        this.correct = correct;
        this.confidence = confidence;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public String getTargetDigit() { return targetDigit; }
    public void setTargetDigit(String targetDigit) { this.targetDigit = targetDigit; }
    public String getRecognizedDigit() { return recognizedDigit; }
    public void setRecognizedDigit(String recognizedDigit) { this.recognizedDigit = recognizedDigit; }
    public boolean isCorrect() { return correct; }
    public void setCorrect(boolean correct) { this.correct = correct; }
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    public String getSessionDate() { return sessionDate; }
    public void setSessionDate(String sessionDate) { this.sessionDate = sessionDate; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
