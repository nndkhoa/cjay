package com.cloudjay.cjay.util.enums;

public enum ImageType {

	IMPORT(0),
	EXPORT(1),
	AUDIT(2),
	REPAIRED(3);

	public final int value;

	ImageType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}

