package com.cloudjay.cjay.event.issue;

import com.cloudjay.cjay.model.AuditItem;

import java.util.List;

/**
 * Created by nambv on 2015/01/22.
 */
public class RepairedItemsGotEvent {

    private List<AuditItem> auditItems;

    public RepairedItemsGotEvent(List<AuditItem> list) {
        this.auditItems = list;
    }

    public List<AuditItem> getAuditItems() {
        return auditItems;
    }
}
