package com.neuroflow.model;

public class NoneEvent {
    private int id;
    private int studentId;
    private String eventDate;  // ISO date: "2025-06-09"
    private int count;
    private String createdAt;

    public NoneEvent() {}

    public NoneEvent(int id, int studentId,
                     String eventDate, int count, String createdAt) {
        this.id        = id;
        this.studentId = studentId;
        this.eventDate = eventDate;
        this.count     = count;
        this.createdAt = createdAt;
    }

    public int getId()                           { return id; }
    public void setId(int id)                    { this.id = id; }
    public int getStudentId()                    { return studentId; }
    public void setStudentId(int studentId)      { this.studentId = studentId; }
    public String getEventDate()                 { return eventDate; }
    public void setEventDate(String eventDate)   { this.eventDate = eventDate; }
    public int getCount()                        { return count; }
    public void setCount(int count)              { this.count = count; }
    public String getCreatedAt()                 { return createdAt; }
    public void setCreatedAt(String createdAt)   { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "NoneEvent{studentId=" + studentId
                + ", date='" + eventDate
                + "', count=" + count + "}";
    }
}
