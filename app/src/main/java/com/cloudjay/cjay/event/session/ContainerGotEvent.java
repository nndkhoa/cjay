package com.cloudjay.cjay.event.session;

import com.cloudjay.cjay.model.Session;

/**
 * Created by thai on 11/11/2014.
 */
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

	public Session getTargets() {
		return session;
	}

}
