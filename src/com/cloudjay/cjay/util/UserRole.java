package com.cloudjay.cjay.util;

public enum UserRole {
	NONE(0), AUDITOR(1), OFFICE_ADMIN(2), OFFICE_SUPERVISOR(3), REPAIR_STAFF(4), OPERATOR(5), GATE_KEEPER(6);

	private int value;

	private UserRole(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
