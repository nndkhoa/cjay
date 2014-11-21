package com.cloudjay.cjay.model;

import android.text.TextUtils;

import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.Utils;
import com.cloudjay.cjay.util.enums.UploadStatus;
import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.UUID;

import javax.annotation.Generated;

/* Gồm hình nhập và hình xuất */
@Generated("org.jsonschema2pojo")
public class GateImage implements Serializable{

	@Expose
	private long id;
	@Expose
	private long type;

	@Expose
	private String url;

	@Expose
	private String name;

	@Expose
	private String uuid;

	private int uploadStatus;

	public int getUploadStatus() {
		return uploadStatus;
	}

	public void setUploadStatus(int status) {

		this.uploadStatus = status;
	}

	public void setUploadStatus(UploadStatus status) {
		Logger.Log("Change image " + uuid + "status to " + status.name());
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

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getUuid() {
		return this.uuid;
	}

	public GateImage withUuid(String newUuid) {
		this.uuid = newUuid;
		return this;
	}

	public GateImage mergeGateImage(GateImage gateImageServer) {
		this.setId(gateImageServer.getId());
		return this;
	}

	public GateImage() {

		url = "";
		name = Utils.getImageNameFromUrl(url);
		id = 0;
		uploadStatus = 0;
		type = 0;
		uuid = UUID.randomUUID().toString();
	}

	/**
	 * Nên so sánh object local --> server
	 *
	 * @param o
	 * @return
	 */
	@Override
	public boolean equals(Object o) {

		if (o instanceof GateImage) {
			GateImage tmp = (GateImage) o;

			if (!TextUtils.isEmpty(name) && tmp.getUrl().contains(name)) {
				id = tmp.getId();

				return true;
			} else if (!TextUtils.isEmpty(tmp.getName()) && url.contains(tmp.getName())) {

				return true;
			}
			return false;
		}

		return super.equals(o);
	}
}