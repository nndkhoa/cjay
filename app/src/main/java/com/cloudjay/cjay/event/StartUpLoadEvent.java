package com.cloudjay.cjay.event;

/**
 * Created by thai on 16/10/2014.
 */
public class StartUpLoadEvent {
    private String containerId;

    public StartUpLoadEvent(String containerId) {
        this.containerId= containerId;
    }

    public String getContainerId() {
        return containerId;
    }
}
