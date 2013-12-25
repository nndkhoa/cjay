package com.cloudjay.cjay.model;

import com.cloudjay.cjay.dao.DepotDaoImpl;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "depot", daoClass = DepotDaoImpl.class)
public class Depot {

	public static final String ID = "id";
	public static final String DEPOT_CODE = "depot_code";
	public static final String DEPOT_NAME = "depot_name";

	@DatabaseField(columnName = ID, generatedId = true, allowGeneratedIdInsert = true)
	private Integer id;

	@DatabaseField(columnName = DEPOT_CODE)
	private String depot_code;

	@DatabaseField(columnName = DEPOT_NAME, defaultValue = "")
	private String depot_name;

	@ForeignCollectionField(eager = true)
	private ForeignCollection<Container> containers;

	@ForeignCollectionField(eager = true)
	private ForeignCollection<User> users;

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

	public void setContainers(ForeignCollection<Container> containers) {
		this.containers = containers;
	}

	public ForeignCollection<Container> getContainers() {
		return this.containers;
	}

	public ForeignCollection<User> getUsers() {
		return users;
	}

	public void setUsers(ForeignCollection<User> users) {
		this.users = users;
	}
}
