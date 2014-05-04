package com.cloudjay.cjay.util;

public enum ContainerState {

	NEW(0), AUDITED(1), PRICING(2), WAITING(3), REPAIRING(4), AVAILABLE(5), EXPORTED(6);
	private int value;

	private ContainerState(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

}
