package com.cloudjay.cjay.model;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

@SuppressLint("ParcelCreator")
public class AuditReportItem implements Parcelable {

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {

	}

	public AuditReportItem(Parcel in) {

		readFromParcel(in);
	}

	private void readFromParcel(Parcel in) {

	}

	public static final Parcelable.Creator<AuditReportItem> CREATOR = new Parcelable.Creator<AuditReportItem>() {

		public AuditReportItem createFromParcel(Parcel source) {
			return new AuditReportItem(source);
		}

		public AuditReportItem[] newArray(int size) {
			return new AuditReportItem[size];
		}
	};

}
