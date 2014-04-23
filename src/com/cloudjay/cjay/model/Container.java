package com.cloudjay.cjay.model;

import java.sql.SQLException;
import java.util.Collection;

import com.cloudjay.cjay.dao.ContainerDaoImpl;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "container", daoClass = ContainerDaoImpl.class)
public class Container {

	public static final String ID = "_id";
	public static final String CONTAINER_ID = "container_id";

	@DatabaseField(columnName = ID, generatedId = true, allowGeneratedIdInsert = true)
	int id;

	@DatabaseField(columnName = CONTAINER_ID)
	String container_id;

	@DatabaseField(canBeNull = true, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
	Operator operator;

	@ForeignCollectionField(eager = true)
	Collection<ContainerSession> containerSessions;

	@DatabaseField(canBeNull = true, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
	Depot depot;

	public Container() {
	}

	public Container(String container_id) {
		this.container_id = container_id;
	}

	public String getContainerId() {
		return container_id;
	}

	public Collection<ContainerSession> getContainerSessions() {
		return containerSessions;
	}

	public Depot getDepot() {
		return depot;
	}

	public String getFullContainerId() {
		if (operator != null)
			return operator.getId() + container_id;
		else
			return container_id;
	}

	public int getId() {
		return id;
	}

	public Operator getOperator() {
		return operator;
	}

	public void setContainerId(String containerId) {
		container_id = containerId;
	}

	public void setContainerSessions(Collection<ContainerSession> newSessions) {
		containerSessions = newSessions;
	}

	public void setDepot(Depot depot) {
		this.depot = depot;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setOperator(Operator operator) {
		this.operator = operator;
	}

	@Override
	public String toString() {
		return operator.getId() + container_id;
	}
}
