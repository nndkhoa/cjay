package com.cloudjay.cjay.event.issue;

public class IssueUpdatedEvent {

    private String containerId;

    public IssueUpdatedEvent(String containerId) {
        this.containerId = containerId;
    }

    public String getContainerId() {
        return containerId;
    }
}
