package com.cloudjay.cjay.event.upload;

public class UploadedEvent {
    private String containerId;

    public UploadedEvent(String containerId) {
        this.containerId= containerId;
    }

    public String getContainerId() {
        return containerId;
    }
}
