package com.cloudjay.cjay.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

// TODO: need to add Parcelable for List<>
@SuppressLint("ParcelCreator")
public class AuditReportItem implements Parcelable {

	private int damage_id;
	private int repair_id;
	private int component_id;
	private String location_code;
	private String length;
	private String height;
	private String quantity;

	private List<AuditReportImage> audit_report_images;

	public AuditReportItem() {

	}

	public AuditReportItem(Issue issue) {
		if (null != issue) {
			this.damage_id = issue.getDamageCode().getId();
			this.repair_id = issue.getRepairCode().getId();
			this.component_id = issue.getComponentCode().getId();
			this.length = issue.getLength();
			this.height = issue.getHeight();
			this.quantity = issue.getQuantity();
			this.location_code = issue.getLocationCode();

			audit_report_images = new ArrayList<AuditReportImage>();
			Collection<CJayImage> cJayImages = issue.getCJayImages();
			if (null != cJayImages) {
				for (CJayImage cJayImage : cJayImages) {
					if (cJayImage.getType() == CJayImage.TYPE_REPORT) {
						audit_report_images.add(new AuditReportImage(cJayImage
								.getType(), cJayImage.getTimePosted(),
								cJayImage.getImageName()));
					}
				}
			}
		}
	}

	@Override
	public int describeContents() {
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
