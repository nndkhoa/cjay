package com.cloudjay.cjay.model;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
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
import com.cloudjay.cjay.util.DatabaseHelper;
import com.cloudjay.cjay.util.StringHelper;
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

	public static final String FIELD_CHECK_OUT_TIME = "check_out_time";
	public static final String FIELD_CHECK_IN_TIME = "check_in_time";
	public static final String FIELD_IMAGE_ID_PATH = "image_id_path";
	public static final String FIELD_STATE = "state";
	public static final String FIELD_ID = "id";

	// _id for cursor loader usage
	public static final String FIELD_UUID = "_id";
	public static final String FIELD_UPLOAD_CONFIRMATION = "upload_confirmation";
	public static final String FIELD_CLEARED = "cleared";
	public static final String FIELD_LOCAL = "on_local";
	public static final String FIELD_FIXED = "fixed";
	public static final String FIELD_EXPORT = "export";

	public static final int STATE_UPLOAD_COMPLETED = 4;
	public static final int STATE_UPLOAD_ERROR = 3;
	public static final int STATE_UPLOAD_IN_PROGRESS = 2;
	public static final int STATE_UPLOAD_WAITING = 1;
	public static final int STATE_NONE = 0;

	static final int MINI_THUMBNAIL_SIZE = 300;
	static final int MICRO_THUMBNAIL_SIZE = 96;

	@DatabaseField(columnName = FIELD_ID, index = true)
	int id;

	@DatabaseField(columnName = FIELD_UUID, id = true)
	String uuid;

	@DatabaseField(columnName = FIELD_CLEARED, defaultValue = "false")
	private boolean cleared;

	@DatabaseField(columnName = FIELD_LOCAL, defaultValue = "false")
	private boolean onLocal;

	@DatabaseField(columnName = FIELD_IMAGE_ID_PATH, defaultValue = "")
	String image_id_path;

	@DatabaseField(columnName = FIELD_CHECK_IN_TIME, defaultValue = "")
	String check_in_time;

	@DatabaseField(columnName = FIELD_CHECK_OUT_TIME, defaultValue = "", index = true)
	String check_out_time;

	@DatabaseField(columnName = FIELD_STATE, defaultValue = "0", index = true)
	int mState;

	@DatabaseField(columnName = FIELD_FIXED, defaultValue = "false")
	boolean fixed;

	@DatabaseField(columnName = FIELD_EXPORT, defaultValue = "false")
	boolean export;

	@DatabaseField(columnName = FIELD_UPLOAD_CONFIRMATION, defaultValue = "false", index = true)
	private boolean uploadConfirmation;

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

	public ContainerSession(Context ctx, String containerId,
			String operatorCode, String checkInTime, String depotCode) {

		IDatabaseManager databaseManager = CJayClient.getInstance()
				.getDatabaseManager();

		DepotDaoImpl depotDaoImpl;
		try {
			depotDaoImpl = databaseManager.getHelper(ctx).getDepotDaoImpl();
			OperatorDaoImpl operatorDaoImpl = databaseManager.getHelper(ctx)
					.getOperatorDaoImpl();
			ContainerDaoImpl containerDaoImpl = databaseManager.getHelper(ctx)
					.getContainerDaoImpl();

			Operator operator = null;
			if (!TextUtils.isEmpty(operatorCode)) {
				List<Operator> listOperators = operatorDaoImpl.queryForEq(
						Operator.FIELD_CODE, operatorCode);
				if (listOperators.isEmpty()) {
					operator = new Operator();
					operator.setCode(operatorCode);
					operator.setName(operatorCode);
					operatorDaoImpl.addOperator(operator);
				} else {
					operator = listOperators.get(0);
				}
			}

			// Create `depot` object if needed
			Depot depot = null;
			List<Depot> listDepots = depotDaoImpl.queryForEq(Depot.DEPOT_CODE,
					depotCode);
			if (listDepots.isEmpty()) {
				depot = new Depot();
				depot.setDepotCode(depotCode);
				depot.setDepotName(depotCode);
				depotDaoImpl.addDepot(depot);
			} else {
				depot = listDepots.get(0);
			}

			// Create `container` object if needed
			Container container = null;
			List<Container> listContainers = containerDaoImpl.queryForEq(
					Container.CONTAINER_ID, containerId);
			if (listContainers.isEmpty()) {
				container = new Container();
				container.setContainerId(containerId);
				if (null != operator)
					container.setOperator(operator);

				if (null != depot)
					container.setDepot(depot);

				containerDaoImpl.addContainer(container);
			} else {
				container = listContainers.get(0);
			}

			// Create `container session` object
			// UUID is primary key
			String uuid = UUID.randomUUID().toString();
			this.setCheckInTime(checkInTime);
			this.setUuid(uuid);

			if (null != container)
				this.setContainer(container);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public void setFixed(boolean fixed) {
		this.fixed = fixed;
	}

	public boolean isFixed() {
		return fixed;
	}

	public int getUploadState() {
		return mState;
	}

	public void setUploadState(int state) {

		if (mState != state) {
			mState = state;

			switch (state) {
			case STATE_UPLOAD_ERROR:
				break;

			case STATE_UPLOAD_COMPLETED:
				// Set Container Big Picture Display = null

				break;
			case STATE_UPLOAD_WAITING:
				mProgress = -1;
				break;
			}

			notifyUploadStateListener();
		}
	}

	public void setUploadProgress(int progress) {
		if (progress != mProgress) {
			mProgress = progress;
			notifyUploadStateListener();
		}
	}

	public int getUploadProgress() {
		return mProgress;
	}

	public void setIssues(Collection<Issue> issues) {
		this.issues = issues;
	}

	public Collection<Issue> getIssues() {
		return issues;
	}

	public String getIssueCount() {
		return String.valueOf(getIssues().size());
	}

	public void setCJayImages(Collection<CJayImage> cJayImages) {
		this.cJayImages = cJayImages;
	}

	public Collection<CJayImage> getCJayImages() {
		return cJayImages;
	}

	public String getOperatorCode() {
		if (getContainer() != null && getContainer().getOperator() != null) {
			return getContainer().getOperator().getCode();
		}
		return null;
	}

	public int getOperatorId() {
		if (getContainer() != null && getContainer().getOperator() != null) {
			return getContainer().getOperator().getId();
		}
		return 0;
	}

	public String getOperatorName() {
		if (getContainer() != null && getContainer().getOperator() != null) {
			return getContainer().getOperator().getName();
		}
		return null;
	}

	public String getContainerId() {
		if (getContainer() != null) {
			return getContainer().getContainerId();
		}
		return null;
	}

	public String getFullContainerId() {
		if (getContainer() != null) {
			return getContainer().getFullContainerId();
		}
		return null;
	}

	public String getCheckInTime() {
		return StringHelper.getRelativeDate(
				CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE,
				check_in_time.toString());
	}

	public String getRawCheckInTime() {
		return check_in_time;
	}

	public String getFormattedCheckInTime() {
		return StringHelper.getTimestamp(CJayConstant.CJAY_DATETIME_FORMAT,
				CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE, check_in_time);
	}

	public String getFormattedCheckOutTime() {
		return StringHelper.getTimestamp(CJayConstant.CJAY_DATETIME_FORMAT,
				CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE, check_out_time);

	}

	public String getCheckOutTime() {

		return StringHelper.getRelativeDate(
				CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE,
				check_out_time.toString());
	}

	public String getRawCheckOutTime() {
		return check_out_time;
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

	public boolean hasUploadConfirmed() {
		return uploadConfirmation;
	}

	public void setUploadConfirmation(boolean uploadConfirmation) {
		this.uploadConfirmation = uploadConfirmation;
	}

	private void notifyUploadStateListener() {
		EventBus.getDefault().post(new UploadStateChangedEvent(this));
	}

	public boolean isCleared() {
		return cleared;
	}

	public void setCleared(boolean cleared) {
		this.cleared = cleared;
	}

	public boolean isOnLocal() {
		return onLocal;
	}

	public void setOnLocal(boolean onLocal) {
		this.onLocal = onLocal;
	}

	// TODO: need to refactor
	public static ContainerSession editContainerSession(Context ctx,
			ContainerSession containerSession, String containerId,
			String operatorCode) throws SQLException {
		if (containerSession.getContainerId().equals(containerId)
				&& containerSession.getOperatorCode().equals(operatorCode)) {
			// do nothing
		} else {
			DatabaseHelper databaseHelper = CJayClient.getInstance()
					.getDatabaseManager().getHelper(ctx);
			OperatorDaoImpl operatorDaoImpl = databaseHelper
					.getOperatorDaoImpl();
			ContainerDaoImpl containerDaoImpl = databaseHelper
					.getContainerDaoImpl();
			ContainerSessionDaoImpl containerSessionDaoImpl = databaseHelper
					.getContainerSessionDaoImpl();

			// find operator
			Operator operator = operatorDaoImpl.findOperator(operatorCode);

			// update container details
			Container container = containerSession.getContainer();
			container.setContainerId(containerId);
			container.setOperator(operator);

			// update database
			containerDaoImpl.update(container);
			containerSessionDaoImpl.update(containerSession);
		}

		return containerSession;
	}

	// 0 issue --> Failed
	// > 0 issues:
	// image without issue <= 1 && all issue is valid --> OK
	public boolean isValidForUpload(int imageType) {

		if (issues.isEmpty()) {
			return false;
		}

		switch (imageType) {
		case CJayImage.TYPE_REPORT:
			// check if all REPORT image assigned to issues
			int imageWithoutIssueCount = 0;

			// count images without issues
			for (CJayImage cJayImage : cJayImages) {
				if (cJayImage.getType() == CJayImage.TYPE_REPORT
						&& cJayImage.getIssue() == null) {
					imageWithoutIssueCount++;
					if (imageWithoutIssueCount > 1) {
						return false;
					}
				}
			}

			// count invalid issues
			for (Issue issue : issues) {
				if (!issue.isValid()) {
					return false;
				}
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
				
				if (issueHasNoImage) {
					return false;
				}
			}
			
			return true;
			
		default:
			return true;
		}
	}

	public boolean isExport() {
		return export;
	}

	public void setExport(boolean export) {
		this.export = export;
	}
}
