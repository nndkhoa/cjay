package com.cloudjay.cjay.model;

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

}
