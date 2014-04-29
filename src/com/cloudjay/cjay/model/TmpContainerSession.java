package com.cloudjay.cjay.model;

import java.util.ArrayList;
import java.util.List;

public class TmpContainerSession {

	private int id;
	private String container_id;
	private String image_id_path;
	private String operator_code;
	private int operator_id;
	private String check_in_time;
	private String check_out_time;
	private String depot_code;
	private String container_id_image;
	private int status;

	private List<AuditReportItem> audit_report_items;
	private List<GateReportImage> gate_report_images;

	public TmpContainerSession() {
		audit_report_items = new ArrayList<AuditReportItem>();
		gate_report_images = new ArrayList<GateReportImage>();
	}

	public List<AuditReportItem> getAuditReportItems() {
		return audit_report_items;
	}

	public String getCheckInTime() {
		return check_in_time;
	}

	public String getCheckOutTime() {
		return check_out_time;
	}

	public String getContainerId() {
		return container_id;
	}

	public String getContainerIdImage() {
		return container_id_image;
	}

	public String getDepotCode() {
		return depot_code;
	}

	public List<GateReportImage> getGateReportImages() {
		return gate_report_images;
	}

	public int getId() {
		return id;
	}

	public String getImageIdPath() {
		return image_id_path;
	}

	public String getOperatorCode() {
		return operator_code;
	}

	public int getOperatorId() {
		return operator_id;
	}

	public void setAuditReportItems(List<AuditReportItem> audit_report_items) {
		this.audit_report_items = audit_report_items;
	}

	public void setCheckInTime(String check_in_time) {
		this.check_in_time = check_in_time;
	}

	public void setCheckOutTime(String check_out_time) {
		this.check_out_time = check_out_time;
	}

	public void setContainerId(String container_id) {
		this.container_id = container_id;
	}

	public void setContainerIdImage(String container_id_image) {
		this.container_id_image = container_id_image;
	}

	public void setDepotCode(String depot_code) {
		this.depot_code = depot_code;
	}

	public void setGateReportImages(List<GateReportImage> gate_report_images) {
		this.gate_report_images = gate_report_images;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setImageIdPath(String image_id_path) {
		this.image_id_path = image_id_path;
	}

	public void setOperatorCode(String operator_code) {
		this.operator_code = operator_code;
	}

	public void setOperatorId(int operator_id) {
		this.operator_id = operator_id;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
}