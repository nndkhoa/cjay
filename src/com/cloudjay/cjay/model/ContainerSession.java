package com.cloudjay.cjay.model;

import java.sql.SQLException;
import java.util.Collection;
import java.util.UUID;

import android.content.Context;
import android.text.TextUtils;

import com.cloudjay.cjay.dao.ContainerDaoImpl;
import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.dao.DepotDaoImpl;
import com.cloudjay.cjay.dao.OperatorDaoImpl;
import com.cloudjay.cjay.events.UploadStateChangedEvent;
import com.cloudjay.cjay.network.CJayClient;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.QueryHelper;
import com.cloudjay.cjay.util.StringHelper;
import com.cloudjay.cjay.util.UploadState;
import com.cloudjay.cjay.util.UploadType;
import com.cloudjay.cjay.util.Utils;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import de.greenrobot.event.EventBus;

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

	public static final String FIELD_ID = "id";
	public static final String FIELD_UUID = "_id";

	public static final String FIELD_STATE = "state"; // 1
	public static final String FIELD_UPLOAD_TYPE = "upload_type";
	public static final String FIELD_SERVER_STATE = "server_state";

	public static final String FIELD_UPLOAD_CONFIRMATION = "upload_confirmation"; // 1
	public static final String FIELD_CLEARED = "cleared";
	public static final String FIELD_LOCAL = "on_local"; // 1
	public static final String FIELD_FIXED = "fixed";
	public static final String FIELD_EXPORT = "export";
	public static final String FIELD_AVAILABLE = "is_available";

	// public static final String FIELD_IS_TEMP = "is_temp";
	// @DatabaseField(columnName = FIELD_IS_TEMP, defaultValue = "0")
	// private boolean is_temp;
	//
	// public boolean isTemporary() {
	// return is_temp;
	// }
	//
	// public void setTemporary(boolean is_temp) {
	// this.is_temp = is_temp;
	// }

	public static final String FIELD_CHECK_OUT_TIME = "check_out_time";
	public static final String FIELD_CHECK_IN_TIME = "check_in_time";
	public static final String FIELD_IMAGE_ID_PATH = "image_id_path";

	@DatabaseField(columnName = FIELD_ID, index = true)
	int id;

	@DatabaseField(columnName = FIELD_UPLOAD_TYPE, defaultValue = "0")
	int upload_type;

	@DatabaseField(columnName = FIELD_UUID, id = true)
	String uuid;

	// Use to mark: cleared from upload fragment
	@DatabaseField(columnName = FIELD_CLEARED, defaultValue = "false")
	boolean cleared;

	@DatabaseField(columnName = FIELD_LOCAL, defaultValue = "false")
	boolean onLocal;

	@DatabaseField(columnName = FIELD_IMAGE_ID_PATH, defaultValue = "")
	String image_id_path;

	@DatabaseField(columnName = FIELD_CHECK_IN_TIME, defaultValue = "")
	String check_in_time;

	@DatabaseField(columnName = FIELD_CHECK_OUT_TIME, defaultValue = "", index = true)
	String check_out_time;

	@DatabaseField(columnName = FIELD_STATE, defaultValue = "0", index = true)
	int mState;

	// remark = true -> container available
	@DatabaseField(columnName = FIELD_AVAILABLE, defaultValue = "false", index = true)
	boolean mAvailable;

	// Use to mark from pending --> fix
	@DatabaseField(columnName = FIELD_FIXED, defaultValue = "false")
	boolean fixed;

	// Use to mark exported
	@DatabaseField(columnName = FIELD_EXPORT, defaultValue = "false")
	boolean export;

	@DatabaseField(columnName = FIELD_UPLOAD_CONFIRMATION, defaultValue = "false", index = true)
	boolean uploadConfirmation;

	@DatabaseField(columnName = FIELD_SERVER_STATE, defaultValue = "6")
	private int serverState;

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

	private int mProgress;

	public ContainerSession() {
		cleared = false;
		onLocal = false;
		fixed = false;
		uploadConfirmation = false;
		mState = 0;
	}

	public ContainerSession(Context ctx, String containerId, String operatorCode, String checkInTime, String depotCode) {

		IDatabaseManager databaseManager = CJayClient.getInstance().getDatabaseManager();

		DepotDaoImpl depotDaoImpl;
		try {
			depotDaoImpl = databaseManager.getHelper(ctx).getDepotDaoImpl();
			OperatorDaoImpl operatorDaoImpl = databaseManager.getHelper(ctx).getOperatorDaoImpl();
			ContainerDaoImpl containerDaoImpl = databaseManager.getHelper(ctx).getContainerDaoImpl();

			// Create operator object if needed
			Operator operator = null;
			if (!TextUtils.isEmpty(operatorCode)) {
				operator = operatorDaoImpl.findOperator(operatorCode);
				if (null == operator) {
					operator = new Operator();
					operator.setCode(operatorCode);
					operator.setName(operatorCode);
					operatorDaoImpl.addOperator(operator);
				}
			}

			// Create `depot` object if needed
			Depot depot = depotDaoImpl.findDepot(depotCode);
			if (null == depot) {
				depot = new Depot();
				depot.setDepotCode(depotCode);
				depot.setDepotName(depotCode);
				depotDaoImpl.addDepot(depot);
			}

			// Create `container` object if needed
			Container container = containerDaoImpl.findContainer(containerId);
			if (null == container) {
				container = new Container();
				container.setContainerId(containerId);

				if (null != operator) {
					container.setOperator(operator);
				}

				if (null != depot) {
					container.setDepot(depot);
				}

				containerDaoImpl.addContainer(container);
			}

			// Create `container session` object
			// UUID is primary key
			String uuid = UUID.randomUUID().toString();
			setCheckInTime(checkInTime);
			setUuid(uuid);

			if (null != container) {
				setContainer(container);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public String getCheckInTime() {
		return StringHelper.getRelativeDate(CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE, check_in_time.toString());
	}

	public String getCheckOutTime() {

		return StringHelper.getRelativeDate(CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE,
											Utils.stripNull(check_out_time));
	}

	public Collection<CJayImage> getCJayImages() {
		return cJayImages;
	}

	public Container getContainer() {
		return container;
	}

	public String getContainerId() {
		if (getContainer() != null) return getContainer().getContainerId();
		return null;
	}

	public String getFullContainerId() {
		if (getContainer() != null) return getContainer().getFullContainerId();
		return null;
	}

	public int getId() {
		return id;
	}

	public String getImageIdPath() {
		return image_id_path;
	}

	public String getIssueCount() {
		return String.valueOf(getIssues().size());
	}

	public Collection<Issue> getIssues() {
		return issues;
	}

	public String getOperatorCode() {
		if (getContainer() != null && getContainer().getOperator() != null)
			return getContainer().getOperator().getCode();
		return null;
	}

	public int getOperatorId() {
		if (getContainer() != null && getContainer().getOperator() != null)
			return getContainer().getOperator().getId();
		return 0;
	}

	public String getOperatorName() {
		if (getContainer() != null && getContainer().getOperator() != null)
			return getContainer().getOperator().getName();
		return null;
	}

	public String getRawCheckInTime() {
		return check_in_time;
	}

	public String getRawCheckOutTime() {
		return check_out_time;
	}

	public int getUploadProgress() {
		return mProgress;
	}

	public int getUploadState() {
		return mState;
	}

	public int getUploadType() {
		return upload_type;
	}

	public String getUuid() {
		return uuid;
	}

	public boolean hasUploadConfirmed() {
		return uploadConfirmation;
	}

	public boolean isCleared() {
		return cleared;
	}

	public boolean isExport() {
		return export;
	}

	public boolean isFixed() {
		return fixed;
	}

	public boolean isOnLocal() {
		return onLocal;
	}

	public void setAvailable(boolean available) {
		mAvailable = available;
	}
	
	public boolean isAvailable() {
		return mAvailable;
	}

	// 0 issue --> Failed
	// > 0 issues:
	// image without issue <= 1 && all issue is valid --> OK
	public boolean isValidForUpload(Context ctx, int imageType) {

		if (issues.isEmpty()) return false;

		switch (imageType) {
			case CJayImage.TYPE_REPORT:
				// check if all REPORT image assigned to issues
				int imageWithoutIssueCount = 0;

				// count images without issues
				for (CJayImage cJayImage : cJayImages) {
					if (cJayImage.getType() == CJayImage.TYPE_REPORT && cJayImage.getIssue() == null) {
						imageWithoutIssueCount++;
						if (imageWithoutIssueCount > 1) return false;
					}
				}

				// count invalid issues
				for (Issue issue : issues) {
					if (!issue.isValid()) return false;
				}

				return true;

			case CJayImage.TYPE_REPAIRED:

				// check if all issues have REPAIRED images
				boolean issueHasNoImage;
				for (Issue issue : issues) {
					issueHasNoImage = true;

					for (CJayImage cJayImage : issue.getCJayImages()) {
						if (cJayImage.getType() == CJayImage.TYPE_REPAIRED) {
							issueHasNoImage = false;
							break;
						}
					}

					if (issueHasNoImage) return false;
				}

				return true;

				// String sql = "SELECT COUNT(img.uuid) AS img_count FROM container_session cs "
				// + "INNER JOIN issue i ON cs._id = i.containerSession_id "
				// + "LEFT JOIN cjay_image img ON i._id = img.issue_id and img.type = 2 "
				// + "WHERE cs._id LIKE ? GROUP BY i._id";
				// SQLiteDatabase db = DataCenter.getDatabaseHelper(ctx.getApplicationContext()).getReadableDatabase();
				// Cursor cursor = db.rawQuery(sql, new String[] { uuid + "%" });
				// if (cursor.moveToFirst()) {
				// do {
				// if (cursor.getInt(cursor.getColumnIndexOrThrow("img_count")) == 0) {
				// return false;
				// }
				// } while (cursor.moveToNext());
				// }
				//
				// return true;

			default:
				return true;
		}
	}

	public void setCheckInTime(String check_in_time) {

		this.check_in_time = check_in_time;
	}

	public void setCheckOutTime(String check_out_time) {
		this.check_out_time = check_out_time;
	}

	public void setCJayImages(Collection<CJayImage> cJayImages) {
		this.cJayImages = cJayImages;
	}

	public void setCleared(boolean cleared) {
		this.cleared = cleared;
	}

	public void setContainer(Container container) {
		this.container = container;
	}

	public void setExport(boolean export) {
		this.export = export;
	}

	public void setFixed(boolean fixed) {
		this.fixed = fixed;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setImageIdPath(String image_id_path) {
		this.image_id_path = image_id_path;
	}

	public void setIssues(Collection<Issue> issues) {
		this.issues = issues;
	}

	public void setOnLocal(boolean onLocal) {
		this.onLocal = onLocal;
	}

	public void setUploadConfirmation(boolean uploadConfirmation) {
		this.uploadConfirmation = uploadConfirmation;
	}

	public void setUploadState(UploadState state) {

		Logger.w("Change upload state from " + UploadState.values()[mState].name() + " to " + state.name());
		int val = state.getValue();

		if (mState != val) {
			mState = val;

			switch (state) {
				case WAITING:
					mProgress = -1;
					break;

				case ERROR:
				case COMPLETED:
				default:
					break;
			}

			EventBus.getDefault().post(new UploadStateChangedEvent(this));
		}
	}

	public void setUploadType(int upload_type) {
		this.upload_type = upload_type;
	}

	public void setUploadType(UploadType type) {
		this.upload_type = type.getValue();
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public int getServerState() {
		return serverState;
	}

	public void setServerState(int serverState) {
		this.serverState = serverState;
	}

	public void updateField(Context ctx, String field, String value) {
		QueryHelper.update(ctx, "container_session", field, value, ContainerSession.FIELD_UUID + " = " + Utils.sqlString(uuid));
	}
}
