package com.cloudjay.cjay.model;

public class AuditReportImage {

	private int id;
	private int type;
	private String created_at;
	private String image_name;
	private String image_url;

	public AuditReportImage() {

	}

	public AuditReportImage(int id, int type, String created_at, String image_name, String image_url) {

		this(type, created_at, image_name, image_url);
		this.id = id;

		// this.type = type;
		// this.time_posted = time_posted;
		// this.image_name = image_name;
	}

	public AuditReportImage(int type, String created_at, String image_name, String image_url) {
		this.type = type;
		this.created_at = created_at;
		this.image_name = image_name;
		this.image_url = image_url;
	}

	public int getId() {
		return id;
	}

	public String getImageName() {
		return image_name;
	}

	public String getCreatedAt() {
		return created_at;
	}

	public int getType() {
		return type;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setImageName(String image_name) {
		this.image_name = image_name;
	}

	public void setCreatedAt(String time_posted) {
		this.created_at = time_posted;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getImageUrl() {
		return image_url;
	}

	public void setImageUrl(String image_url) {
		this.image_url = image_url;
	}
}
