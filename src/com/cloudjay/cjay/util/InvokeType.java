package com.cloudjay.cjay.util;

public enum InvokeType {
	FIRST_TIME(0), FOLLOWING(1), NOTIFICATION(2);

	private int value;

	private InvokeType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

}
