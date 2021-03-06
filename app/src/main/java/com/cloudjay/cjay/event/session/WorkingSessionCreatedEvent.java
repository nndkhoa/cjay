package com.cloudjay.cjay.event.session;

import com.cloudjay.cjay.model.Session;

/**
 * Sự kiện được kích hoạt khi một WorkingSession được tạo ra.
 * Dùng để update UI của WorkingFragment.
 */
public class WorkingSessionCreatedEvent {

	Session session;

	public WorkingSessionCreatedEvent(Session current) {
		this.session = current;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}
}
