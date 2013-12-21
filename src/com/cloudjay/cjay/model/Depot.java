package com.cloudjay.cjay.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "depot")
public class Depot {

	private static final String ID = "id";

	@DatabaseField(id = true, columnName = ID)
	private int id;

	@ForeignCollectionField(eager = true)
	private ForeignCollection<Container> containers;

}
