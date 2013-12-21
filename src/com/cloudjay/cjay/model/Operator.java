package com.cloudjay.cjay.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Danh sách hãng tàu. Dùng để load list lúc tạo Container.
 * 
 * @author tieubao
 * 
 */
@DatabaseTable(tableName = "operator")
public class Operator {

	private static final String ID = "id";
	private static final String NAME = "name";

	@DatabaseField(id = true, columnName = ID)
	private String id;

	@DatabaseField(columnName = NAME)
	private String name;

	@ForeignCollectionField(eager = true)
	private ForeignCollection<Container> containers;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String operatorId) {
		this.id = operatorId;
	}
}
