package com.cloudjay.cjay.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;
import com.cloudjay.cjay.dao.ContainerDaoImpl;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "container", daoClass = ContainerDaoImpl.class)
public class Container implements Parcelable {

	public static final String ID = "id";
	public static final String CONTAINER_ID = "container_id";

	public Container(String container_id) {
		this.container_id = container_id;
	}

	public Container() {
	}

	@DatabaseField(columnName = ID, generatedId = true, allowGeneratedIdInsert = true)
	int id;

	@DatabaseField(columnName = CONTAINER_ID)
	String container_id;

	@DatabaseField(canBeNull = true, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
	Operator operator;

	@ForeignCollectionField(eager = true)
	Collection<ContainerSession> containerSessions;

	@DatabaseField(canBeNull = true, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
	Depot depot;

	public Depot getDepot() {
		return depot;
	}

	public void setDepot(Depot depot) {
		this.depot = depot;
	}

	public String getFullContainerId() {
		return operator.getId() + container_id;
	}

	public String getContainerId() {
		return container_id;
	}

	public void setContainerId(String containerId) {
		this.container_id = containerId;
	}

	public Operator getOperator() {
		return operator;
	}

	public void setOperator(Operator operator) {
		this.operator = operator;
	}

	public Collection<ContainerSession> getContainerSessions() {
		return containerSessions;
	}

	public void setContainerSessions(Collection<ContainerSession> newSessions) {
		this.containerSessions = newSessions;
	}

	public String toString() {
		return operator.getId() + container_id;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeString(container_id);
		dest.writeParcelable(operator, 0);
		dest.writeParcelable(depot, 0);
		parcelCollection(dest, containerSessions);
	}

	private void readFromParcel(Parcel in) {
		this.id = in.readInt();
		this.container_id = in.readString();
		in.readParcelable(Operator.class.getClassLoader());
		in.readParcelable(Depot.class.getClassLoader());
		containerSessions = unparcelCollection(in, ContainerSession.CREATOR);
	}

	public static final Parcelable.Creator<Container> CREATOR = new Parcelable.Creator<Container>() {

		public Container createFromParcel(Parcel source) {
			return new Container(source);
		}

		public Container[] newArray(int size) {
			return new Container[size];
		}
	};

	public Container(Parcel in) {

		readFromParcel(in);
	}

	void parcelCollection(final Parcel out,
			final Collection<ContainerSession> collection) {
		if (collection != null) {
			out.writeInt(collection.size());
			out.writeTypedList(new ArrayList<ContainerSession>(collection));
		} else {
			out.writeInt(-1);
		}
	}

	Collection<ContainerSession> unparcelCollection(final Parcel in,
			final Creator<ContainerSession> creator) {
		final int size = in.readInt();

		if (size >= 0) {
			final List<ContainerSession> list = new ArrayList<ContainerSession>(
					size);
			in.readTypedList(list, creator);
			return list;
		} else {
			return null;
		}
	}
}
