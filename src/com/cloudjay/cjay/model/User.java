package com.cloudjay.cjay.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "user")
public class User {

	@DatabaseField(id = true, columnName = "id")
	private int mId;

	@DatabaseField(columnName = "username")
	private String mUserName;

	@DatabaseField(columnName = "is_main_account")
	private boolean mIsMainAccount;

	@DatabaseField(columnName = "access_token")
	private String mAccessToken;

	@DatabaseField(columnName = "first_name")
	private String mFirstName;

	@DatabaseField(columnName = "last_name")
	private String mLastName;

	@DatabaseField(columnName = "role")
	private String mRole;

	public String getAccessToken() {
		return mAccessToken;
	}

	public void setAccessToken(String accessToken) {
		mAccessToken = accessToken;
	}
}
