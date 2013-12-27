package com.cloudjay.cjay.model;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

@SuppressLint("ParcelCreator")
public class GateReportImage implements Parcelable {

	public static final String FIELD_STATE = "state";
	static final String FIELD_URI = "uri";

	private int id;
	private int type;
	private String time_posted;
	private String image_name;

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
