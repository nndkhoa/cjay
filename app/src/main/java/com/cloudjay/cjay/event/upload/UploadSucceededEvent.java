package com.cloudjay.cjay.event.upload;

import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.enums.UploadType;

public class UploadSucceededEvent {
	private Session session;
	public UploadType uploadType;
	public String containerId;

	public UploadSucceededEvent(Session session, UploadType type) {
		this.session = session;
		this.uploadType = type;
	}

	public UploadSucceededEvent(Session session, int type) {
		this.session = session;
		this.uploadType = UploadType.values()[type];
	}

	public UploadSucceededEvent(String containerId, UploadType type) {
		this.containerId = containerId;
		this.uploadType = type;
	}

	public Session getSession() {
		return session;
	}

	public UploadType getUploadType() {
		return uploadType;
	}
}
