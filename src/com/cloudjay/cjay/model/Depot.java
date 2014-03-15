package com.cloudjay.cjay.model;

import java.util.Collection;
import com.cloudjay.cjay.dao.DepotDaoImpl;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "depot", daoClass = DepotDaoImpl.class)
public class Depot {

	public static final String ID = "id";
	public static final String DEPOT_CODE = "depot_code";
	public static final String DEPOT_NAME = "depot_name";

	@DatabaseField(columnName = ID, generatedId = true, allowGeneratedIdInsert = true)
	Integer id;

	@DatabaseField(columnName = DEPOT_CODE)
	String depot_code;

	@DatabaseField(columnName = DEPOT_NAME, defaultValue = "")
	String depot_name;

	@ForeignCollectionField(eager = true)
	Collection<Container> containers;

	public void setDepotCode(String depotCode) {
		this.depot_code = depotCode;
	}

	public String getDepotCode() {
		return depot_code;
	}

	public void setDepotName(String depotName) {
		this.depot_name = depotName;
	}

	public String getDepotName() {
		return this.depot_name;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setContainers(Collection<Container> containers) {
		this.containers = containers;
	}

	public Collection<Container> getContainers() {
		return this.containers;
	}

	public Depot() {
	}
}
