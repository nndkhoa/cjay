package com.cloudjay.cjay.model;

import java.sql.Date;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "container")
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

	public String toString() {
		return operator.getId() + container_id;
	}

	// TODO: cần edit -> ContainerSession
	private String containerId;
	private String ownerName;
	private Date date;

	public String getContainerId() {
		return containerId;
	}

	public void setContainerId(String containerId) {
		this.containerId = containerId;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}

}
