package com.cloudjay.cjay.model;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.cloudjay.cjay.dao.ContainerDaoImpl;
import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.dao.DepotDaoImpl;
import com.cloudjay.cjay.dao.OperatorDaoImpl;
import com.cloudjay.cjay.network.CJayClient;
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

@SuppressLint("ParcelCreator")
@DatabaseTable(tableName = "container_session", daoClass = ContainerSessionDaoImpl.class)
public class ContainerSession implements Parcelable {

	private static final String TAG = "ContainerSession";

	private static final String CHECK_OUT_TIME = "check_out_time";
	private static final String CHECK_IN_TIME = "check_in_time";
	private static final String IMAGE_ID_PATH = "image_id_path";
	public static final String FIELD_STATE = "state";
	private static final String ID = "id";
	private static final String FIELD_UUID = "uuid";

	public static final int STATE_UPLOAD_COMPLETED = 4;
	public static final int STATE_UPLOAD_ERROR = 3;
	public static final int STATE_UPLOAD_IN_PROGRESS = 2;
	public static final int STATE_UPLOAD_WAITING = 1;
	public static final int STATE_NONE = 0;

	@DatabaseField(columnName = ID)
	int id;

	@DatabaseField(columnName = FIELD_UUID, id = true)
	String uuid;

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

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public int getUploadState() {
		return mState;
	}

	public void setUploadState(int state) {
		mState = state;
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

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeString(image_id_path);
		dest.writeString(check_in_time);
		dest.writeString(check_out_time);
		dest.writeInt(mState);

		try {
			dest.writeParcelable(container, flags);
		} catch (Exception e) {
			e.printStackTrace();
		}

		parcelCollection(dest, cJayImages);
		parcelCollection(dest, issues);
	}

	private void readFromParcel(Parcel in) {

		this.id = in.readInt();
		this.image_id_path = in.readString();
		this.check_in_time = in.readString();
		this.check_out_time = in.readString();
		this.mState = in.readInt();
		in.readParcelable(Container.class.getClassLoader());
		this.cJayImages = unparcelCollection(in, CJayImage.CREATOR);
		this.issues = unparcelCollection(in, Issue.CREATOR);
	}

	public static final Parcelable.Creator<ContainerSession> CREATOR = new Parcelable.Creator<ContainerSession>() {

		public ContainerSession createFromParcel(Parcel source) {
			return new ContainerSession(source);
		}

		public ContainerSession[] newArray(int size) {
			return new ContainerSession[size];
		}
	};

	public ContainerSession(Parcel in) {
		readFromParcel(in);
	}

	public ContainerSession() {
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
			List<Operator> listOperators = operatorDaoImpl.queryForEq(
					Operator.CODE, operatorCode);

			if (listOperators.isEmpty()) {
				operator = new Operator();
				operator.setCode(operatorCode);
				operator.setName(operatorCode);
				operatorDaoImpl.addOperator(operator);
			} else {
				operator = listOperators.get(0);
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

	<T extends Parcelable> void parcelCollection(final Parcel out,
			final Collection<T> collection) {
		if (collection != null) {
			out.writeInt(collection.size());
			out.writeTypedList(new ArrayList<T>(collection));
		} else {
			out.writeInt(-1);
		}
	}

	<T extends Parcelable> Collection<T> unparcelCollection(final Parcel in,
			final Creator<T> creator) {
		final int size = in.readInt();

		if (size >= 0) {
			final List<T> list = new ArrayList<T>(size);
			in.readTypedList(list, creator);
			return list;
		} else {
			return null;
		}
	}

}
