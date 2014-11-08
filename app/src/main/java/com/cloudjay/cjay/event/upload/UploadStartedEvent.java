package com.cloudjay.cjay.event.upload;

import com.cloudjay.cjay.util.enums.UploadType;

public class UploadStartedEvent {

	public String getContainerId() {
		return containerId;
	}

	public void setContainerId(String containerId) {
		this.containerId = containerId;
	}

	public String containerId;
	public UploadType uploadType;

	public UploadStartedEvent(String containerId, UploadType type) {
		this.containerId = containerId;
		this.uploadType = type;
	}

	public UploadStartedEvent(String containerId, int type) {
		this.containerId = containerId;
		this.uploadType = UploadType.values()[type];
	}

}
