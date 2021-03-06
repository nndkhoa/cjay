package com.cloudjay.cjay.event.session;

import com.cloudjay.cjay.model.Session;

import java.util.List;

;

public class ContainerSearchedEvent {

	public boolean searchInImportFragment;
	private List<Session> sessions;
	private boolean failed = false;

	public ContainerSearchedEvent(List<Session> sessions) {
		this.sessions = sessions;
	}

	public ContainerSearchedEvent(boolean failed) {
		this.failed = failed;
	}

	public ContainerSearchedEvent(List<Session> sessions, boolean searchInImportFragment) {
		this.sessions = sessions;
		this.searchInImportFragment = searchInImportFragment;
	}

	public List<Session> getSessions() {
		return sessions;
	}

	public List<Session> getTargets() {
		return sessions;
	}

	public Session getTarget() {
		if (isSingleChange()) {
			return sessions.get(0);
		} else {
			throw new IllegalStateException("Can only call this when isSingleChange returns true");
		}
	}

	public boolean isFailed() {
		return failed;
	}

	public void setFailed(boolean failed) {
		this.failed = failed;
	}

	public boolean isSingleChange() {
		return sessions.size() == 1;
	}

	public boolean isSearchInImportFragment() {
		return this.searchInImportFragment;
	}
}
