package com.cloudjay.cjay.util.enums;

public enum Role {
	GATE(6);

	public final int value;

	Role(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
