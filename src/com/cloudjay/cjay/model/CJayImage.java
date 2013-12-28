package com.cloudjay.cjay.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.cloudjay.cjay.dao.CJayImageDaoImpl;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "cjay_image", daoClass = CJayImageDaoImpl.class)
public class CJayImage implements Parcelable {

	public static final int STATE_UPLOAD_COMPLETED = 4;
	public static final int STATE_UPLOAD_ERROR = 3;
	public static final int STATE_UPLOAD_IN_PROGRESS = 2;
	public static final int STATE_UPLOAD_WAITING = 1;
	public static final int STATE_NONE = 0;

	private static final String ID = "id";
	private static final String IMAGE_NAME = "image_name";
	private static final String TYPE = "type";
	private static final String TIME_POSTED = "time_posted";
	private static final String FIELD_STATE = "state";
	private static final String FIELD_URI = "uri";
	private static final String FIELD_UUID = "uuid";

	public CJayImage() {

	}

	public CJayImage(int id, int type, String time_posted, String image_name) {
		this.id = id;
		this.setType(type);
		this.setImageName(image_name);
		this.time_posted = time_posted;
	}

	@DatabaseField(columnName = ID)
	int id;

	@DatabaseField(columnName = IMAGE_NAME)
	String image_name;

	@DatabaseField(columnName = TIME_POSTED)
	String time_posted;

	@DatabaseField(columnName = FIELD_UUID)
	String uuid;

	/**
	 * TYPE include: in | out | issue | repaired
	 */
	@DatabaseField(columnName = TYPE)
	int type;

	@DatabaseField(columnName = FIELD_STATE)
	int mState;

	@DatabaseField(columnName = FIELD_URI, id = true)
	String mUri;

	Uri mFullUri;

	@DatabaseField(canBeNull = true, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
	ContainerSession containerSession;

	@DatabaseField(canBeNull = true, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
	Issue issue;

	public Issue getIssue() {
		return issue;
	}

	public void setIssue(Issue issue) {
		this.issue = issue;
	}

	public Uri getOriginalPhotoUri() {
		if (null == mFullUri && !TextUtils.isEmpty(mUri)) {
			mFullUri = Uri.parse(mUri);
		}
		return mFullUri;
	}

	public String getTimePosted() {
		return time_posted;
	}

	public void setTimePosted(String time_posted) {
		this.time_posted = time_posted;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public ContainerSession getContainerSession() {
		return containerSession;
	}

	public void setContainerSession(ContainerSession containerSession) {
		this.containerSession = containerSession;
	}

	public String getImageName() {
		return image_name;
	}

	public void setImageName(String image_name) {
		this.image_name = image_name;
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
		mState = state;
	}

	public String getUri() {
		return mUri;
	}

	public void setUri(String uri) {
		mUri = uri;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {

		dest.writeInt(id);
		dest.writeString(image_name);
		dest.writeString(time_posted);
		dest.writeString(uuid);
		dest.writeInt(type);
		dest.writeInt(mState);
		dest.writeString(mUri);
		dest.writeParcelable(containerSession, 0);
		dest.writeParcelable(issue, 0);
	}

	private void readFromParcel(Parcel in) {
		this.id = in.readInt();
		this.image_name = in.readString();
		this.time_posted = in.readString();
		this.uuid = in.readString();
		this.type = in.readInt();
		this.mState = in.readInt();
		this.mUri = in.readString();
		in.readParcelable(ContainerSession.class.getClassLoader());
		in.readParcelable(Issue.class.getClassLoader());
	}

	public static final Parcelable.Creator<CJayImage> CREATOR = new Parcelable.Creator<CJayImage>() {

		public CJayImage createFromParcel(Parcel source) {
			return new CJayImage(source);
		}

		public CJayImage[] newArray(int size) {
			return new CJayImage[size];
		}
	};

	public CJayImage(Parcel in) {

		readFromParcel(in);
	}
}
