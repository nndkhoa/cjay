package com.cloudjay.cjay.event.session;

import com.cloudjay.cjay.model.Session;

import java.util.List;

public class ContainersGotEvent {

	public List<Session> getSessions() {
		return sessions;
	}

	private List<Session> sessions;

	public ContainersGotEvent(List<Session> list) {
		this.sessions = list;
	}

}
