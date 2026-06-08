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
    private int weeklyProgress;   //will be in percentage format
    private String primaryIssue;  //for example: "b/d reversal"
    private String trend;         // "↑" | "→" | "↓"

    public Student() {}
    public Student(int studentId, int userId, int parentId, int educatorId,
                   String name, String initials, int streakDays,
                   String currentLetter, int weeklyProgress,
                   String primaryIssue, String trend) {
        this.studentId = studentId;
        this.userId = userId;
        this.parentId = parentId;
        this.educatorId = educatorId;
        this.name = name;
        this.initials = initials;
        this.streakDays = streakDays;
        this.currentLetter = currentLetter;
        this.weeklyProgress = weeklyProgress;
        this.primaryIssue = primaryIssue;
        this.trend = trend;
    }

    public int getStudentId(){ return studentId; }
    public void setStudentId(int id){ this.studentId = studentId; }

    public int getUserId(){ return userId; }
    public void setUserId(int userId){ this.userId = userId; }

    public int getParentId(){ return parentId; }
    public void setParentId(int parentId){ this.parentId = parentId; }

    public int getEducatorId(){ return educatorId; }
    public void setEducatorId(int educatorId){ this.educatorId = educatorId; }

    public String getName(){ return name; }
    public void setName(String name){ this.name = name; }

    public String getInitials(){ return initials; }
    public void setInitials(String initials){ this.initials = initials; }

    public int getStreakDays(){ return streakDays; }
    public void setStreakDays(int streakDays){ this.streakDays = streakDays; }

    public String getCurrentLetter(){ return currentLetter; }
    public void setCurrentLetter(String currentLetter){ this.currentLetter = currentLetter; }

    public int getWeeklyProgress(){ return weeklyProgress; }
    public void setWeeklyProgress(int weeklyProgress){ this.weeklyProgress = weeklyProgress; }

    public String getPrimaryIssue(){ return primaryIssue; }
    public void setPrimaryIssue(String primaryIssue){ this.primaryIssue = primaryIssue; }

    public String getTrend(){ return trend; }
    public void setTrend(String trend){ this.trend = trend; }

    @Override
    public String toString() {
        return "Student{id=" + studentId + ", name='" + name + "', initials='" + initials
                + "', progress=" + weeklyProgress + "%, trend='" + trend + "'}";
    }
}