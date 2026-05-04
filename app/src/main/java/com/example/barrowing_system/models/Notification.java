package com.example.barrowing_system.models;

import com.google.firebase.Timestamp;

public class Notification {
    private String id;
    private String userId;
    private String title;
    private String message;
    private String type; // "request_update", "penalty", "general"
    private boolean isRead;
    private Timestamp createdAt;

    public Notification() {}

    public Notification(String id, String userId, String title, String message,
                       String type, boolean isRead, Timestamp createdAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    public String getId()          { return id; }
    public String getUserId()      { return userId; }
    public String getTitle()       { return title; }
    public String getMessage()     { return message; }
    public String getType()        { return type; }
    public boolean isRead()        { return isRead; }
    public Timestamp getCreatedAt() { return createdAt; }

    public void setId(String id)                   { this.id = id; }
    public void setUserId(String userId)           { this.userId = userId; }
    public void setTitle(String title)             { this.title = title; }
    public void setMessage(String message)         { this.message = message; }
    public void setType(String type)               { this.type = type; }
    public void setRead(boolean read)              { this.isRead = read; }
    public void setCreatedAt(Timestamp createdAt)  { this.createdAt = createdAt; }
}
