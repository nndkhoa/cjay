package com.cloudjay.cjay.model;

public class LogItem {
    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    String containerId;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    String time;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    String message;
}
