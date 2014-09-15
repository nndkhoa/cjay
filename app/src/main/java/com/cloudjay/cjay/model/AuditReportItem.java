package com.cloudjay.cjay.model;

import android.net.Uri;

/**
 * Created by nambv on 14/09/2014.
 */
public class AuditReportItem {

    int id;
    String time_posted;
    int repair_id;
    int repair_code;
    int damage_id;
    int damage_code;
    int component_id;
    int component_code;
    String component_name;
    String location_code;
    int length;
    int height;
    int quantity;
    int is_fix_allowed;
    int session_id;

    public static final String TABLE = "audit_report_item";
    public static final String ID = "id";
    public static final String TIME_POSTED = "time_posted";
    public static final String REPAIR_ID = "repair_id";
    public static final String REPAIR_CODE = "repair_code";
    public static final String DAMAGE_ID = "damage_id";
    public static final String DAMAGE_CODE = "damage_code";
    public static final String COMPONENT_ID = "component_id";
    public static final String COMPONENT_CODE = "component_code";
    public static final String COMPONENT_NAME = "component_name";
    public static final String LOCATION_CODE = "location_code";
    public static final String LENGTH = "length";
    public static final String HEIGHT = "height";

    public static final String QUANTITY = "quantity";
    public static final String IS_FIX_ALLOWED = "is_fix_allowed";
    public static final String SESSION_ID = "session_id";

    public static final Uri URI = Uri.parse("content://" + User.AUTHORITY + "/" + TABLE);

}
