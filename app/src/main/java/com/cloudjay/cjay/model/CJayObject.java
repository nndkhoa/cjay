package com.cloudjay.cjay.model;

import java.io.Serializable;

public class CJayObject implements Serializable {

	Class cls;
	Object object;

	public int getQueuePriority() {
		return queuePriority;
	}

	public void setQueuePriority(int queuePriority) {
		this.queuePriority = queuePriority;
	}

	int queuePriority;

	public int getSessionPriority() {
		return sessionPriority;
	}

	public void setSessionPriority(int sessionPriority) {
		this.sessionPriority = sessionPriority;
	}

	int sessionPriority;

	public CJayObject() {
	}

	public CJayObject(Object obj) {
		this.object = obj;
	}

	public CJayObject(Object obj, Class cls, int queuePriority, int sessionPriority) {
		this.object = obj;
		this.cls = cls;
		this.queuePriority = queuePriority;
		this.sessionPriority = sessionPriority;
	}

	public GateImage getGateImage() {

		if (cls == GateImage.class || object instanceof GateImage) {
			return (GateImage) object;
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

}
