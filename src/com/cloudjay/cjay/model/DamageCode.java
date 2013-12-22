package com.cloudjay.cjay.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Danh sách lỗi hư hỏng.
 * 
 * @author tieubao
 * 
 */
@DatabaseTable(tableName = "damage_code")
public class DamageCode {

	private static final String ID = "id";
	private static final String NAME = "name";
	private static final String CODE = "code";

	@DatabaseField(id = true, columnName = ID)
	private String id;

	@DatabaseField(columnName = NAME)
	private String name;

	@DatabaseField(columnName = CODE)
	private String code;

	// @ForeignCollectionField(eager = true)
	// private ForeignCollection<Issue> issues;

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

	public String getId() {
		return id;
	}

	public void setId(String operatorId) {
		this.id = operatorId;
	}
}
