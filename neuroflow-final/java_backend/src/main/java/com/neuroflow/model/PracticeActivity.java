package com.neuroflow.model;

public class PracticeActivity {
    private int id;
    private int educatorId;
    private String title;
    private String description;
    private boolean completed;
    private String weekDate;
    private String activityType;
    private boolean sharedWithParents;

    public PracticeActivity() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getEducatorId() { return educatorId; }
    public void setEducatorId(int educatorId) { this.educatorId = educatorId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public String getWeekDate() { return weekDate; }
    public void setWeekDate(String weekDate) { this.weekDate = weekDate; }
    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }
    public boolean isSharedWithParents() { return sharedWithParents; }
    public void setSharedWithParents(boolean sharedWithParents) { this.sharedWithParents = sharedWithParents; }
}
