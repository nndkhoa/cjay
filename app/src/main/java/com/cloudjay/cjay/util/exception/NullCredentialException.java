package com.cloudjay.cjay.util.exception;

public class NullCredentialException extends Exception {

	public NullCredentialException() {
		super();
	}

	public NullCredentialException(String message) {
		super(message);
	}

	public NullCredentialException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
