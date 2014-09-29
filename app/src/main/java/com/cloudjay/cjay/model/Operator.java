package com.cloudjay.cjay.model;

import android.net.Uri;

public class Operator {
    public int id;
    public String Name;
    public String operator_code;

    public static final String TABLE = "operator";
    public static final String ID = "id";
    public static final String OPERATOR_CODE = "operator_code";
    public static final String OPERATOR_NAME = "operator_name";

    public static final Uri URI = Uri.parse("content://" + User.AUTHORITY + "/" + TABLE);
}
