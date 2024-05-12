package com.example.soulscrypt.NotificationPackages;

public class NotificationModel {
    private String title;
    private String context;
    private String createdAt;
    private String isRead; // Add this field

    public NotificationModel(String title, String context, String createdAt, String isRead) {
        this.title = title;
        this.context = context;
        this.createdAt = createdAt;
        this.isRead = isRead;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getIsRead() {
        return isRead;
    }

    public void setIsRead(String isRead) {
        this.isRead = isRead;
    }
}
