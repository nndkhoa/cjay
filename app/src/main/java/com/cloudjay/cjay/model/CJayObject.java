package com.cloudjay.cjay.model;

import java.io.Serializable;

public class CJayObject implements Serializable {

	Class cls;
	Object object;

	public CJayObject() {
	}

	public CJayObject(Object obj) {
		this.object = obj;
	}

	public CJayObject(Object obj, Class cls) {
		this.object = obj;
		this.cls = cls;
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
