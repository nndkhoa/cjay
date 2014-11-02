package com.cloudjay.cjay.event.issue;

/**
 * Created by nambv on 29/10/2014.
 */
public class IssueMergedEvent {
    private String containerId;
    private String auditItemRemoveUUID;

    public IssueMergedEvent(String containerId, String auditItemRemoveUUID) {
        this.containerId = containerId;
        this.auditItemRemoveUUID = auditItemRemoveUUID;
    }

    public String getContainerId() {
        return containerId;
    }

    public String getAuditItemRemoveUUID() {
        return auditItemRemoveUUID;
    }
}
