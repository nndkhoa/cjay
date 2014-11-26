package com.cloudjay.cjay.event.session;

/**
 * Event được trigger khi bắt đầu search session từ SERVER.
 */
public class SearchAsyncStartedEvent {
	public String beginSearchOnServer;

	public SearchAsyncStartedEvent(String beginSearchOnServer) {
		this.beginSearchOnServer = beginSearchOnServer;
	}

	public String getStringEvent() {
		return beginSearchOnServer;
	}
}
