package com.cloudjay.cjay.event.upload;

import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.enums.UploadType;

public class UploadStartedEvent {

	public Session session;
	public UploadType uploadType;
	public String containerId;

	public UploadStartedEvent(String containerId, UploadType type) {
		this.containerId = containerId;
		this.uploadType = type;
	}
	public UploadStartedEvent(Session session, UploadType type) {
		this.session = session;
		this.uploadType = type;
	}
	public UploadStartedEvent(Session session, int type) {
		this.session = session;
		this.uploadType = UploadType.values()[type];
	}

	public Session getSession() {
		return session;
	}

	public void setContainerId(Session session) {
		this.session = session;
	}

	public UploadType getUploadType() {
		return uploadType;
	}
}
