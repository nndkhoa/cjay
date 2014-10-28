package com.cloudjay.cjay.util.enums;

public enum UploadStatus {

	NONE(0),
	UPLOADING(1),
	COMPLETE(2),
	ERROR(3);

	public final int value;

	UploadStatus(int value) {
		this.value = value;
	}

}
