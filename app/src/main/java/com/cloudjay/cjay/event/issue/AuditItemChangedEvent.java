package com.cloudjay.cjay.event.issue;

public class AuditItemChangedEvent {

    private String containerId;

    public AuditItemChangedEvent(String containerId) {
        this.containerId = containerId;
    }

    public String getContainerId() {
        return containerId;
    }
}
