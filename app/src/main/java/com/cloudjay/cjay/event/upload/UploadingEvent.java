package com.cloudjay.cjay.event.upload;

import com.cloudjay.cjay.util.enums.UploadType;

public class UploadingEvent {

	public String containerId;
	public UploadType uploadType;

	public UploadingEvent(String containerId, UploadType type) {
		this.containerId = containerId;
		this.uploadType = type;
	}

	public UploadingEvent(String containerId, int type) {
		this.containerId = containerId;
		this.uploadType = UploadType.values()[type];
	}

}
