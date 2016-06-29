package com.notetimer;

/**
 * @author shashankm
 */
public class Task {
    private String description, createdAt;
    private int taskStatus, elapsedTime;
    private long id;

    public Task(String description, int taskStatus, int elapsedTime, String createdAt) {
        this.description = description;
        this.taskStatus = taskStatus;
        this.elapsedTime = elapsedTime;
        this.createdAt = createdAt;
    }

    public int getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(int elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(int taskStatus) {
        this.taskStatus = taskStatus;
    }

    public String getDescription() {
        return description;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
