package com.cloudjay.cjay.model;

import com.cloudjay.cjay.dao.UserDaoImpl;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "user", daoClass = UserDaoImpl.class)
public class User {

	public static final int ROLE_GATE_KEEPER = 6;
	public static final int ROLE_AUDITOR = 1;
	public static final int ROLE_REPAIR_STAFF = 4;

	public static final String ID = "id";
	public static final String USERNAME = "username";
	public static final String EMAIL = "email";
	public static final String IS_MAIN_ACCOUNT = "is_main_account";
	public static final String ACCESS_TOKEN = "access_token";
	public static final String FIRST_NAME = "first_name";
	public static final String LAST_NAME = "last_name";
	public static final String FULL_NAME = "full_name";
	public static final String ROLE_NAME = "role_name";
	public static final String ROLE = "role";
	public static final String AVATAR_URL = "avatar_url";
	public static final String DIALING_CODE = "dialing_code";
	public static final String PHONE = "phone";
	public static final String EXPIRE = "expire_in";

	@DatabaseField(id = true, columnName = ID)
	int id;

	@DatabaseField(columnName = USERNAME)
	String username;

	@DatabaseField(columnName = EMAIL)
	String email;

	@DatabaseField(columnName = IS_MAIN_ACCOUNT, defaultValue = "0")
	boolean is_main_account;

	@DatabaseField(columnName = ACCESS_TOKEN)
	String access_token;

	@DatabaseField(columnName = FIRST_NAME)
	String first_name;

	@DatabaseField(columnName = LAST_NAME)
	String last_name;

	@DatabaseField(columnName = FULL_NAME)
	String full_name;

	@DatabaseField(columnName = ROLE_NAME)
	String role_name;

	@DatabaseField(columnName = ROLE, canBeNull = false)
	int role;

	@DatabaseField(columnName = AVATAR_URL)
	String avatar_url;

	@DatabaseField(columnName = DIALING_CODE)
	int dialing_code;

	@DatabaseField(columnName = PHONE)
	int phone;

	@DatabaseField(columnName = EXPIRE)
	int expire_in;

	@DatabaseField(canBeNull = true, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
	private Depot depot;

	// use for json parsing purpose
	private String depot_code;

	public String getAccessToken() {
		return access_token;
	}

	public String getAvatar() {
		return avatar_url;
	}

	public Depot getDepot() {
		return depot;
	}

	public String getDepotCode() {
		return depot_code;
	}

	public int getDialingCode() {
		return dialing_code;
	}

	public String getEmail() {
		return email;
	}

	public int getExpire() {
		return expire_in;
	}

	public int getFilterStatus() {
		int result = 0;

		switch (role) {
			case User.ROLE_GATE_KEEPER:
				result = 5; // repaired containers
				break;

			case User.ROLE_AUDITOR:
				result = 0; // checked in containers
				break;

			case User.ROLE_REPAIR_STAFF:
				result = 4; // repair confirmed containers
				// result = 1; // temporary
				break;

			default:
				break;
		}
		return result;
	}

	public String getFirstName() {
		return first_name;
	}

	public String getFullName() {
		return full_name;
	}

	public int getID() {
		return id;
	}

	public String getLastName() {
		return last_name;
	}

	public int getPhone() {
		return phone;
	}

	public int getRole() {
		return role;
	}

	public String getRoleName() {
		return role_name;
	}

	public String getUserName() {
		return username;
	}

	public boolean isMainAccount() {
		return is_main_account;
	}

	public void setAccessToken(String accessToken) {
		access_token = accessToken;
	}

	public void setAvatar(String avatarUrl) {
		avatar_url = avatarUrl;
	}

	public void setDepot(Depot depot) {
		this.depot = depot;
	}

	public void setDepotCode(String depot_code) {
		this.depot_code = depot_code;
	}

	public void setDialingCode(int dialingCode) {
		dialing_code = dialingCode;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setExpire(int day) {
		expire_in = day;
	}

	public void setFirstName(String firstName) {
		first_name = firstName;
	}

	public void setFullName(String fullName) {
		full_name = fullName;
	}

	public void setID(int id) {
		this.id = id;
	}

	public void setLastName(String lastName) {
		last_name = lastName;
	}

	public void setMainAccount(boolean isMainAccount) {
		is_main_account = isMainAccount;
	}

	public void setPhone(int phone) {
		this.phone = phone;
	}

	public void setRole(int role) {
		this.role = role;
	}

	public void setRoleName(String roleName) {
		role_name = roleName;
	}

	public void setUserName(String userName) {
		username = userName;
	}
}
