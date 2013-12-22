package com.cloudjay.cjay.model;

import java.util.Collection;

import com.cloudjay.cjay.dao.DepotDaoImpl;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "depot", daoClass = DepotDaoImpl.class)
public class Depot {

	private static final String ID = "id";
	private static final String DEPOT_CODE = "depot_code";
	private static final String DEPOT_NAME = "depot_name";

	@DatabaseField(id = true, columnName = ID)
	private int id;

	@DatabaseField(columnName = DEPOT_CODE)
	private String depot_code;

	@DatabaseField(columnName = DEPOT_NAME)
	private String depot_name;

	@ForeignCollectionField(eager = true)
	private ForeignCollection<Container> containers;

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
		this.containers = (ForeignCollection<Container>) containers;
	}

	public ForeignCollection<Container> getContainers() {
		return this.containers;
	}
}
