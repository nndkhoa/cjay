package com.cloudjay.cjay.event;

/**
 * Created by thai on 09/10/2014.
 */
public class UploadedEvent {
    private String containerId;

    public UploadedEvent(String containerId) {
        this.containerId= containerId;
    }

    public String getContainerId() {
        return containerId;
    }
}
