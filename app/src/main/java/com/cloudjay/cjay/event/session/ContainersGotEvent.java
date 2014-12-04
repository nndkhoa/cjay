package com.cloudjay.cjay.event.session;

import com.cloudjay.cjay.model.Session;

import java.util.List;

public class ContainersGotEvent {

	private List<Session> sessions;
	private String prefix;


	public ContainersGotEvent(List<Session> list, String prefix) {
		this.sessions = list;
		this.prefix = prefix;
	}

	public Session getTarget() {
		if (isSingleChange())
			return sessions.get(0);
		else
			throw new IllegalStateException("Can only call this when isSingleChange returns true, current size is:" + sessions.size());
	}

	public boolean isSingleChange() {
		return sessions.size() == 1;
	}

	public String getPrefix() {
		return prefix;
	}

	public List<Session> getTargets() {
		return sessions;
	}

}
