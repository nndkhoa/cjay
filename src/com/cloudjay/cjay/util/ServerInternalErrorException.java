package com.cloudjay.cjay.util;

public class ServerInternalErrorException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5294673916999798477L;

	public ServerInternalErrorException() {
		super();
	}

	public ServerInternalErrorException(String message) {
		super(message);
	}

	public ServerInternalErrorException(String message, Throwable throwable) {
		super(message, throwable);
	}

}
