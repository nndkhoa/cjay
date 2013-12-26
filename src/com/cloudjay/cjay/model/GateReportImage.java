package com.cloudjay.cjay.model;

import java.util.ArrayList;
import java.util.Date;

import com.cloudjay.cjay.events.UploadStateChangedEvent;
import com.j256.ormlite.field.DatabaseField;

import de.greenrobot.event.EventBus;

import android.R.integer;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

@SuppressLint("ParcelCreator")
public class GateReportImage implements Parcelable {

	public static final int STATE_UPLOAD_COMPLETED = 5;
	public static final int STATE_UPLOAD_ERROR = 4;
	public static final int STATE_UPLOAD_IN_PROGRESS = 3;
	public static final int STATE_UPLOAD_WAITING = 2;
	public static final int STATE_SELECTED = 1;
	public static final int STATE_NONE = 0;

	public static final String FIELD_STATE = "state";
	static final String FIELD_URI = "uri";

	private int id;
	private int type;
	private String time_posted;
	private String image_name;
	@DatabaseField(columnName = FIELD_STATE)
	private int mState;

	private int mProgress;
	private Bitmap mBigPictureNotificationBmp;

	public int getUploadState() {
		return mState;
	}

	public int getUploadProgress() {
		return mProgress;
	}

	public void setUploadProgress(int progress) {
		if (progress != mProgress) {
			mProgress = progress;
			notifyUploadStateListener();
		}
	}

	private void notifyUploadStateListener() {
		EventBus.getDefault().post(new UploadStateChangedEvent(this));
	}

	public GateReportImage() {

	}

	public GateReportImage(int id, int type, String time_posted,
			String image_name) {
		this.id = id;
		this.type = type;
		this.time_posted = time_posted;
		this.image_name = image_name;
	}

	public GateReportImage(int type, String time_posted, String image_name) {
		this.type = type;
		this.time_posted = time_posted;
		this.image_name = image_name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getTimePosted() {
		return time_posted;
	}

	public void setTimePosted(String time_posted) {
		this.time_posted = time_posted;
	}

	public String getImageName() {
		return image_name;
	}

	public void setImageName(String image_name) {
		this.image_name = image_name;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeInt(type);
		dest.writeString(time_posted);
		dest.writeString(image_name);

	}

	public GateReportImage(Parcel in) {

		readFromParcel(in);
	}

	private void readFromParcel(Parcel in) {
		this.id = in.readInt();
		this.type = in.readInt();
		this.time_posted = in.readString();
		this.image_name = in.readString();
	}

	public static final Parcelable.Creator<GateReportImage> CREATOR = new Parcelable.Creator<GateReportImage>() {

		public GateReportImage createFromParcel(Parcel source) {
			return new GateReportImage(source);
		}

		public GateReportImage[] newArray(int size) {
			return new GateReportImage[size];
		}
	};
}
