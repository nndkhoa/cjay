package com.cloudjay.cjay.event.upload;

import com.cloudjay.cjay.util.enums.UploadType;

public class ItemEnqueueEvent {

	public String containerId;
	public UploadType uploadType;

	public ItemEnqueueEvent(String containerId, UploadType type) {
		this.containerId = containerId;
		this.uploadType = type;
	}

	public ItemEnqueueEvent(String containerId, int type) {
		this.containerId = containerId;
		this.uploadType = UploadType.values()[type];
	}

}
