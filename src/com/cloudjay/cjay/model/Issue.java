package com.cloudjay.cjay.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import android.os.Parcel;
import android.os.Parcelable;
import com.cloudjay.cjay.dao.IssueDaoImpl;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "issue", daoClass = IssueDaoImpl.class)
public class Issue implements Parcelable {

	public static final String ID = "id";
	public static final String FIELD_UUID = "uuid";
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

	public Issue(Parcel in) {
		readFromParcel(in);
	}

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

	}

	public Issue() {
		this.setUUID(UUID.randomUUID().toString());
	}

	public void setUUID(String uuid) {
		this.uuid = uuid;
	}

	public String getUUID() {
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
		return this.length;
	}

	public void setHeight(String height) {
		this.height = height;
	}

	public String getHeight() {
		return this.height;
	}

	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}

	public String getQuantity() {
		return this.quantity;
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

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(getId());
		dest.writeString(locationCode);
		dest.writeParcelable(damageCode, 0);
		dest.writeParcelable(repairCode, 0);
		dest.writeString(length);
		dest.writeString(height);
		dest.writeString(quantity);
		dest.writeParcelable(containerSession, 0);
		parcelCollection(dest, cJayImages);
		// dest.writeTypedList((List<CJayImage>) cJayImages);
	}

	private void readFromParcel(Parcel in) {
		this.setId(in.readInt());
		this.locationCode = in.readString();
		in.readParcelable(DamageCode.class.getClassLoader());
		in.readParcelable(RepairCode.class.getClassLoader());
		this.length = in.readString();
		this.height = in.readString();
		this.quantity = in.readString();
		cJayImages = unparcelCollection(in, CJayImage.CREATOR);
		// in.readTypedList(cJayImages, CJayImage.CREATOR);
	}

	public static final Parcelable.Creator<Issue> CREATOR = new Parcelable.Creator<Issue>() {

		public Issue createFromParcel(Parcel source) {
			return new Issue(source);
		}

		public Issue[] newArray(int size) {
			return new Issue[size];
		}
	};

	void parcelCollection(final Parcel out,
			final Collection<CJayImage> collection) {
		if (collection != null) {
			out.writeInt(collection.size());
			out.writeTypedList(new ArrayList<CJayImage>(collection));
		} else {
			out.writeInt(-1);
		}
	}

	Collection<CJayImage> unparcelCollection(final Parcel in,
			final Creator<CJayImage> creator) {
		final int size = in.readInt();

		if (size >= 0) {
			final List<CJayImage> list = new ArrayList<CJayImage>(size);
			in.readTypedList(list, creator);
			return list;
		} else {
			return null;
		}
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
}
