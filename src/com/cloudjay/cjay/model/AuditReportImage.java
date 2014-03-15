package com.cloudjay.cjay.model;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

public class AuditReportImage {

	public static final String FIELD_STATE = "state";
	static final String FIELD_URI = "uri";

	private int id;
	private int type;
	private String time_posted;
	private String image_name;

	public AuditReportImage() {

	}

	public AuditReportImage(int id, int type, String time_posted,
			String image_name) {
		this.id = id;
		this.type = type;
		this.time_posted = time_posted;
		this.image_name = image_name;
	}

	public AuditReportImage(int type, String time_posted, String image_name) {
		this.type = type;
		this.time_posted = time_posted;
		this.image_name = image_name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getTimePosted() {
		return time_posted;
	}

	public void setTimePosted(String time_posted) {
		this.time_posted = time_posted;
	}

	public String getImageName() {
		return image_name;
	}

	public void setImageName(String image_name) {
		this.image_name = image_name;
	}
}
