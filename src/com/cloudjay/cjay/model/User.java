package com.cloudjay.cjay.model;

import android.R.integer;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "user")
public class User {

	@DatabaseField(id = true, columnName = "id")
	private int id;

	@DatabaseField(columnName = "username")
	private String username;

	@DatabaseField(columnName = "email")
	private String email;

	@DatabaseField(columnName = "is_main_account", defaultValue = "0")
	private boolean is_main_account;

	@DatabaseField(columnName = "access_token")
	private String access_token;

	@DatabaseField(columnName = "first_name")
	private String first_name;

	@DatabaseField(columnName = "last_name")
	private String last_name;

	@DatabaseField(columnName = "full_name")
	private String full_name;

	@DatabaseField(columnName = "role_name")
	private String role_name;

	@DatabaseField(columnName = "role", canBeNull = false)
	private int role;

	@DatabaseField(columnName = "avatar_url")
	private String avatar_url;

	@DatabaseField(columnName = "dialing_code")
	private int dialing_code;

	@DatabaseField(columnName = "phone")
	private int phone;

	public String getAccessToken() {
		return access_token;
	}

	public void setAccessToken(String accessToken) {
		this.access_token = accessToken;
	}

	public String getUserName() {
		return username;
	}

	public void setUserName(String userName) {
		this.username = userName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFirstName() {
		return first_name;
	}

	public void setFirstName(String firstName) {
		this.first_name = firstName;
	}

	public String getLastName() {
		return last_name;
	}

	public void setLastName(String lastName) {
		this.last_name = lastName;
	}

	public boolean isMainAccount() {
		return is_main_account;
	}

	public void setMainAccount(boolean isMainAccount) {
		this.is_main_account = isMainAccount;
	}

	public String getFullName() {
		return full_name;
	}

	public void setFullName(String fullName) {
		this.full_name = fullName;
	}

	public String getAvatar() {
		return this.avatar_url;
	}

	public void setAvatar(String avatarUrl) {
		this.avatar_url = avatarUrl;
	}

	public String getRoleName() {
		return role_name;
	}

	public void setRoleName(String roleName) {
		this.role_name = roleName;
	}

	public int getDialingCode() {
		return dialing_code;
	}

	public void setDialingCode(int dialingCode) {
		this.dialing_code = dialingCode;
	}

	public int getPhone() {
		return phone;
	}

	public void setPhone(int phone) {
		this.phone = phone;
	}
}
