package com.cloudjay.cjay.util.enums;

/**
 * Created by thai on 09/10/2014.
 */
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
