package com.cloudjay.cjay.event.session;

import com.cloudjay.cjay.model.Session;

import java.util.List;

public class ContainersGotEvent {

	public Session getTarget() {
		if (isSingleChange())
			return sessions.get(0);
		else
			throw new IllegalStateException("Can only call this when isSingleChange returns true, current size is:" + sessions.size());
	}

	public boolean isSingleChange() {
		return sessions.size() == 1;
	}


	private List<Session> sessions;

	public String getPrefix() {
		return prefix;
	}

	private String prefix;

	public List<Session> getTargets() {
		return sessions;
	}

	public ContainersGotEvent(List<Session> list, String prefix) {
		this.sessions = list;
		this.prefix = prefix;
	}

}
