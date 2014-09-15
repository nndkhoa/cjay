package com.cloudjay.cjay.model;

import android.net.Uri;

/**
 * Created by nambv on 14/09/2014.
 */
public class AuditReportItem {

    public int id;
    public String time_posted;
    public int repair_id;
    public int repair_code;
    public int damage_id;
    public int damage_code;
    public int component_id;
    public int component_code;
    public String component_name;
    public String location_code;
    public int length;
    public int height;
    public int quantity;
    public int is_fix_allowed;
    public int session_id;

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
