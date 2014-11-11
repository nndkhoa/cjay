package com.cloudjay.cjay.event;

import com.cloudjay.cjay.model.Session;

import java.util.List;

/**
 * Created by thai on 11/11/2014.
 */
public class ContainerGotEvent {
	public ContainerGotEvent(Session session, String containerId) {
		this.session = session;
		this.containerId = containerId;
	}

	public Session getSession() {
		return session;
	}

	private Session session;

	public String containerId() {
		return containerId;
	}

	private String containerId;

	public Session getTargets() {
		return session;
	}

}
