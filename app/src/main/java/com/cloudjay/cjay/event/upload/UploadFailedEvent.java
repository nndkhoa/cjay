package com.cloudjay.cjay.event.upload;

public class UploadFailedEvent {
	public String containerId;

	public UploadFailedEvent(String containerId) {
		this.containerId = containerId;
	}

}
