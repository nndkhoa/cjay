package com.cloudjay.cjay.util;

public enum InvokeType {

	FIRST_TIME(0), // get data
	FOLLOWING(1), // return
	NOTIFICATION(2), // get data
	FORCE_REFRESH(3); // get data

	private int value;

	private InvokeType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

}
