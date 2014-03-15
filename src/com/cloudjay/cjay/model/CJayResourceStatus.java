package com.cloudjay.cjay.model;

import java.util.Date;

public class CJayResourceStatus {

	// "id": 1,
	// "type": 0,
	// "type_name": "RESOURCE DAMAGE",
	// "last_updated": "2013-12-23T12:22:30+08:00"

	private int id;
	private int type;
	private String type_name;
	private Date last_updated;

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

	public String getTypeName() {
		return type_name;
	}

	public void setTypeName(String type_name) {
		this.type_name = type_name;
	}

	public Date getLastUpdated() {
		return last_updated;
	}

	public void setLastUpdated(Date last_updated) {
		this.last_updated = last_updated;
	}

}
