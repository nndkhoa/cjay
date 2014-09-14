package com.cloudjay.cjay.model;

/**
 * Created by nambv on 14/09/2014.
 */
public class AuditReportItem {

    int id;
    String timePosted;
    int repairCodeId;
    int damageCodeId;
    int payCodeId;
    int componentCodeId;
    String locationCode;
    int length;
    int heigth;
    int quantity;
    int isFixAllowed;

    public static final String TABLE = "audit_report_item";
    public static final String ID = "id";
    public static final String TIME_POSTED = "time_posted";
    public static final String REPAIR_CODE_ID = "repair_code_id";
    public static final String DAMAGE_CODE_ID = "damage_code_id";
    public static final String PAY_CODE_ID = "pay_code_id";
    public static final String COMPONENT_CODE_ID = "component_code_id";
    public static final String LOCATION_CODE = "location_code";
    public static final String LENGTH = "length";
    public static final String HEIGHT = "height";
    public static final String QUANTITY = "quantity";
    public static final String IS_FIX_ALLOWED = "is_fix_allowed";

}
