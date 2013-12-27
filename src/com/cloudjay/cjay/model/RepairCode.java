package com.cloudjay.cjay.model;

import java.util.Collection;

import org.parceler.Parcel;

import com.cloudjay.cjay.dao.RepairCodeDaoImpl;
import com.j256.ormlite.dao.ForeignCollection;
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
@Parcel
public class RepairCode {

	private static final String ID = "id";
	private static final String NAME = "name";
	private static final String CODE = "code";

	@DatabaseField(id = true, columnName = ID)
	int id;

	@DatabaseField(columnName = NAME)
	String name;

	@DatabaseField(columnName = CODE)
	String code;

	@ForeignCollectionField(eager = true)
	Collection<Issue> issues;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public int getId() {
		return id;
	}

	public void setId(int operatorId) {
		this.id = operatorId;
	}
}
