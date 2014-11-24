package com.cloudjay.cjay.event.issue;

public class IssueMergedEvent {
	private String containerId;
	private String itemUuid;

	public IssueMergedEvent(String containerId, String itemUuid) {
		this.containerId = containerId;
		this.itemUuid = itemUuid;
	}

	public String getContainerId() {
		return containerId;
	}

	public String getItemUuid() {
		return itemUuid;
	}
}
