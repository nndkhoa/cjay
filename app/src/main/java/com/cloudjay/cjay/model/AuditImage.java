package com.cloudjay.cjay.model;

import com.google.gson.annotations.Expose;

import javax.annotation.Generated;

import io.realm.RealmObject;

@Generated("org.jsonschema2pojo")
public class AuditImage extends RealmObject {

	@Expose
	long id;

	@Expose
	long type;

	@Expose
	String url;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public AuditImage withId(long id) {
		this.id = id;
		return this;
	}

	public long getType() {
		return type;
	}

	public void setType(long type) {
		this.type = type;
	}

	public AuditImage withType(long type) {
		this.type = type;
		return this;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public AuditImage withUrl(String url) {
		this.url = url;
		return this;
	}

}