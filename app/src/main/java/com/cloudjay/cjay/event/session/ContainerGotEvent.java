package com.cloudjay.cjay.event.session;

import com.cloudjay.cjay.model.Session;

public class ContainerGotEvent {
	private Session session;
	private String containerId;

	public ContainerGotEvent(Session session, String containerId) {
		this.session = session;
		this.containerId = containerId;
	}

	public Session getSession() {
		return session;
	}

	public String containerId() {
		return containerId;
	}
}
