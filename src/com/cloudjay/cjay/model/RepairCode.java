package com.cloudjay.cjay.model;

import java.util.Collection;

import com.cloudjay.cjay.dao.RepairCodeDaoImpl;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Danh sách lỗi sửa chữa.
 * 
 * @author tieubao
 * 
 */
@DatabaseTable(tableName = "repair_code", daoClass = RepairCodeDaoImpl.class)
public class RepairCode {

	public static final String ID = "id";
	public static final String DISPLAY_NAME = "display_name";
	public static final String CODE = "code";

	@DatabaseField(id = true, columnName = ID)
	int id;

	@DatabaseField(columnName = DISPLAY_NAME)
	String display_name;

	@DatabaseField(columnName = CODE, index = true)
	String code;

	@ForeignCollectionField(eager = true)
	Collection<Issue> issues;

	public String getCode() {
		return code;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return display_name;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public void setId(int operatorId) {
		id = operatorId;
	}

	public void setName(String name) {
		display_name = name;
	}
}
