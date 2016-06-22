package com.notetimer;

/**
 * @author shashankm
 */
public class Task {
    private String description, elapsedTime, pauseTime, createdAt;
    private int isRunning;
    private long id;

    public Task(String description, int isRunning, String elapsedTime, String pauseTime, String createdAt) {
        this.description = description;
        this.isRunning = isRunning;
        this.elapsedTime = elapsedTime;
        this.pauseTime = pauseTime;
        this.createdAt = createdAt;
    }

    public String getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(String elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public String getPauseTime() {
        return pauseTime;
    }

    public void setPauseTime(String pauseTime) {
        this.pauseTime = pauseTime;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getIsRunning() {
        return isRunning;
    }

    public String getDescription() {
        return description;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
