package com.cloudjay.cjay.util;

public class NullSessionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3745157026344306321L;

	public NullSessionException() {
		super();
	}

	public NullSessionException(String message) {
		super(message);
	}

	public NullSessionException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
