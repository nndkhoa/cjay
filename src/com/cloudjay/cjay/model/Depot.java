package com.cloudjay.cjay.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.cloudjay.cjay.dao.DepotDaoImpl;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "depot", daoClass = DepotDaoImpl.class)
public class Depot implements Parcelable {

	public static final String ID = "id";
	public static final String DEPOT_CODE = "depot_code";
	public static final String DEPOT_NAME = "depot_name";

	@DatabaseField(columnName = ID, generatedId = true, allowGeneratedIdInsert = true)
	Integer id;

	@DatabaseField(columnName = DEPOT_CODE)
	String depot_code;

	@DatabaseField(columnName = DEPOT_NAME, defaultValue = "")
	String depot_name;

	@ForeignCollectionField(eager = true)
	Collection<Container> containers;

	// @ForeignCollectionField(eager = true)
	// Collection<User> users;

	public void setDepotCode(String depotCode) {
		this.depot_code = depotCode;
	}

	public String getDepotCode() {
		return depot_code;
	}

	public void setDepotName(String depotName) {
		this.depot_name = depotName;
	}

	public String getDepotName() {
		return this.depot_name;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setContainers(Collection<Container> containers) {
		this.containers = containers;
	}

	public Collection<Container> getContainers() {
		return this.containers;
	}

	// public Collection<User> getUsers() {
	// return users;
	// }
	//
	// public void setUsers(Collection<User> users) {
	// this.users = users;
	// }

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeString(depot_name);
		dest.writeString(depot_code);
		parcelCollection(dest, containers);
	}

	private void readFromParcel(Parcel in) {
		this.id = in.readInt();
		this.depot_name = in.readString();
		this.depot_code = in.readString();
		this.containers = unparcelCollection(in, Container.CREATOR);
	}

	public static final Parcelable.Creator<Depot> CREATOR = new Parcelable.Creator<Depot>() {

		public Depot createFromParcel(Parcel source) {
			return new Depot(source);
		}

		public Depot[] newArray(int size) {
			return new Depot[size];
		}
	};

	public Depot(Parcel in) {
		readFromParcel(in);
	}

	public Depot() {
	}

	void parcelCollection(final Parcel out,
			final Collection<Container> collection) {
		if (collection != null) {
			out.writeInt(collection.size());
			out.writeTypedList(new ArrayList<Container>(collection));
		} else {
			out.writeInt(-1);
		}
	}

	Collection<Container> unparcelCollection(final Parcel in,
			final Creator<Container> creator) {
		final int size = in.readInt();

		if (size >= 0) {
			final List<Container> list = new ArrayList<Container>(size);
			in.readTypedList(list, creator);
			return list;
		} else {
			return null;
		}
	}
}
