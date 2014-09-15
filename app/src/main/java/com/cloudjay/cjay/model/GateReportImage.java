package com.cloudjay.cjay.model;

/**
 * Created by nambv on 14/09/2014.
 */
public class GateReportImage {

    int id;
    int type;
    String imageName;
    String imageUrl;
    String createdAt;
    int containerId;

    public static final String TABLE = "gate_report_image";
    public static final String ID = "id";
    public static final String TYPE = "type";
    public static final String IMAGE_NAME = "image_name";
    public static final String IMAGE_URL = "image_url";
    public static final String CREATED_AT = "created_at";
    public static final String CONTAINER_ID = "container_id";
}
