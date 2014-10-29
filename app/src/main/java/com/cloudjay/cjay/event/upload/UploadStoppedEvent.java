package com.cloudjay.cjay.event.upload;

public class UploadStoppedEvent {
	public String containerId;

	public UploadStoppedEvent(String containerId) {
		this.containerId = containerId;
	}

}
