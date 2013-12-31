package com.cloudjay.cjay.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;
import com.cloudjay.cjay.dao.IssueDaoImpl;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "issue", daoClass = IssueDaoImpl.class)
public class Issue implements Parcelable {

	private static final String ID = "id";

	@DatabaseField(id = true, columnName = ID)
	int id;

	@DatabaseField(canBeNull = true)
	String locationCode;

	@DatabaseField(canBeNull = true, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
	DamageCode damageCode;

	@DatabaseField(canBeNull = true, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
	RepairCode repairCode;

	@DatabaseField(canBeNull = true)
	double length;
	
	@DatabaseField(canBeNull = true)
	double height;
	
	@DatabaseField(canBeNull = true)
	int quantity;
	
	@DatabaseField(canBeNull = true, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
	ContainerSession containerSession;

	@ForeignCollectionField(eager = true)
	Collection<CJayImage> cJayImages;

	public Issue(Parcel in) {
		readFromParcel(in);
	}

	public Issue() {

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
	
	public void setDamageCode(DamageCode damageCode) {
		this.damageCode = damageCode;
	}
	
	public DamageCode getDamageCode() {
		return this.damageCode;
	}
	
	public void setLength(double length) {
		this.length = length;
	}
	
	public double getLength() {
		return this.length;
	}
	
	public void setHeight(double height) {
		this.height = height;
	}
	
	public double getHeight() {
		return this.height;
	}
	
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	
	public int getQuantity() {
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
		dest.writeInt(id);
		dest.writeString(locationCode);
		dest.writeParcelable(damageCode, 0);
		dest.writeParcelable(repairCode, 0);
		dest.writeDouble(length);
		dest.writeDouble(height);
		dest.writeInt(quantity);
		dest.writeParcelable(containerSession, 0);
		parcelCollection(dest, cJayImages);
		// dest.writeTypedList((List<CJayImage>) cJayImages);
	}

	private void readFromParcel(Parcel in) {
		this.id = in.readInt();
		this.locationCode = in.readString();
		in.readParcelable(DamageCode.class.getClassLoader());
		in.readParcelable(RepairCode.class.getClassLoader());
		this.length = in.readDouble();
		this.height = in.readDouble();
		this.quantity = in.readInt();
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
}
