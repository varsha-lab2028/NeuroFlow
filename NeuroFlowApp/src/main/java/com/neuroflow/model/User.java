//this class is for the people who will be using the app:
// student, parent, educator and the admin
package com.neuroflow.model;

public class User {
    private int userId;
    private String name;
    private String role;   //parent, child or educator
    private String pin;
    private String createdAt; //time when the user made the account

    //constructor
    public User() {}
    public User(int id, String name, String role, String pin, String createdAt) {
        this.userId = id;
        this.name = name;
        this.role = role;
        this.pin = pin;
        this.createdAt = createdAt;
    }

    public int getUserId(){ return userId; }
    public void setUserId(int id){ this.userId = id; }

    public String getName(){ return name; }
    public void setName(String name){ this.name = name; }

    public String getRole(){ return role; }
    public void setRole(String role){ this.role = role; }

    public String getPin(){ return pin; }
    public void setPin(String pin){ this.pin = pin; }

    public String getCreatedAt(){ return createdAt; }
    public void setCreatedAt(String createdAt){ this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "User{id=" + userId + ", name='" + name + "', role='" + role + "'}";
    }
}