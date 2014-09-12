package com.cloudjay.cjay.model;

public class Container {

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
	int operator_id;

	String audit_report_items;
	String gate_report_images;

	// Local fields
	int upload_type;        // NONE |   WAITING |   IN_PROGRESS
}
