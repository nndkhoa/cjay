package com.cloudjay.cjay.model;

import com.cloudjay.cjay.util.enums.ImageType;
import com.cloudjay.cjay.util.enums.UploadStatus;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

/* Gồm hình giám định và sửa chữa */
@Generated("org.jsonschema2pojo")
public class AuditImage {

	@Expose
	private long id;

	@Expose
	private long type;

	@Expose
	private String url;

	private String name;

	private int uploadStatus;

	public String getAuditImageUUID() {
		return auditImageUUID;
	}

	public void setAuditImageUUID(String auditImageUUID) {
		this.auditImageUUID = auditImageUUID;
	}

	@SerializedName("audit_image_uuid")
	@Expose
	private String auditImageUUID;

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

	public AuditImage withType(ImageType type) {
		this.type = type.value;
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

	public AuditImage withUUID(String s) {
		this.auditImageUUID = s;
		return this;
	}

	public AuditImage mergeAuditImage(AuditImage auditImageServer) {
		this.setId(auditImageServer.getId());
		return this;
	}
}