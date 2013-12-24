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

	public static final String ID = "id";
	public static final String CODE = "operator_code";
	public static final String NAME = "operator_name";

	@DatabaseField(id = true, columnName = ID, generatedId = true, allowGeneratedIdInsert = true)
	private int id;

	@DatabaseField(columnName = CODE)
	private String operator_code;

	@DatabaseField(columnName = NAME)
	private String operator_name;

	// @ForeignCollectionField(eager = true)
	// private ForeignCollection<Container> containers;

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

	// public void setContainers(Collection<Container> listContainers) {
	// this.containers = (ForeignCollection<Container>) listContainers;
	// }
	//
	// public Collection<Container> getContainers() {
	// return containers;
	// }
}
