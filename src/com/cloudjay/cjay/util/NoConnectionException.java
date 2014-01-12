package com.cloudjay.cjay.util;

public class NoConnectionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5294673916999798477L;

	public NoConnectionException() {
		super();
	}

	public NoConnectionException(String message) {
		super(message);
	}

	public NoConnectionException(String message, Throwable throwable) {
		super(message, throwable);
	}

}
