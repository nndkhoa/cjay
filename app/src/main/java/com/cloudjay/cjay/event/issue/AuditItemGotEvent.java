package com.cloudjay.cjay.event.issue;

import com.cloudjay.cjay.model.AuditItem;

/**
 * Created by nambv on 2014/11/12.
 */
public class AuditItemGotEvent {
    private AuditItem auditItem;

    public AuditItemGotEvent(AuditItem auditItem) {
        this.auditItem = auditItem;
    }

    public AuditItem getAuditItem() {
        return this.auditItem;
    }
}
