package com.cloudjay.cjay.model;

import android.net.Uri;

import java.util.List;

public class Session {

	// Server fields
	int id;
	String depot_code;
	String container_id;
	String image_id_path;
	String check_in_time;
	String check_out_time;
	String time_modified;
	String type;            // A    |   B   |   C
	String status;          // NEW  |   XX  |   YY
	String operator_code;
	String operator_name;
	int operator_id; //references operatorTable

	List<AuditReportItem> audit_report_items;
	List<GateReportImage> gate_report_images;

	// Local fields
	int upload_type;        // NONE |   WAITING |   IN_PROGRESS

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

}
