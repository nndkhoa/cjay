package com.cloudjay.cjay.model;

import android.net.Uri;

import java.util.List;

public class Session {

	// Server fields
    private int id;
	private String depot_code;
	private String container_id;
	private String image_id_path;
	private String check_in_time;
	private String check_out_time;
	private String time_modified;
	private String type;            // A    |   B   |   C
	private String status;          // NEW  |   XX  |   YY
	private String operator_code;
	private String operator_name;
	private int operator_id; //references operatorTable

	private List<AuditReportItem> audit_report_items;
	private List<GateReportImage> gate_report_images;

	// Local fields
    private int upload_type;        // NONE |   WAITING |   IN_PROGRESS

    public static final String TABLE = "session";
    public static final String ID = "id";
    public static final String DEPOT_CODE = "depot_code";
    public static final String CONTAINER_ID = "container_id";
    public static final String IMAGE_ID_PATH = "image_id_path";
    public static final String CHECK_IN_TIME = "check_in_time";
    public static final String CHECK_OUT_TIME = "check_out_time";
    public static final String TIME_MODIFIED = "time_modified";
    public static final String TYPE = "type";
    public static final String STATUS = "status";
    public static final String OPERATOR_ID = "operator_id";
    public static final String OPERATOR_CODE = "operator_code";
    public static final String OPERATOR_NAME = "operator_name";
    public static final String UPLOAD_TYPE = "upload_type";

    public static final Uri URI = Uri.parse("content://" + User.AUTHORITY + "/" + TABLE);

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDepot_code() {
        return depot_code;
    }

    public void setDepotCode(String depot_code) {
        this.depot_code = depot_code;
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

    public String getTimeModified() {
        return time_modified;
    }

    public void setTimeModified(String time_modified) {
        this.time_modified = time_modified;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOperatorCode() {
        return operator_code;
    }

    public void setOperatorCode(String operator_code) {
        this.operator_code = operator_code;
    }

    public String getOperatorName() {
        return operator_name;
    }

    public void setOperatorName(String operator_name) {
        this.operator_name = operator_name;
    }

    public int getOperatorId() {
        return operator_id;
    }

    public void setOperatorId(int operator_id) {
        this.operator_id = operator_id;
    }

    public List<AuditReportItem> getAudit_report_items() {
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

    public int getUploadType() {
        return upload_type;
    }

    public void setUploadType(int upload_type) {
        this.upload_type = upload_type;
    }
}
