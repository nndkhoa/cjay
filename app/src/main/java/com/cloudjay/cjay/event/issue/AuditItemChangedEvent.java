package com.cloudjay.cjay.event.issue;

import com.cloudjay.cjay.model.IsoCode;

public class AuditItemChangedEvent {

	private String containerId;

	public AuditItemChangedEvent(String containerId) {
		this.containerId = containerId;
	}

	public String getContainerId() {
		return containerId;
	}
}
