package com.cloudjay.cjay.util;

public enum ContainerState {

	IMPORTED(0), CHECKED(1), CLASSIFIED(2), NOTIFIED(3), APPROVED(4), REPAIRED(5), EXPORTED(6);
	private int value;

	private ContainerState(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

}
