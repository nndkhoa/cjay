package com.cloudjay.cjay.event.upload;

public class ContainerUploadedEvent {
    private String containerId;

    public ContainerUploadedEvent(String containerId) {
        this.containerId= containerId;
    }

    public String getContainerId() {
        return containerId;
    }
}
