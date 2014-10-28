package com.cloudjay.cjay.model;

import com.cloudjay.cjay.util.enums.UploadStatus;
import com.google.gson.annotations.Expose;

import java.io.Serializable;

import javax.annotation.Generated;

/* Gồm hình giám định và sửa chữa */
@Generated("org.jsonschema2pojo")
public class AuditImage implements Serializable {

	@Expose
	private long id;

	@Expose
	private long type;

	@Expose
	private String url;

	private String name;

	private int uploadStatus;

	public int getUploadStatus() {
		return uploadStatus;
	}

	public void setUploadStatus(int status) {
		this.uploadStatus = status;
	}

	public void setUploadStatus(UploadStatus status) {
		this.uploadStatus = status.value;
	}

	public AuditImage withUploadStatus(int status) {
		this.uploadStatus = status;
		return this;
	}

	public AuditImage withUploadStatus(UploadStatus status) {
		this.uploadStatus = status.value;
		return this;
	}

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public AuditImage withName(String name) {
		this.name = name;
		return this;
	}

}