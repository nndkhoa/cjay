package com.cloudjay.cjay.model;

import android.net.Uri;

public class User {

    private int id;
    private String email;
    private String access_token;
    private int role;
    private String role_name;
    private String first_name;
    private String last_name;
    private String full_name;
    private String phone;
    private String avatar_url;
    private int dialing_code;
    private String depot_code;

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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAccessToken() {
        return access_token;
    }

    public void setAccessToken(String access_token) {
        this.access_token = access_token;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public String getRoleName() {
        return role_name;
    }

    public void setRoleName(String role_name) {
        this.role_name = role_name;
    }

    public String getFirstName() {
        return first_name;
    }

    public void setFirstName(String first_name) {
        this.first_name = first_name;
    }

    public String getLastName() {
        return last_name;
    }

    public void setLastName(String last_name) {
        this.last_name = last_name;
    }

    public String getFullName() {
        return full_name;
    }

    public void setFullName(String full_name) {
        this.full_name = full_name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatarUrl() {
        return avatar_url;
    }

    public void setAvatarUrl(String avatar_url) {
        this.avatar_url = avatar_url;
    }

    public int getDialingCode() {
        return dialing_code;
    }

    public void setDialingCode(int dialing_code) {
        this.dialing_code = dialing_code;
    }

    public String getDepotCode() {
        return depot_code;
    }

    public void setDepotCode(String depot_code) {
        this.depot_code = depot_code;
    }
}

