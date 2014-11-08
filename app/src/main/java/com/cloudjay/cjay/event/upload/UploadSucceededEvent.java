package com.cloudjay.cjay.event.upload;

import com.cloudjay.cjay.util.enums.UploadType;

public class UploadSucceededEvent {
    private String containerId;
	public UploadType uploadType;

	public UploadSucceededEvent(String containerId, UploadType type) {
		this.containerId = containerId;
		this.uploadType = type;
	}

	public UploadSucceededEvent(String containerId, int type) {
		this.containerId = containerId;
		this.uploadType = UploadType.values()[type];
	}

    public String getContainerId() {
        return containerId;
    }

	public UploadType getUploadType() {
		return uploadType;
	}
}
