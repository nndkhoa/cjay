package com.cloudjay.cjay.event.issue;

import com.cloudjay.cjay.model.AuditItem;

/**
 * Created by nambv on 2014/11/12.
 */
public class AuditItemGotEvent {
    private AuditItem auditItem;
    private boolean cameraMode;

    public AuditItemGotEvent(AuditItem auditItem, boolean cameraMode) {
        this.auditItem = auditItem;
        this.cameraMode = cameraMode;
    }

    public AuditItem getAuditItem() {
        return this.auditItem;
    }
    public boolean isCameraMode() {
        return this.cameraMode;
    }
}
