package com.cloudjay.cjay.model;

import java.io.Serializable;

public class UploadObject implements Serializable {

	public Class getCls() {
		return cls;
	}

	Class cls;
	Object object;
	long sessionId;
	String containerId;

	public UploadObject() {
	}

	public UploadObject(Object obj) {
		this.object = obj;
	}

	public UploadObject(Object obj, Class cls, String containerId) {
		this.object = obj;
		this.cls = cls;
		this.containerId = containerId;
	}

	public UploadObject(Object obj, Class cls, long id) {
		this.object = obj;
		this.cls = cls;
		this.sessionId = id;
	}

	public UploadObject(Object obj, Class cls, String containerId, long id) {
		this.object = obj;
		this.cls = cls;
		this.sessionId = id;
		this.containerId = containerId;
	}

	public GateImage getGateImage() {

		if (cls == GateImage.class || object instanceof GateImage) {
			return (GateImage) object;
		} else {
			throw new IllegalStateException("This object is not a GateImage");
		}
	}

	public long getSessionId() {
		return sessionId;
	}

	public String getContainerId() {
		if (cls == GateImage.class || object instanceof GateImage) {
			return containerId;
		} else if (cls == Session.class || object instanceof Session) {
			return ((Session) object).getContainerId();
		} else if (cls == AuditImage.class || object instanceof AuditImage) {
			return containerId;
		} else if (cls == AuditItem.class || object instanceof AuditItem) {
			return containerId;
		} else {
			throw new IllegalStateException("This object is not a GateImage");
		}
	}

	public AuditImage getAuditImage() {
		if (cls == AuditImage.class || object instanceof AuditImage) {
			return (AuditImage) object;
		} else {
			throw new IllegalStateException("This object is not a AuditImage");
		}
	}

	public AuditItem getAuditItem() {
		if (cls == AuditItem.class || object instanceof AuditItem) {
			return (AuditItem) object;
		} else {
			throw new IllegalStateException("This object is not a AuditItem");
		}
	}

	public Session getSession() {
		if (cls == Session.class || object instanceof Session) {
			return (Session) object;
		} else {
			throw new IllegalStateException("This object is not a Session");
		}
	}

	public UploadObject mergeCJayObject(UploadObject cJayObject) {
		this.sessionId = cJayObject.getSessionId();

		if (cls == GateImage.class || object instanceof GateImage) {
			return cJayObject;
		} else if (cls == Session.class || object instanceof Session) {
			this.sessionId = cJayObject.getSession().getId();
			return this;
		} else if (cls == AuditImage.class || object instanceof AuditImage) {
			return cJayObject;
		} else if (cls == AuditItem.class || object instanceof AuditItem) {
			this.object = cJayObject.getAuditItem();
			return this;
		} else {
			throw new IllegalStateException("This object is not a GateImage");
		}
	}
}
