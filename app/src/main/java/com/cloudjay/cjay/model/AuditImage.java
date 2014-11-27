package com.cloudjay.cjay.model;

import android.text.TextUtils;

import com.cloudjay.cjay.util.Utils;
import com.cloudjay.cjay.util.enums.ImageType;
import com.cloudjay.cjay.util.enums.UploadStatus;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.UUID;

import javax.annotation.Generated;

/* Gồm hình giám định và sửa chữa */
@Generated("org.jsonschema2pojo")
public class AuditImage implements Serializable{

	@Expose
	private long id;

	@Expose
	private long type;

	@Expose
	private String url;

	private String name;

	private int uploadStatus;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@SerializedName("audit_image_uuid")
	@Expose
	private String uuid;

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
		this.uuid = s;
		return this;
	}

	public AuditImage() {
		url = "";
		name = Utils.getImageNameFromUrl(url);
		id = 0;
		uploadStatus = 0;
		type = 0;
		uuid = UUID.randomUUID().toString();
	}

	public AuditImage mergeAuditImage(AuditImage auditImageServer) {
		this.setId(auditImageServer.getId());
		return this;
	}


	/**
	 * Nên so sánh object local --> server
	 *
	 * @param o
	 * @return
	 */
	@Override
	public boolean equals(Object o) {

		if (o instanceof AuditImage) {
			AuditImage tmp = (AuditImage) o;

			if (!TextUtils.isEmpty(name) && tmp.getUrl().contains(name)) {
				id = tmp.getId();
				return true;
			} else if (!TextUtils.isEmpty(tmp.getName()) && url.contains(tmp.getName())) {
                name = tmp.getName();
				return true;
			}
			return false;
		}

		return super.equals(o);
	}
}