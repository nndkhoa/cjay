package com.cloudjay.cjay.model;

import com.cloudjay.cjay.dao.UserLogDaoImpl;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "user_log", daoClass = UserLogDaoImpl.class)
public class UserLog {

	public static final String FIELD_ID = "_id";
	public static final String FIELD_CONTENT = "content";
	public static final String FIELD_TIME = "time";

	@DatabaseField(id = true, columnName = FIELD_ID)
	int id;

	@DatabaseField(columnName = FIELD_CONTENT)
	String content;

	@DatabaseField(columnName = FIELD_TIME)
	String time;
}
