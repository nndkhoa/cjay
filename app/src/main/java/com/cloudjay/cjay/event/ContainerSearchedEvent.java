package com.cloudjay.cjay.event;

import com.cloudjay.cjay.model.Session;

import io.realm.RealmObject;
import io.realm.RealmResults;

public class ContainerSearchedEvent {

	public RealmResults<Session> getSessions() {
		return sessions;
	}

	public RealmResults<Session> getTargets() {
		return sessions;
	}

	public RealmObject getTarget() {
		if (isSingleChange()) {
			return sessions.get(0);
		} else {
			throw new IllegalStateException("Can only call this when isSingleChange returns true");
		}
	}

	private RealmResults<Session> sessions;

	public ContainerSearchedEvent(RealmResults<Session> sessions) {
		this.sessions = sessions;
	}

	public boolean isSingleChange() {
		return sessions.size() == 1;
	}
}
