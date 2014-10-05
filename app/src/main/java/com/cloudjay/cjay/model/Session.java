package com.cloudjay.cjay.model;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;

@Generated("org.jsonschema2pojo")
public class Session extends RealmObject {

	@Ignore
	public static final String FIELD_CONTAINER_ID = "container_id";

	@Expose
	private long id;

	@Expose
	private long step;

	@SerializedName("pre_status")
	@Expose
	private long preStatus;

	@Expose
	private long status;

	@SerializedName(FIELD_CONTAINER_ID)
	@Expose
	private String containerId;

	@SerializedName("operator_code")
	@Expose
	private String operatorCode;

	@SerializedName("operator_id")
	@Expose
	private long operatorId;

	@SerializedName("depot_code")
	@Expose
	private String depotCode;

	@SerializedName("depot_id")
	@Expose
	private long depotId;

	@SerializedName("check_in_time")
	@Expose
	private String checkInTime;

	@SerializedName("check_out_time")
	@Expose
	private String checkOutTime;

	@SerializedName("gate_images")
	@Expose
	private RealmList<GateImage> gateImages;

	@SerializedName("audit_items")
	@Expose
	private RealmList<AuditItem> auditItems;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Session withId(long id) {
		this.id = id;
		return this;
	}

	public long getStep() {
		return step;
	}

	public void setStep(long step) {
		this.step = step;
	}

	public Session withStep(long step) {
		this.step = step;
		return this;
	}

	public long getPreStatus() {
		return preStatus;
	}

	public void setPreStatus(long preStatus) {
		this.preStatus = preStatus;
	}

	public Session withPreStatus(long preStatus) {
		this.preStatus = preStatus;
		return this;
	}

	public long getStatus() {
		return status;
	}

	public void setStatus(long status) {
		this.status = status;
	}

	public Session withStatus(long status) {
		this.status = status;
		return this;
	}

	public String getContainerId() {
		return containerId;
	}

	public void setContainerId(String containerId) {
		this.containerId = containerId;
	}

	public Session withContainerId(String containerId) {
		this.containerId = containerId;
		return this;
	}

	public String getOperatorCode() {
		return operatorCode;
	}

	public void setOperatorCode(String operatorCode) {
		this.operatorCode = operatorCode;
	}

	public Session withOperatorCode(String operatorCode) {
		this.operatorCode = operatorCode;
		return this;
	}

	public long getOperatorId() {
		return operatorId;
	}

	public void setOperatorId(long operatorId) {
		this.operatorId = operatorId;
	}

	public Session withOperatorId(long operatorId) {
		this.operatorId = operatorId;
		return this;
	}

	public String getDepotCode() {
		return depotCode;
	}

	public void setDepotCode(String depotCode) {
		this.depotCode = depotCode;
	}

	public Session withDepotCode(String depotCode) {
		this.depotCode = depotCode;
		return this;
	}

	public long getDepotId() {
		return depotId;
	}

	public void setDepotId(long depotId) {
		this.depotId = depotId;
	}

	public Session withDepotId(long depotId) {
		this.depotId = depotId;
		return this;
	}

	public String getCheckInTime() {
		return checkInTime;
	}

	public void setCheckInTime(String checkInTime) {
		this.checkInTime = checkInTime;
	}

	public Session withCheckInTime(String checkInTime) {
		this.checkInTime = checkInTime;
		return this;
	}

	public Object getCheckOutTime() {
		return checkOutTime;
	}

	public void setCheckOutTime(String checkOutTime) {
		this.checkOutTime = checkOutTime;
	}

	public Session withCheckOutTime(String checkOutTime) {
		this.checkOutTime = checkOutTime;
		return this;
	}

	public RealmList<GateImage> getGateImages() {
		return gateImages;
	}

	public void setGateImages(RealmList<GateImage> gateImages) {
		this.gateImages = gateImages;
	}

	public Session withGateImages(RealmList<GateImage> gateImages) {
		this.gateImages = gateImages;
		return this;
	}

	public RealmList<AuditItem> getAuditItems() {
		return auditItems;
	}

	public void setAuditItems(RealmList<AuditItem> auditItems) {
		this.auditItems = auditItems;
	}

	public Session withAuditItems(RealmList<AuditItem> auditItems) {
		this.auditItems = auditItems;
		return this;
	}

}