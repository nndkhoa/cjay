package com.cloudjay.cjay.data.model;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Session {

	@Expose
	private long id;

	@Expose
	private long step;

	@SerializedName("pre_status")
	@Expose
	private long preStatus;

	@Expose
	private long status;
	@SerializedName("container_id")
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
	@SerializedName("supervise_time")
	@Expose
	private String superviseTime;
	@SerializedName("approve_time")
	@Expose
	private String approveTime;
	@SerializedName("input_time")
	@Expose
	private String inputTime;
	@SerializedName("repair_time")
	@Expose
	private Object repairTime;
	@SerializedName("available_time")
	@Expose
	private Object availableTime;
	@SerializedName("check_out_time")
	@Expose
	private Object checkOutTime;
	@SerializedName("estimate_time")
	@Expose
	private String estimateTime;
	@SerializedName("user_check_in_name")
	@Expose
	private String userCheckInName;
	@SerializedName("user_supervise_name")
	@Expose
	private String userSuperviseName;
	@SerializedName("user_repair_name")
	@Expose
	private Object userRepairName;
	@SerializedName("user_available_name")
	@Expose
	private Object userAvailableName;
	@SerializedName("user_check_out_name")
	@Expose
	private Object userCheckOutName;
	@SerializedName("gate_images")
	@Expose
	private List<GateImage> gateImages = new ArrayList<GateImage>();
	@SerializedName("audit_items")
	@Expose
	private List<AuditItem> auditItems = new ArrayList<AuditItem>();

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

	public String getSuperviseTime() {
		return superviseTime;
	}

	public void setSuperviseTime(String superviseTime) {
		this.superviseTime = superviseTime;
	}

	public Session withSuperviseTime(String superviseTime) {
		this.superviseTime = superviseTime;
		return this;
	}

	public String getApproveTime() {
		return approveTime;
	}

	public void setApproveTime(String approveTime) {
		this.approveTime = approveTime;
	}

	public Session withApproveTime(String approveTime) {
		this.approveTime = approveTime;
		return this;
	}

	public String getInputTime() {
		return inputTime;
	}

	public void setInputTime(String inputTime) {
		this.inputTime = inputTime;
	}

	public Session withInputTime(String inputTime) {
		this.inputTime = inputTime;
		return this;
	}

	public Object getRepairTime() {
		return repairTime;
	}

	public void setRepairTime(Object repairTime) {
		this.repairTime = repairTime;
	}

	public Session withRepairTime(Object repairTime) {
		this.repairTime = repairTime;
		return this;
	}

	public Object getAvailableTime() {
		return availableTime;
	}

	public void setAvailableTime(Object availableTime) {
		this.availableTime = availableTime;
	}

	public Session withAvailableTime(Object availableTime) {
		this.availableTime = availableTime;
		return this;
	}

	public Object getCheckOutTime() {
		return checkOutTime;
	}

	public void setCheckOutTime(Object checkOutTime) {
		this.checkOutTime = checkOutTime;
	}

	public Session withCheckOutTime(Object checkOutTime) {
		this.checkOutTime = checkOutTime;
		return this;
	}

	public String getEstimateTime() {
		return estimateTime;
	}

	public void setEstimateTime(String estimateTime) {
		this.estimateTime = estimateTime;
	}

	public Session withEstimateTime(String estimateTime) {
		this.estimateTime = estimateTime;
		return this;
	}

	public String getUserCheckInName() {
		return userCheckInName;
	}

	public void setUserCheckInName(String userCheckInName) {
		this.userCheckInName = userCheckInName;
	}

	public Session withUserCheckInName(String userCheckInName) {
		this.userCheckInName = userCheckInName;
		return this;
	}

	public String getUserSuperviseName() {
		return userSuperviseName;
	}

	public void setUserSuperviseName(String userSuperviseName) {
		this.userSuperviseName = userSuperviseName;
	}

	public Session withUserSuperviseName(String userSuperviseName) {
		this.userSuperviseName = userSuperviseName;
		return this;
	}

	public Object getUserRepairName() {
		return userRepairName;
	}

	public void setUserRepairName(Object userRepairName) {
		this.userRepairName = userRepairName;
	}

	public Session withUserRepairName(Object userRepairName) {
		this.userRepairName = userRepairName;
		return this;
	}

	public Object getUserAvailableName() {
		return userAvailableName;
	}

	public void setUserAvailableName(Object userAvailableName) {
		this.userAvailableName = userAvailableName;
	}

	public Session withUserAvailableName(Object userAvailableName) {
		this.userAvailableName = userAvailableName;
		return this;
	}

	public Object getUserCheckOutName() {
		return userCheckOutName;
	}

	public void setUserCheckOutName(Object userCheckOutName) {
		this.userCheckOutName = userCheckOutName;
	}

	public Session withUserCheckOutName(Object userCheckOutName) {
		this.userCheckOutName = userCheckOutName;
		return this;
	}

	public List<GateImage> getGateImages() {
		return gateImages;
	}

	public void setGateImages(List<GateImage> gateImages) {
		this.gateImages = gateImages;
	}

	public Session withGateImages(List<GateImage> gateImages) {
		this.gateImages = gateImages;
		return this;
	}

	public List<AuditItem> getAuditItems() {
		return auditItems;
	}

	public void setAuditItems(List<AuditItem> auditItems) {
		this.auditItems = auditItems;
	}

	public Session withAuditItems(List<AuditItem> auditItems) {
		this.auditItems = auditItems;
		return this;
	}

}