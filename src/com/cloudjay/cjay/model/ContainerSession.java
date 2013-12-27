package com.cloudjay.cjay.model;

import java.util.Collection;

import org.parceler.Parcel;

import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.StringHelper;
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
@Parcel
public class ContainerSession {

	private static final String TAG = "ContainerSession";

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

	@DatabaseField(id = true, columnName = ID)
	int id;

	@DatabaseField(columnName = IMAGE_ID_PATH)
	String image_id_path;

	@DatabaseField(columnName = CHECK_IN_TIME, defaultValue = "")
	String check_in_time;

	@DatabaseField(columnName = CHECK_OUT_TIME, defaultValue = "")
	String check_out_time;

	@DatabaseField(columnName = FIELD_STATE)
	int mState;

	// container_id
	// operator_code
	@DatabaseField(canBeNull = true, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
	Container container;

	// gate_report_images where type = 0 (check in) | 1 (check out)
	// audit_report_items too
	@ForeignCollectionField(eager = true)
	Collection<CJayImage> cJayImages;

	@ForeignCollectionField(eager = true)
	Collection<Issue> issues;

	public int getUploadState() {
		return mState;
	}

	public void setIssues(Collection<Issue> issues) {
		this.issues = issues;
	}

	public Collection<Issue> getIssues() {
		return issues;
	}

	public void setCJayImages(Collection<CJayImage> cJayImages) {
		this.cJayImages = cJayImages;
	}

	public Collection<CJayImage> getCJayImages() {
		return cJayImages;
	}

	public String getOperatorName() {
		return getContainer().getOperator().getCode();
	}

	public String getContainerId() {
		return getContainer().getContainerId();
	}

	public String getFullContainerId() {
		return getContainer().getFullContainerId();
	}

	public String getCheckInTime() {
		return StringHelper.getRelativeDate(check_in_time.toString());
	}

	public String getCheckOutTime() {

		return StringHelper.getRelativeDate(check_out_time.toString());
	}

	public String getImageIdPath() {
		return image_id_path;
	}

	public void setImageIdPath(String image_id_path) {
		this.image_id_path = image_id_path;
	}

	public void setCheckInTime(String check_in_time) {

		this.check_in_time = check_in_time;
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
		Logger.Log(TAG, "CId: " + getFullContainerId() + " - OpCode: "
				+ getOperatorName() + " - TimeIn: " + getCheckInTime()
				+ " - TimeOut: " + getCheckOutTime());
	}

}
