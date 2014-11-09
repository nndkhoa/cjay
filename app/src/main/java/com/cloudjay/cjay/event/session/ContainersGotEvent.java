package com.cloudjay.cjay.event.session;

import com.cloudjay.cjay.model.Session;

import java.util.List;

public class ContainersGotEvent {

	private List<Session> sessions;

	public String getPrefix() {
		return prefix;
	}

	private String prefix;

	public List<Session> getSessions() {
		return sessions;
	}

	public ContainersGotEvent(List<Session> list, String prefix) {
		this.sessions = list;
		this.prefix = prefix;
	}

}
