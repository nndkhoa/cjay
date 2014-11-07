package com.cloudjay.cjay.model;

import com.cloudjay.cjay.util.enums.UploadStatus;
import com.google.gson.annotations.Expose;

import javax.annotation.Generated;

/* Gồm hình nhập và hình xuất */
@Generated("org.jsonschema2pojo")
public class GateImage {

	@Expose
	private long id;
	@Expose
	private long type;

	@Expose
	private String url;

	@Expose
	private String name;

	@Expose
	private String gateImageUUID;

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

	public GateImage withUploadStatus(int status) {
		this.uploadStatus = status;
		return this;
	}

	public GateImage withUploadStatus(UploadStatus status) {
		this.uploadStatus = status.value;
		return this;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public GateImage withId(long id) {
		this.id = id;
		return this;
	}

	public long getType() {
		return type;
	}

	public void setType(long type) {
		this.type = type;
	}

	public GateImage withType(long type) {
		this.type = type;
		return this;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public GateImage withUrl(String url) {
		this.url = url;
		return this;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public GateImage withName(String name) {
		this.name = name;
		return this;
	}

	public void setGateImageUUID(String gateImageUUID) {
		this.gateImageUUID = gateImageUUID;
	}

	public String getGateImageUUID() {
		return this.gateImageUUID;
	}

	public GateImage withUUID(String gateImageUUID) {
		this.gateImageUUID = gateImageUUID;
		return this;
	}

	public GateImage mergeGateImage(GateImage gateImageServer) {
		this.setId(gateImageServer.getId());
		return this;
	}
}