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

	public Issue() {
		setUuid(UUID.randomUUID().toString());
	}

	public Issue(int id, DamageCode damageCode, RepairCode repairCode, ComponentCode componentCode,
					String location_code, String length, String height, String quantity) {

		this.id = id;
		this.damageCode = damageCode;
		this.repairCode = repairCode;
		this.componentCode = componentCode;
		locationCode = location_code;
		this.length = length;
		this.height = height;
		this.quantity = quantity;
		uuid = UUID.randomUUID().toString();
	}

	public Issue(int id, DamageCode damageCode, RepairCode repairCode, ComponentCode componentCode,
					String location_code, String length, String height, String quantity,
					Collection<CJayImage> cJayImages) {
		this.id = id;
		this.damageCode = damageCode;
		this.repairCode = repairCode;
		this.componentCode = componentCode;
		locationCode = location_code;
		this.length = length;
		this.height = height;
		this.quantity = quantity;
		this.cJayImages = cJayImages;
		uuid = UUID.randomUUID().toString();
	}

	@Override
	public boolean equals(Object o) {

		if (o.getClass() == AuditReportItem.class) {
			AuditReportItem tmp = (AuditReportItem) o;
			
			boolean isEqual = (damageCode != null ? damageCode.getId() : 0) == tmp.getDamageId()
					&& (repairCode != null ? repairCode.getId() : 0) == tmp.getRepairId()
					&& (componentCode != null ? componentCode.getId() : 0) == tmp.getComponentId()
					&& locationCode == tmp.getLocationCode();
			isEqual = isEqual && Integer.parseInt(Utils.replaceNull(quantity, "0")) 
					== Integer.parseInt(Utils.replaceNull(tmp.getQuantity(), "0"));
			isEqual = isEqual && Math.abs((Float.parseFloat(Utils.replaceNull(length, "0")))
					- (Float.parseFloat(Utils.replaceNull(tmp.getLength(), "0")))) < 0.0001;
			isEqual = isEqual && Math.abs((Float.parseFloat(Utils.replaceNull(height, "0"))) 
					- (Float.parseFloat(Utils.replaceNull(tmp.getHeight(), "0")))) < 0.0001;
			return isEqual;
		}

		return super.equals(o);
	}

	public Collection<CJayImage> getCJayImages() {
		return cJayImages;
	}

	public ComponentCode getComponentCode() {
		return componentCode;
	}

	public String getComponentCodeString() {
		if (componentCode != null)
			return componentCode.getCode();
		else
			return null;
	}

	public ContainerSession getContainerSession() {
		return containerSession;
	}

	public DamageCode getDamageCode() {
		return damageCode;
	}

	public String getDamageCodeString() {
		if (damageCode != null)
			return damageCode.getCode();
		else
			return null;
	}

	public String getHeight() {
		return Utils.stripNull(height);
	}

	public int getId() {
		return id;
	}

	public String getLength() {
		return Utils.stripNull(length);
	}

	public String getLocationCode() {
		return locationCode;
	}

	public String getQuantity() {
		return Utils.stripNull(quantity);
	}

	public RepairCode getRepairCode() {
		return repairCode;
	}

	public String getRepairCodeString() {
		if (repairCode != null)
			return repairCode.getCode();
		else
			return null;
	}

	public String getUuid() {
		return uuid;
	}

	public boolean isFixed() {
		return fixed;
	}

	public boolean isValid() {

		if (componentCode == null && repairCode == null && TextUtils.isEmpty(locationCode) && damageCode == null)
			return false;

		return true;

	}

	public void setCJayImages(Collection<CJayImage> cJayImages) {
		this.cJayImages = cJayImages;
	}

	public void setComponentCode(ComponentCode componentCode) {
		this.componentCode = componentCode;
	}

	public void setContainerSession(ContainerSession containerSession) {
		this.containerSession = containerSession;
	}

	public void setDamageCode(DamageCode damageCode) {
		this.damageCode = damageCode;
	}

	public void setFixed(boolean fixed) {
		this.fixed = fixed;
	}

	public void setHeight(String height) {
		this.height = height;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setLength(String length) {
		this.length = length;
	}

	public void setLocationCode(String locationCode) {
		this.locationCode = locationCode;
	}

	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}

	public void setRepairCode(RepairCode repairCode) {
		this.repairCode = repairCode;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
}
