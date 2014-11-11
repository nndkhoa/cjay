package com.cloudjay.cjay.event.upload;

import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.enums.UploadType;

/**
 * Created by thai on 11/11/2014.
 */
public class PreUploadStartedEvent {

	public PreUploadStartedEvent(String containerId, UploadType type) {
		this.containerId = containerId;
		this.uploadType = type;
	}

	public Session getSession() {
		return session;
	}

	public void setContainerId(Session session) {
		this.session = session;
	}

	public Session session;
	public UploadType uploadType;
	public String containerId;

	public PreUploadStartedEvent(Session session, UploadType type) {
		this.session = session;
		this.uploadType = type;
	}

	public PreUploadStartedEvent(Session session, int type) {
		this.session = session;
		this.uploadType = UploadType.values()[type];
	}

	public UploadType getUploadType() {
		return uploadType;
	}
}
