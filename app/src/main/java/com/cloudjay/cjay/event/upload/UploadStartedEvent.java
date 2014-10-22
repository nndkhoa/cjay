package com.cloudjay.cjay.event.upload;

public class UploadStartedEvent {
    private String containerId;

    public UploadStartedEvent(String containerId) {
        this.containerId= containerId;
    }

    public String getContainerId() {
        return containerId;
    }
}
