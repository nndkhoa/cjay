package com.cloudjay.cjay.model;

public class AuditReportImage {

	private int id;
	private int type;
	private String time_posted;
	private String image_name;
	private String image_url;

	public AuditReportImage() {

	}

	public AuditReportImage(int id, int type, String time_posted, String image_name) {
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

	public String getImageName() {
		return image_name;
	}

	public String getTimePosted() {
		return time_posted;
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

	public void setTimePosted(String time_posted) {
		this.time_posted = time_posted;
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
