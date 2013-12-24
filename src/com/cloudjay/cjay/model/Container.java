package com.cloudjay.cjay.model;

import java.util.Collection;

import com.cloudjay.cjay.dao.ContainerDaoImpl;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "container", daoClass = ContainerDaoImpl.class)
public class Container {

	private static final String ID = "id";
	private static final String CONTAINER_ID = "container_id";

	@DatabaseField(id = true, columnName = ID)
	private int id;

	@DatabaseField(columnName = CONTAINER_ID)
	private String container_id;

	@DatabaseField(canBeNull = false, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
	private Operator operator;

	@ForeignCollectionField(eager = true)
	private ForeignCollection<ContainerSession> containerSessions;

	// @DatabaseField(canBeNull = false, foreign = true, foreignAutoCreate =
	// true, foreignAutoRefresh = true)
	// private Depot depot;
	//
	// public Depot getDepot() {
	// return depot;
	// }
	//
	// public void setDepot(Depot depot) {
	// this.depot = depot;
	// }

	public String getContainerId() {
		return operator.getId() + container_id;
	}

	public void setContainerId(String containerId) {
		this.container_id = containerId;
	}

	public Operator getOperator() {
		return operator;
	}

	public void setOperator(Operator operator) {
		this.operator = operator;
	}

	public Collection<ContainerSession> getContainerSessions() {
		return containerSessions;
	}

	public void setContainerSessions(Collection<ContainerSession> newSessions) {
		this.containerSessions = (ForeignCollection<ContainerSession>) newSessions;
	}

	public String toString() {
		return operator.getId() + container_id;
	}

}
