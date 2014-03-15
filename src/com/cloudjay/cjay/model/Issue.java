package com.cloudjay.cjay.model;

import java.util.Collection;
import java.util.UUID;

import android.text.TextUtils;

import com.cloudjay.cjay.dao.IssueDaoImpl;
import com.cloudjay.cjay.util.Utils;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "issue", daoClass = IssueDaoImpl.class)
public class Issue {

	public static final String ID = "id";
	public static final String FIELD_UUID = "_id";
	public static final String FIELD_FIXED = "fixed";

	@DatabaseField(columnName = ID, defaultValue = "0")
	int id;

	@DatabaseField(columnName = FIELD_UUID, id = true)
	String uuid;

	@DatabaseField(canBeNull = true)
	String locationCode;

	@DatabaseField(canBeNull = true, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
	DamageCode damageCode;

	@DatabaseField(canBeNull = true, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
	RepairCode repairCode;

	@DatabaseField(canBeNull = true, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
	ComponentCode componentCode;

	@DatabaseField(canBeNull = true)
	String length;

	@DatabaseField(canBeNull = true)
	String height;

	@DatabaseField(canBeNull = true)
	String quantity;

	@DatabaseField(columnName = FIELD_FIXED, canBeNull = true, defaultValue = "false")
	boolean fixed;

	@DatabaseField(canBeNull = true, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
	ContainerSession containerSession;

	@ForeignCollectionField(eager = true)
	Collection<CJayImage> cJayImages;

	public Issue(int id, DamageCode damageCode, RepairCode repairCode,
			ComponentCode componentCode, String location_code, String length,
			String height, String quantity, Collection<CJayImage> cJayImages) {
		this.id = id;
		this.damageCode = damageCode;
		this.repairCode = repairCode;
		this.componentCode = componentCode;
		this.locationCode = location_code;
		this.length = length;
		this.height = height;
		this.quantity = quantity;
		this.cJayImages = cJayImages;
		this.uuid = UUID.randomUUID().toString();
	}

	public Issue(int id, DamageCode damageCode, RepairCode repairCode,
			ComponentCode componentCode, String location_code, String length,
			String height, String quantity) {

		this.id = id;
		this.damageCode = damageCode;
		this.repairCode = repairCode;
		this.componentCode = componentCode;
		this.locationCode = location_code;
		this.length = length;
		this.height = height;
		this.quantity = quantity;
		this.uuid = UUID.randomUUID().toString();
	}

	public Issue() {
		this.setUuid(UUID.randomUUID().toString());
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getUuid() {
		return this.uuid;
	}

	public void setFixed(boolean fixed) {
		this.fixed = fixed;
	}

	public boolean isFixed() {
		return fixed;
	}

	public void setLocationCode(String locationCode) {
		this.locationCode = locationCode;
	}

	public String getLocationCode() {
		return this.locationCode;
	}

	public void setRepairCode(RepairCode repairCode) {
		this.repairCode = repairCode;
	}

	public RepairCode getRepairCode() {
		return this.repairCode;
	}

	public String getRepairCodeString() {
		if (this.repairCode != null) {
			return this.repairCode.getCode();
		} else {
			return null;
		}
	}

	public void setComponentCode(ComponentCode componentCode) {
		this.componentCode = componentCode;
	}

	public ComponentCode getComponentCode() {
		return this.componentCode;
	}

	public String getComponentCodeString() {
		if (this.componentCode != null) {
			return this.componentCode.getCode();
		} else {
			return null;
		}
	}

	public void setDamageCode(DamageCode damageCode) {
		this.damageCode = damageCode;
	}

	public DamageCode getDamageCode() {
		return this.damageCode;
	}

	public String getDamageCodeString() {
		if (this.damageCode != null) {
			return this.damageCode.getCode();
		} else {
			return null;
		}
	}

	public void setLength(String length) {
		this.length = length;
	}

	public String getLength() {
		return Utils.stripNull(this.length);
	}

	public void setHeight(String height) {
		this.height = height;
	}

	public String getHeight() {
		return Utils.stripNull(this.height);
	}

	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}

	public String getQuantity() {
		return Utils.stripNull(this.quantity);
	}

	public void setContainerSession(ContainerSession containerSession) {
		this.containerSession = containerSession;
	}

	public ContainerSession getContainerSession() {
		return this.containerSession;
	}

	public void setCJayImages(Collection<CJayImage> cJayImages) {
		this.cJayImages = cJayImages;
	}

	public Collection<CJayImage> getCJayImages() {
		return cJayImages;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object o) {

		if (o.getClass() == AuditReportItem.class) {
			AuditReportItem tmp = (AuditReportItem) o;

			boolean isEqual = this.damageCode.getId() == tmp.getDamageId()
					&& this.repairCode.getId() == tmp.getRepairId()
					&& this.getComponentCode().getId() == tmp.getComponentId()
					&& Float.parseFloat(this.length) == Float.parseFloat(tmp
							.getLength())
					&& Float.parseFloat(this.height) == Float.parseFloat(tmp
							.getHeight())
					&& Integer.parseInt(this.quantity) == Integer.parseInt(tmp
							.getQuantity());

			return isEqual;
		}

		return super.equals(o);
	}

	public boolean isValid() {

		if (componentCode == null && repairCode == null
				&& TextUtils.isEmpty(locationCode) && damageCode == null) {
			return false;
		}

		return true;

	}
}
