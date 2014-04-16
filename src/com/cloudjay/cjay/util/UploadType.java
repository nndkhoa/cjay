package com.cloudjay.cjay.util;

public enum UploadType {

	NONE(0), IN(1), AUDIT(2), REPAIR(3), OUT(4);

	private int value;

	private UploadType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
