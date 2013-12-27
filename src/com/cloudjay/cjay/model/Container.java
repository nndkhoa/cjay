package com.cloudjay.cjay.model;

import java.util.Collection;

import org.parceler.Parcel;

import com.cloudjay.cjay.dao.ContainerDaoImpl;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "container", daoClass = ContainerDaoImpl.class)
@Parcel
public class Container {

	public static final String ID = "id";
	public static final String CONTAINER_ID = "container_id";

	public Container(String container_id) {
		this.container_id = container_id;
	}

	public Container() {
	}

	@DatabaseField(columnName = ID, generatedId = true, allowGeneratedIdInsert = true)
	int id;

	@DatabaseField(columnName = CONTAINER_ID)
	String container_id;

	@DatabaseField(canBeNull = false, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
	Operator operator;

	@ForeignCollectionField(eager = true)
	Collection<ContainerSession> containerSessions;

	@DatabaseField(canBeNull = true, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
	Depot depot;

	public Depot getDepot() {
		return depot;
	}

	public void setDepot(Depot depot) {
		this.depot = depot;
	}

	public String getFullContainerId() {
		return operator.getId() + container_id;
	}

	public String getContainerId() {
		return container_id;
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
		this.containerSessions = newSessions;
	}

	public String toString() {
		return operator.getId() + container_id;
	}

}
