package com.cloudjay.cjay.dao;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "items", daoClass = UploadItemDaoImpl.class)
public class UploadItem {

	@DatabaseField(id = true)
	private String uuid;

	@DatabaseField
	private int uploadStatus;

	@DatabaseField
	private boolean noteStatus;

	@DatabaseField
	private String imageURL;

	@DatabaseField
	private String tmpImgUri;

	@DatabaseField
	private String jsonPostStr;

	public String getUUID() {
		return uuid;
	}

	public void setUUID(String uUID) {
		uuid = uUID;
	}

	public int getUploadStatus() {
		return uploadStatus;
	}

	public void setUploadStatus(int uploadStatus) {
		this.uploadStatus = uploadStatus;
	}

	public String getImageURL() {
		return imageURL;
	}

	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}

	public String getTmpImgUri() {
		return tmpImgUri;
	}

	public void setTmpImgUri(String tmpImgUri) {
		this.tmpImgUri = tmpImgUri;
	}

	public boolean isNoteStatus() {
		return noteStatus;
	}

	public void setNoteStatus(boolean noteStatus) {
		this.noteStatus = noteStatus;
	}

	public String getJsonPostStr() {
		return jsonPostStr;
	}

	public void setJsonPostStr(String jsonPostStr) {
		this.jsonPostStr = jsonPostStr;
	}
}
