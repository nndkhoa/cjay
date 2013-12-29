package com.cloudjay.cjay.model;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore.Images.Thumbnails;
import android.text.TextUtils;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.dao.ContainerDaoImpl;
import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.dao.DepotDaoImpl;
import com.cloudjay.cjay.dao.OperatorDaoImpl;
import com.cloudjay.cjay.events.UploadStateChangedEvent;
import com.cloudjay.cjay.events.UploadsModifiedEvent;
import com.cloudjay.cjay.network.CJayClient;
import com.cloudjay.cjay.util.Flags;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.StringHelper;
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
	private static final String FIELD_UPLOAD_CONFIRMATION = "upload_confirmation";

	public static final int STATE_UPLOAD_COMPLETED = 4;
	public static final int STATE_UPLOAD_ERROR = 3;
	public static final int STATE_UPLOAD_IN_PROGRESS = 2;
	public static final int STATE_UPLOAD_WAITING = 1;
	public static final int STATE_NONE = 0;

	static final int MINI_THUMBNAIL_SIZE = 300;
	static final int MICRO_THUMBNAIL_SIZE = 96;

	private Uri mFullUri;

	@DatabaseField(columnName = ID)
	int id;

	@DatabaseField(columnName = FIELD_UUID, id = true)
	String uuid;

	@DatabaseField(columnName = IMAGE_ID_PATH, defaultValue = "")
	String image_id_path;

	@DatabaseField(columnName = CHECK_IN_TIME, defaultValue = "")
	String check_in_time;

	@DatabaseField(columnName = CHECK_OUT_TIME, defaultValue = "")
	String check_out_time;

	@DatabaseField(columnName = FIELD_STATE, defaultValue = "0")
	int mState;

	@DatabaseField(columnName = FIELD_UPLOAD_CONFIRMATION, defaultValue = "false")
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
	private Bitmap mBigPictureNotificationBmp;

	public Bitmap getBigPictureNotificationBmp() {
		return mBigPictureNotificationBmp;
	}

	public void setBigPictureNotificationBmp(Context context,
			Bitmap bigPictureNotificationBmp) {
		if (null == bigPictureNotificationBmp) {
			mBigPictureNotificationBmp = BitmapFactory.decodeResource(
					context.getResources(), R.drawable.ic_logo);
		} else {
			mBigPictureNotificationBmp = bigPictureNotificationBmp;
		}
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

		if (mState != state) {
			mState = state;

			switch (state) {
			case STATE_UPLOAD_ERROR:
			case STATE_UPLOAD_COMPLETED:
				mBigPictureNotificationBmp = null;
				EventBus.getDefault().post(new UploadsModifiedEvent());
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

	public boolean hasUploadConfirmed() {
		return uploadConfirmation;
	}

	public void setUploadConfirmation(boolean uploadConfirmation) {
		this.uploadConfirmation = uploadConfirmation;
	}

	private void notifyUploadStateListener() {
		EventBus.getDefault().post(new UploadStateChangedEvent(this));
	}

	public Bitmap getThumbnailImage(Context context) {
		if (ContentResolver.SCHEME_CONTENT.equals(getOriginalPhotoUri()
				.getScheme())) {
			return getThumbnailImageFromMediaStore(context);
		}

		final Resources res = context.getResources();
		int size = res.getBoolean(R.bool.load_mini_thumbnails) ? MINI_THUMBNAIL_SIZE
				: MICRO_THUMBNAIL_SIZE;
		if (size == MINI_THUMBNAIL_SIZE
				&& res.getBoolean(R.bool.sample_mini_thumbnails)) {
			size /= 2;
		}

		try {
			Bitmap bitmap = Utils.decodeImage(context.getContentResolver(),
					getOriginalPhotoUri(), size);

			return bitmap;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	private Bitmap getThumbnailImageFromMediaStore(Context context) {
		Resources res = context.getResources();

		final int kind = res.getBoolean(R.bool.load_mini_thumbnails) ? Thumbnails.MINI_KIND
				: Thumbnails.MICRO_KIND;

		BitmapFactory.Options opts = null;
		if (kind == Thumbnails.MINI_KIND
				&& res.getBoolean(R.bool.sample_mini_thumbnails)) {
			opts = new BitmapFactory.Options();
			opts.inSampleSize = 2;
		}

		try {
			final long id = Long.parseLong(getOriginalPhotoUri()
					.getLastPathSegment());

			Bitmap bitmap = Thumbnails.getThumbnail(
					context.getContentResolver(), id, kind, opts);

			return bitmap;
		} catch (Exception e) {
			if (Flags.DEBUG) {
				e.printStackTrace();
			}
			return null;
		}
	}

	public Uri getOriginalPhotoUri() {

		Logger.Log(TAG, "getOriginalPhotoUri from: " + image_id_path);

		if (null == mFullUri && !TextUtils.isEmpty(image_id_path)) {
			mFullUri = Uri.parse(image_id_path);
		}
		return mFullUri;
	}
}
