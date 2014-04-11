package com.cloudjay.cjay.events;

/**
 * Trigger when Application need to log user activity
 * 
 * @author tieubao
 * 
 */
public class LogUserActivityEvent {

	private final String message;

	public LogUserActivityEvent(String msg) {
		message = msg;
	}

	public String getTarget() {
		return message;
	}
}
