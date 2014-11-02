package com.cloudjay.cjay.util.enums;

public enum Step {

	AUDIT(0),
	REPAIR(1),
	AVAILABLE(2),
	EXPORTED(3),
	CLEAR(5),
    IMPORT(4);

	public final int value;

	Step(int value) {
		this.value = value;
	}
}
