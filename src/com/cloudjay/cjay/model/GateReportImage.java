package com.cloudjay.cjay.model;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

public class GateReportImage {

	public static final String FIELD_STATE = "state";
	static final String FIELD_URI = "uri";

	private int id;
	private int type;
	private String created_at;
	private String image_name;

	public GateReportImage() {

	}

	public GateReportImage(int id, int type, String time_posted,
			String image_name) {
		this.id = id;
		this.type = type;
		this.created_at = time_posted;
		this.image_name = image_name;
	}

	public GateReportImage(int type, String time_posted, String image_name) {
		this.type = type;
		this.created_at = time_posted;
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

	public String getCreatedAt() {
		return created_at;
	}

	public void setCreatedAt(String time_posted) {
		this.created_at = time_posted;
	}

	public String getImageName() {
		return image_name;
	}

	public void setImageName(String image_name) {
		this.image_name = image_name;
	}
}
