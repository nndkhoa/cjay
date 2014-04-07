package com.cloudjay.cjay.model;

import java.util.Collection;
import com.cloudjay.cjay.dao.ComponentCodeDaoImpl;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Danh sách components.
 * 
 * @author tieubao
 * 
 */
@DatabaseTable(tableName = "component_code", daoClass = ComponentCodeDaoImpl.class)
public class ComponentCode {

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

	public String getName() {
		return display_name;
	}

	public void setName(String name) {
		this.display_name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int operatorId) {
		this.id = operatorId;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
}
