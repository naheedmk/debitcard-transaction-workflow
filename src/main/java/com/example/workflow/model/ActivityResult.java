package com.example.workflow.model;

import java.time.Instant;

public class ActivityResult {
    private String name;
    private String status;
    private String message;
    private Instant startTime;
    private Instant endTime;

    public ActivityResult(String name) {
        this.name = name;
        this.startTime = Instant.now();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public void complete(String status, String message) {
        this.status = status;
        this.message = message;
        this.endTime = Instant.now();
    }
} 