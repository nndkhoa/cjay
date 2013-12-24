package com.cloudjay.cjay.model;

import java.util.Date;

public class GateReportImage {
	private int id;
	private int type;
	private Date time_posted;
	private String image_name;

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

	public Date getTimePosted() {
		return time_posted;
	}

	public void setTimePosted(Date time_posted) {
		this.time_posted = time_posted;
	}

	public String getImageName() {
		return image_name;
	}

	public void setImageName(String image_name) {
		this.image_name = image_name;
	}
}
