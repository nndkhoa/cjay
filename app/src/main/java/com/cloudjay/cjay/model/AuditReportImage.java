package com.cloudjay.cjay.model;

/**
 * Created by nambv on 14/09/2014.
 */
public class AuditReportImage {

    int id;
    int type;
    String imageName;
    String imageUrl;
    String createdAt;
    int auditReportItemId;

    public static final String TABLE = "audit_report_image";
    public static final String ID = "id";
    public static final String TYPE = "type";
    public static final String IMAGE_NAME = "image_name";
    public static final String IMAGE_URL = "image_url";
    public static final String CREATED_AT = "created_at";
    public static final String AUDIT_REPORT_ITEM_ID = "audit_report_item_id";
}
