package com.cloudjay.cjay.util;

public enum UploadState {
	NONE(0), WAITING(1), IN_PROGRESS(2), ERROR(3), COMPLETED(4);

	private int value;

	private UploadState(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

}
