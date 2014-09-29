package com.cloudjay.cjay.model;

import android.net.Uri;

public class GateReportImage {

    public int id;
    public int type;
    public String image_name;
    public String image_url;
    public String created_at;
    public int container_id;

    public static final String TABLE = "gate_report_image";
    public static final String ID = "id";
    public static final String TYPE = "type";
    public static final String IMAGE_NAME = "image_name";
    public static final String IMAGE_URL = "image_url";
    public static final String CREATED_AT = "created_at";
    public static final String CONTAINER_ID = "container_id";

    public static final Uri URI = Uri.parse("content://" + User.AUTHORITY + "/" + TABLE);
}
