package com.cloudjay.cjay.model;

import android.net.Uri;

public class User {

    int id;
    String email;
    String access_token;
    int role;
    String role_name;
    String first_name;
    String last_name;
    String full_name;
    String phone;
    String avatar_url;
    int dialing_code;
    int depot_code;

    public static final String TABLE = "user";
    public static final String EMAIL = "email";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String ROLE = "role";
    public static final String ROLE_NAME = "role_name";
    public static final String ID = "id";
    public static final String FIRST_NAME = "first_name";
    public static final String LAST_NAME = "last_name";
    public static final String FULL_NAME = "full_name";
    public static final String PHONE = "phone";
    public static final String AVATAR_URL = "avatar_url";
    public static final String DIALING_CODE = "dialing_code";
    public static final String DEPOT_CODE = "depot_code";
    public static final String AUTHORITY = "com.cloudjay.cjay";
    public static final Uri URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE);
}

