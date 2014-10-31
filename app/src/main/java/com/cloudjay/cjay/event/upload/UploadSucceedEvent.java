package com.cloudjay.cjay.event.upload;

public class UploadSucceedEvent {
    private String containerId;

    public UploadSucceedEvent(String containerId) {
        this.containerId= containerId;
    }

    public String getContainerId() {
        return containerId;
    }
}
