package com.neuroflow.model;

public class Student {
    private int studentId;
    private int userId;
    private int parentId;
    private int educatorId;
    private String name;
    private String initials;
    private int streakDays;
    private String currentLetter;
    private int weeklyProgress;
    private String primaryIssue;
    private String trend;

    public Student() {}

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getParentId() { return parentId; }
    public void setParentId(int parentId) { this.parentId = parentId; }
    public int getEducatorId() { return educatorId; }
    public void setEducatorId(int educatorId) { this.educatorId = educatorId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getInitials() { return initials; }
    public void setInitials(String initials) { this.initials = initials; }
    public int getStreakDays() { return streakDays; }
    public void setStreakDays(int streakDays) { this.streakDays = streakDays; }
    public String getCurrentLetter() { return currentLetter; }
    public void setCurrentLetter(String currentLetter) { this.currentLetter = currentLetter; }
    public int getWeeklyProgress() { return weeklyProgress; }
    public void setWeeklyProgress(int weeklyProgress) { this.weeklyProgress = weeklyProgress; }
    public String getPrimaryIssue() { return primaryIssue; }
    public void setPrimaryIssue(String primaryIssue) { this.primaryIssue = primaryIssue; }
    public String getTrend() { return trend; }
    public void setTrend(String trend) { this.trend = trend; }
}
