package com.cloudjay.cjay.activity;

import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.enums.UploadType;

/**
 * Created by thai on 11/11/2014.
 */
public class PreUploadSucceededEvent {
	private Session session;
	public UploadType uploadType;
	public String containerId;

	public PreUploadSucceededEvent(Session session, UploadType type) {
		this.session = session;
		this.uploadType = type;
	}

	public PreUploadSucceededEvent(Session session, int type) {
		this.session = session;
		this.uploadType = UploadType.values()[type];
	}

	public PreUploadSucceededEvent(String containerId, UploadType type) {
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
