package com.cloudjay.cjay.util;

public enum UserRole {
	NONE(0), AUDITOR(1), X(2), Y(3), REPAIR_STAFF(4), Z(5), GATE_KEEPER(6);

	private int value;

	private UserRole(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
