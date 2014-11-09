package com.cloudjay.cjay.event.issue;

import com.cloudjay.cjay.model.AuditItem;

import java.util.List;

public class AuditItemsGotEvent {

	private List<AuditItem> auditItems;

	public List<AuditItem> getAuditItems() {
		return auditItems;
	}

	public AuditItemsGotEvent(List<AuditItem> list) {
		this.auditItems = list;
	}

}
