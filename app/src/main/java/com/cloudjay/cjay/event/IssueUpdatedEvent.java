package com.cloudjay.cjay.event;

/**
 * Created by nambv on 28/10/2014.
 */
public class IssueUpdatedEvent {

    private String containerId;

    public IssueUpdatedEvent(String containerId) {
        this.containerId = containerId;
    }

    public String getContainerId() {
        return containerId;
    }
}
