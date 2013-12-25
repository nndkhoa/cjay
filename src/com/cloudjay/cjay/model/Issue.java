package com.cloudjay.cjay.model;

import java.util.Collection;

import com.cloudjay.cjay.dao.IssueDaoImpl;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "issue", daoClass = IssueDaoImpl.class)
public class Issue {

	private static final String ID = "id";

	@DatabaseField(id = true, columnName = ID)
	private int id;

	@DatabaseField(canBeNull = true, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
	LocationCode locationCode;

	@DatabaseField(canBeNull = true, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
	DamageCode damageCode;

	@DatabaseField(canBeNull = true, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
	RepairCode repairCode;

	@DatabaseField(canBeNull = true, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
	ContainerSession containerSession;

	@ForeignCollectionField(eager = true)
	private ForeignCollection<CJayImage> cJayImages;

	public void setCJayImages(Collection<CJayImage> cJayImages) {
		this.cJayImages = (ForeignCollection<CJayImage>) cJayImages;
	}

	public ForeignCollection<CJayImage> getCJayImages() {
		return cJayImages;
	}
}
