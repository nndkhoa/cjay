package com.cloudjay.cjay.model;

import java.util.UUID;

import com.cloudjay.cjay.dao.CJayImageDaoImpl;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "cjay_image", daoClass = CJayImageDaoImpl.class)
public class CJayImage {

	public static final int STATE_UPLOAD_COMPLETED = 4;
	public static final int STATE_UPLOAD_ERROR = 3;
	public static final int STATE_UPLOAD_IN_PROGRESS = 2;
	public static final int STATE_UPLOAD_WAITING = 1;
	public static final int STATE_NONE = 0;

	public static final int TYPE_IMPORT = 0;
	public static final int TYPE_EXPORT = 1;
	public static final int TYPE_REPORT = 2;
	public static final int TYPE_REPAIRED = 3;

	public static final String ID = "id";
	public static final String FIELD_IMAGE_NAME = "image_name";
	public static final String FIELD_TYPE = "type";
	public static final String FIELD_TIME_POSTED = "time_posted";
	public static final String FIELD_STATE = "state";
	public static final String FIELD_URI = "_id";
	public static final String FIELD_UUID = "uuid";

	@DatabaseField(columnName = ID, defaultValue = "0")
	private int id;

	@DatabaseField(columnName = FIELD_IMAGE_NAME)
	String image_name;

	@DatabaseField(columnName = FIELD_TIME_POSTED)
	String time_posted;

	@DatabaseField(columnName = FIELD_UUID)
	String uuid;

	/**
	 * TYPE include: in | out | issue | repaired
	 */
	@DatabaseField(columnName = FIELD_TYPE)
	int type;

	@DatabaseField(columnName = FIELD_STATE, index = true)
	int mState;

	@DatabaseField(columnName = FIELD_URI, id = true)
	String mUri;

	@DatabaseField(canBeNull = true, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
	ContainerSession containerSession;

	@DatabaseField(canBeNull = true, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
	Issue issue;

	public CJayImage() {

	}

	public CJayImage(int id, int type, String image_name) {
		this.id = id;
		this.type = type;
		this.image_name = image_name;
		uuid = UUID.randomUUID().toString();
		time_posted = "";
		mUri = image_name;
	}

	public CJayImage(int id, int type, String created_at, String image_name) {
		this.id = id;
		this.type = type;
		this.image_name = image_name;
		time_posted = created_at;
		uuid = UUID.randomUUID().toString();
		mUri = image_name;
	}

	public ContainerSession getContainerSession() {
		return containerSession;
	}

	public int getId() {
		return id;
	}

	public String getImageName() {
		return image_name;
	}

	public Issue getIssue() {
		return issue;
	}

	public String getIssueComponentCode() {
		if (issue != null) return issue.getComponentCodeString();
		return null;
	}

	public String getIssueDamageCode() {
		if (issue != null) return issue.getDamageCodeString();
		return null;
	}

	public String getIssueHeight() {
		if (issue != null) return String.valueOf(issue.getHeight());
		return null;
	}

	public String getIssueLength() {
		if (issue != null) return String.valueOf(issue.getLength());
		return null;
	}

	public String getIssueLocationCode() {
		if (issue != null) return issue.getLocationCode();
		return null;
	}

	public String getIssueQuantity() {
		if (issue != null) return String.valueOf(issue.getQuantity());
		return null;
	}

	public String getIssueRepairCode() {
		if (issue != null) return issue.getRepairCodeString();
		return null;
	}

	public String getTimePosted() {
		return time_posted;
	}

	public int getType() {
		return type;
	}

	public int getUploadState() {
		return mState;
	}

	public String getUri() {
		return mUri;
	}

	public String getUuid() {
		return uuid;
	}

	public void setContainerSession(ContainerSession containerSession) {
		this.containerSession = containerSession;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setImageName(String image_name) {
		this.image_name = image_name;
	}

	public void setIssue(Issue issue) {
		this.issue = issue;
	}

	public void setTimePosted(String time_posted) {
		this.time_posted = time_posted;
	}

	public void setType(int type) {
		this.type = type;
	}

	public void setUploadState(int state) {
		mState = state;
	}

	public void setUri(String uri) {
		mUri = uri;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
}
