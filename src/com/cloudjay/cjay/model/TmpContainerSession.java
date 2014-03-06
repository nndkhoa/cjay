package com.cloudjay.cjay.model;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

import com.cloudjay.cjay.util.Logger;

@SuppressLint("ParcelCreator")
public class TmpContainerSession implements Parcelable {

	private static final String TAG = "TmpContainerSession";

	private int id;
	private String container_id;
	private String image_id_path;
	private String operator_code;
	private int operator_id;
	private String check_in_time;
	private String check_out_time;
	private String depot_code;
	private String container_id_image;
	private List<AuditReportItem> audit_report_items;
	private List<GateReportImage> gate_report_images;

	public TmpContainerSession() {
		audit_report_items = new ArrayList<AuditReportItem>();
		gate_report_images = new ArrayList<GateReportImage>();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getContainerId() {
		return container_id;
	}

	public void setContainerId(String container_id) {
		this.container_id = container_id;
	}

	public String getImageIdPath() {
		return image_id_path;
	}

	public void setImageIdPath(String image_id_path) {
		this.image_id_path = image_id_path;
	}

	public String getOperatorCode() {
		return operator_code;
	}

	public void setOperatorCode(String operator_code) {
		this.operator_code = operator_code;
	}

	public String getCheckInTime() {
		return check_in_time;
	}

	public void setCheckInTime(String check_in_time) {
		this.check_in_time = check_in_time;
	}

	public String getCheckOutTime() {
		return check_out_time;
	}

	public void setCheckOutTime(String check_out_time) {
		this.check_out_time = check_out_time;
	}

	public String getDepotCode() {
		return depot_code;
	}

	public void setDepotCode(String depot_code) {
		this.depot_code = depot_code;
	}

	public List<AuditReportItem> getAuditReportItems() {
		return audit_report_items;
	}

	public void setAuditReportItems(List<AuditReportItem> audit_report_items) {
		this.audit_report_items = audit_report_items;
	}

	public List<GateReportImage> getGateReportImages() {
		return gate_report_images;
	}

	public void setGateReportImages(List<GateReportImage> gate_report_images) {
		this.gate_report_images = gate_report_images;
	}

	public void printMe() {
		Logger.Log(TAG, "CId: " + getContainerId() + " - OpCode: "
				+ getOperatorCode() + " - Depot Code: " + getDepotCode()
				+ " - Time In: " + getCheckInTime() + " - Time Out: "
				+ getCheckOutTime());
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {

		dest.writeInt(id);
		dest.writeString(container_id);
		dest.writeString(image_id_path);
		dest.writeString(operator_code);
		dest.writeString(check_in_time);
		dest.writeString(check_out_time);
		dest.writeString(depot_code);
		dest.writeTypedList(audit_report_items);
		dest.writeTypedList(gate_report_images);
	}

	private void readFromParcel(Parcel in) {
		this.audit_report_items = new ArrayList<AuditReportItem>();
		this.gate_report_images = new ArrayList<GateReportImage>();

		this.id = in.readInt();
		this.container_id = in.readString();
		this.image_id_path = in.readString();
		this.operator_code = in.readString();
		this.check_in_time = in.readString();
		this.check_out_time = in.readString();
		this.depot_code = in.readString();

		in.readTypedList(audit_report_items, AuditReportItem.CREATOR);
		in.readTypedList(gate_report_images, GateReportImage.CREATOR);
	}

	public static final Parcelable.Creator<TmpContainerSession> CREATOR = new Parcelable.Creator<TmpContainerSession>() {

		public TmpContainerSession createFromParcel(Parcel source) {
			return new TmpContainerSession(source);
		}

		public TmpContainerSession[] newArray(int size) {
			return new TmpContainerSession[size];
		}
	};

	public TmpContainerSession(Parcel in) {

		readFromParcel(in);
	}

	public int getOperatorId() {
		return operator_id;
	}

	public void setOperatorId(int operator_id) {
		this.operator_id = operator_id;
	}

	public String getContainerIdImage() {
		return container_id_image;
	}

	public void setContainerIdImage(String container_id_image) {
		this.container_id_image = container_id_image;
	}

}