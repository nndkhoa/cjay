package com.cloudjay.cjay.model;

import com.cloudjay.cjay.dao.OperatorDaoImpl;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "user_log", daoClass = OperatorDaoImpl.class)
public class UserLog {

	public static final String FIELD_ID = "_id";
	public static final String FIELD_CONTENT = "content";

	@DatabaseField(id = true, columnName = FIELD_ID)
	int id;

	@DatabaseField(columnName = FIELD_CONTENT, index = true)
	String content;
}
