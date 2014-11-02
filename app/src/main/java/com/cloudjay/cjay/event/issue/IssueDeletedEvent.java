package com.cloudjay.cjay.event.issue;

/**
 * Created by nambv on 28/10/2014.
 */
public class IssueDeletedEvent {

    private String containerId;

    public IssueDeletedEvent(String containerId) {
        this.containerId = containerId;
    }

    public String getContainerId() {
        return containerId;
    }
}
