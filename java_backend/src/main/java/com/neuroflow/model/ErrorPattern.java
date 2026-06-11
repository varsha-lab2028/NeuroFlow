package com.neuroflow.model;

public class ErrorPattern {
    private int id;
    private int studentId;
    private String errorType;
    private String weekStart;
    private int count;

    public ErrorPattern() {}
    public ErrorPattern(int id, int studentId, String errorType, String weekStart, int count) {
        this.id = id; this.studentId = studentId; this.errorType = errorType;
        this.weekStart = weekStart; this.count = count;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public String getErrorType() { return errorType; }
    public void setErrorType(String errorType) { this.errorType = errorType; }
    public String getWeekStart() { return weekStart; }
    public void setWeekStart(String weekStart) { this.weekStart = weekStart; }
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
}
