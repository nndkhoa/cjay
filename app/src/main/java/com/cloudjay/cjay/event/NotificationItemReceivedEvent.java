package com.cloudjay.cjay.event;

import com.cloudjay.cjay.model.Session;

public class NotificationItemReceivedEvent {

	Session session;
	int type;

	public NotificationItemReceivedEvent(Session session, int type) {
		this.session = session;
		this.type = type;
	}

	public Session getSession() {
		return session;
	}

	public int getType() {
		return type;
	}
}
