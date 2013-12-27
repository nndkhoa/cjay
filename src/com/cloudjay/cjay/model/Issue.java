package com.cloudjay.cjay.model;

import java.util.Collection;

import org.parceler.Parcel;

import com.cloudjay.cjay.dao.IssueDaoImpl;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "issue", daoClass = IssueDaoImpl.class)
@Parcel
public class Issue {

	private static final String ID = "id";

	@DatabaseField(id = true, columnName = ID)
	int id;

	@DatabaseField(canBeNull = true, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
	LocationCode locationCode;

	@DatabaseField(canBeNull = true, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
	DamageCode damageCode;

	@DatabaseField(canBeNull = true, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
	RepairCode repairCode;

	@DatabaseField(canBeNull = true, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
	ContainerSession containerSession;

	@ForeignCollectionField(eager = true)
	Collection<CJayImage> cJayImages;

	public void setCJayImages(Collection<CJayImage> cJayImages) {
		this.cJayImages = cJayImages;
	}

	public Collection<CJayImage> getCJayImages() {
		return cJayImages;
	}
}
