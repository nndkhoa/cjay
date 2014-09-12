package com.cloudjay.cjay.model;

import android.net.Uri;

public class User {
    public static final String TABLE = "user";
    public static final String EMAIL = "email";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String ROLE = "role";
    public static final String MOTHER_NAME = "mother_name";
    public static final String FATHER_NAME = "father_name";
    public static final String AUTHORITY = "com.cloudjay.cjay";
    public static final Uri URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE);
}

