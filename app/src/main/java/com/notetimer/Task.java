package com.notetimer;

/**
 * @author shashankm
 */
public class Task {
    private String description, pauseTime, createdAt;
    private int isRunning, elapsedTime;
    private long id;

    public Task(String description, int isRunning, int elapsedTime, String pauseTime, String createdAt) {
        this.description = description;
        this.isRunning = isRunning;
        this.elapsedTime = elapsedTime;
        this.pauseTime = pauseTime;
        this.createdAt = createdAt;
    }

    public int getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(int elapsedTime) {
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

    public void setIsRunning(int isRunning) {
        this.isRunning = isRunning;
    }
}
