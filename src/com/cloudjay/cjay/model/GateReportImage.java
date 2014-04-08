package com.cloudjay.cjay.model;

public class GateReportImage {

	public static final String FIELD_STATE = "state";
	static final String FIELD_URI = "uri";

	private int id;
	private int type;
	private String created_at;
	private String image_name;

	public GateReportImage() {

	}

	public GateReportImage(int id, int type, String time_posted, String image_name) {
		this.id = id;
		this.type = type;
		created_at = time_posted;
		this.image_name = image_name;
	}

	public GateReportImage(int type, String time_posted, String image_name) {
		this.type = type;
		created_at = time_posted;
		this.image_name = image_name;
	}

	public String getCreatedAt() {
		return created_at;
	}

	public int getId() {
		return id;
	}

	public String getImageName() {
		return image_name;
	}

	public int getType() {
		return type;
	}

	public void setCreatedAt(String time_posted) {
		created_at = time_posted;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setImageName(String image_name) {
		this.image_name = image_name;
	}

	public void setType(int type) {
		this.type = type;
	}
}
