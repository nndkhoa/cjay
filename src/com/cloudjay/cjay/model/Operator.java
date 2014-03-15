package com.cloudjay.cjay.model;

import com.cloudjay.cjay.dao.OperatorDaoImpl;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Danh sách hãng tàu. Dùng để load list operators lúc tạo Container.
 * 
 * @author tieubao
 * 
 */
@DatabaseTable(tableName = "operator", daoClass = OperatorDaoImpl.class)
public class Operator {

	public static final String FIELD_ID = "_id";
	public static final String FIELD_CODE = "operator_code";
	public static final String FIELD_NAME = "operator_name";

	@DatabaseField(id = true, columnName = FIELD_ID)
	int id;

	@DatabaseField(columnName = FIELD_CODE, index = true)
	String operator_code;

	@DatabaseField(columnName = FIELD_NAME)
	String operator_name;

	public String getName() {
		return operator_name;
	}

	public void setName(String name) {
		this.operator_name = name;
	}

	public String getCode() {
		return operator_code;
	}

	public void setCode(String code) {
		this.operator_code = code;
	}

	public int getId() {
		return id;
	}

	public void setId(int operatorId) {
		this.id = operatorId;
	}

	public Operator() {

	}

	public Operator(String operatorCode, String operatorName) {
		this.operator_code = operatorCode;
		this.operator_name = operatorName;
	}
}
