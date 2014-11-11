package com.cloudjay.cjay.event.upload;

import com.cloudjay.cjay.model.Session;

public class UploadStoppedEvent {
	public Session session;
	String containerId;

	public UploadStoppedEvent(Session session) {
		this.session = session;
	}

	public UploadStoppedEvent(String containerId) {
		this.containerId = containerId;
	}
}
