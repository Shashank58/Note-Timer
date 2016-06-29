package com.notetimer;

/**
 * @author shashankm
 */
public class Task {
    private String description, createdAt;
    private int isRunning, elapsedTime, isStopped;
    private long id;

    public Task(String description, int isRunning, int elapsedTime, int isStopped, String createdAt) {
        this.description = description;
        this.isRunning = isRunning;
        this.elapsedTime = elapsedTime;
        this.isStopped = isStopped;
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

    public int getIsStopped() {
        return isStopped;
    }

    public void setIsStopped(int isStopped) {
        this.isStopped = isStopped;
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
