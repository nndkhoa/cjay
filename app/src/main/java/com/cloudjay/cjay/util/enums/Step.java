package com.cloudjay.cjay.util.enums;

public enum Step {

	AUDIT(0),
	REPAIR(1),
	AVAILABLE(2),
	EXPORTED(3),
    IMPORT(4),
	CLEAR(5),
	EXPORTIMMEDIATELY(6);

	public final int value;

	Step(int value) {
		this.value = value;
	}
}
