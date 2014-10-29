package com.cloudjay.cjay.event.upload;

import com.cloudjay.cjay.util.enums.UploadType;

public class UploadStartedEvent {

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
