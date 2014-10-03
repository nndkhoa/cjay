package com.cloudjay.cjay.data.api;

public enum Step {

	AUDIT(0),
	REPAIR(1),
	AVAILABLE(2),
	EXPORT(3);

	public final int value;

	Step(int value) {
		this.value = value;
	}
}
