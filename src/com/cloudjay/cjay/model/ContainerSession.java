package com.cloudjay.cjay.model;

import java.util.Collection;
import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.util.Logger;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * { "container_id": "abcdef12313", "image_id_path": "abcdef12313.jpg",
 * "operator_code": "BS", "check_in_time": "2013-12-20T20:14:47",
 * "check_out_time": null, "gate_report_images": [ { "type": 0, "image_name":
 * "asd" }, { "type": 0, "image_name": "asd21" }, { "type": 1, "image_name":
 * "1233313" } ] }
 * 
 * @author tieubao
 * 
 */

@DatabaseTable(tableName = "container_session", daoClass = ContainerSessionDaoImpl.class)
public class ContainerSession {

	private static final String CHECK_OUT_TIME = "check_out_time";
	private static final String CHECK_IN_TIME = "check_in_time";
	private static final String IMAGE_ID_PATH = "image_id_path";
	public static final String FIELD_STATE = "state";
	private static final String ID = "id";

	public static final int STATE_UPLOAD_COMPLETED = 5;
	public static final int STATE_UPLOAD_ERROR = 4;
	public static final int STATE_UPLOAD_IN_PROGRESS = 3;
	public static final int STATE_UPLOAD_WAITING = 2;
	public static final int STATE_SELECTED = 1;
	public static final int STATE_NONE = 0;

	public int getUploadState() {
		return mState;
	}

	@DatabaseField(columnName = FIELD_STATE)
	private int mState;

	private static final String TAG = "ContainerSession";

	@DatabaseField(id = true, columnName = ID)
	private int id;

	@DatabaseField(columnName = IMAGE_ID_PATH)
	private String image_id_path;

	@DatabaseField(columnName = CHECK_IN_TIME)
	private String check_in_time;

	@DatabaseField(columnName = CHECK_OUT_TIME)
	private String check_out_time;

	// container_id
	// operator_code
	@DatabaseField(canBeNull = true, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
	private Container container;

	// gate_report_images where type = 0 (check in) | 1 (check out)
	@ForeignCollectionField(eager = true)
	private Collection<CJayImage> cJayImages;

	public void setCJayImages(Collection<CJayImage> cJayImages) {
		this.cJayImages = cJayImages;
	}

	public Collection<CJayImage> getCJayImages() {
		return cJayImages;
	}

	//
	@ForeignCollectionField(eager = true)
	private Collection<Issue> issues;

	public void setIssues(Collection<Issue> issues) {
		this.issues = issues;
	}

	public Collection<Issue> getIssues() {
		return issues;
	}

	public String getOperatorName() {
		return getContainer().getOperator().getName();
	}

	public String getContainerId() {
		return getContainer().getContainerId();
	}

	// public String getCheckInTime() {
	// return StringHelper.getRelativeDate(check_in_time.toString());
	// }
	//
	// public String getCheckOutTime() {
	// return StringHelper.getRelativeDate(check_out_time.toString());
	// }

	public String getImageIdPath() {
		return image_id_path;
	}

	public void setImageIdPath(String image_id_path) {
		this.image_id_path = image_id_path;
	}

	public String getCheckInTime() {
		return check_in_time;
	}

	public void setCheckInTime(String check_in_time) {

		this.check_in_time = check_in_time;
	}

	public String getCheckOutTime() {
		return check_out_time;
	}

	public void setCheckOutTime(String check_out_time) {
		this.check_out_time = check_out_time;
	}

	public Container getContainer() {
		return container;
	}

	public void setContainer(Container container) {
		this.container = container;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void printMe() {
		Logger.Log(TAG, "CId: " + getContainerId() + " - OpCode: "
				+ getOperatorName() + " - TimeIn: " + getCheckInTime()
				+ " - TimeOut: " + getCheckOutTime());
	}
}
