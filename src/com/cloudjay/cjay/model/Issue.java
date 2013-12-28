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

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeParcelable(locationCode, 0);
		dest.writeParcelable(damageCode, 0);
		dest.writeParcelable(repairCode, 0);
		dest.writeParcelable(containerSession, 0);
		parcelCollection(dest, cJayImages);
		// dest.writeTypedList((List<CJayImage>) cJayImages);
	}

	private void readFromParcel(Parcel in) {
		this.id = in.readInt();
		in.readParcelable(LocationCode.class.getClassLoader());
		in.readParcelable(DamageCode.class.getClassLoader());
		in.readParcelable(RepairCode.class.getClassLoader());
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

	public Issue(Parcel in) {

		readFromParcel(in);
	}

	public Issue() {

	}

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
