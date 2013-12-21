package com.cloudjay.cjay.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "issue")
public class Issue {

	private static final String ID = "id";

	@DatabaseField(id = true, columnName = ID)
	private int id;

	@DatabaseField
	LocationCode locationCode;

	@DatabaseField
	DamageCode damageCode;

	@DatabaseField
	RepairCode repairCode;

}
