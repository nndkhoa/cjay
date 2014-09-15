package com.cloudjay.cjay.model;

import android.net.Uri;

public class IsoCode {

    int id;
    int type;
    String code;
    String display_name;

    public static final String TABLE = "iso_code";
	public static final String ID = "id";
    public static final String TYPE = "type";
    public static final String CODE = "code";
    public static final String DISPLAY_NAME = "display_name";

    public static final Uri URI = Uri.parse("content://" + User.AUTHORITY + "/" + TABLE);
}
