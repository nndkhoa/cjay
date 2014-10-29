package com.cloudjay.cjay.util.enums;

public enum UploadType {

	IMAGE(0),
	AUDIT_ITEM(1),
	SESSION(2);

	public final int value;

	UploadType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}

