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
	private String created_at;
	private String image_name;

	public GateReportImage() {

	}

	public GateReportImage(int id, int type, String time_posted,
			String image_name) {
		this.id = id;
		this.type = type;
		this.created_at = time_posted;
		this.image_name = image_name;
	}

	public GateReportImage(int type, String time_posted, String image_name) {
		this.type = type;
		this.created_at = time_posted;
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

	public String getCreatedAt() {
		return created_at;
	}

	public void setCreatedAt(String time_posted) {
		this.created_at = time_posted;
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
		dest.writeString(created_at);
		dest.writeString(image_name);

	}

	public GateReportImage(Parcel in) {

		readFromParcel(in);
	}

	private void readFromParcel(Parcel in) {
		this.id = in.readInt();
		this.type = in.readInt();
		this.created_at = in.readString();
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
