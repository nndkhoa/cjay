package com.cloudjay.cjay.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.cloudjay.cjay.dao.DamageCodeDaoImpl;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Danh sách lỗi hư hỏng.
 * 
 * @author tieubao
 * 
 */
@DatabaseTable(tableName = "damage_code", daoClass = DamageCodeDaoImpl.class)
public class DamageCode implements Parcelable {

	public static final String ID = "id";
	public static final String DISPLAY_NAME = "display_name";
	public static final String CODE = "code";

	@DatabaseField(id = true, columnName = ID)
	int id;

	@DatabaseField(columnName = DISPLAY_NAME)
	String display_name;

	@DatabaseField(columnName = CODE)
	String code;

	@ForeignCollectionField(eager = true)
	Collection<Issue> issues;

	public String getName() {
		return display_name;
	}

	public void setName(String name) {
		this.display_name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public int getId() {
		return id;
	}

	public void setId(int operatorId) {
		this.id = operatorId;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeString(display_name);
		dest.writeString(code);
		parcelCollection(dest, issues);
	}

	private void readFromParcel(Parcel in) {
		this.id = in.readInt();
		this.display_name = in.readString();
		this.code = in.readString();
		this.issues = unparcelCollection(in, Issue.CREATOR);
	}

	public static final Parcelable.Creator<DamageCode> CREATOR = new Parcelable.Creator<DamageCode>() {

		public DamageCode createFromParcel(Parcel source) {
			return new DamageCode(source);
		}

		public DamageCode[] newArray(int size) {
			return new DamageCode[size];
		}
	};

	public DamageCode(Parcel in) {

		readFromParcel(in);
	}

	public DamageCode() {

	}

	void parcelCollection(final Parcel out, final Collection<Issue> collection) {
		if (collection != null) {
			out.writeInt(collection.size());
			out.writeTypedList(new ArrayList<Issue>(collection));
		} else {
			out.writeInt(-1);
		}
	}

	Collection<Issue> unparcelCollection(final Parcel in,
			final Creator<Issue> creator) {
		final int size = in.readInt();

		if (size >= 0) {
			final List<Issue> list = new ArrayList<Issue>(size);
			in.readTypedList(list, creator);
			return list;
		} else {
			return null;
		}
	}
}
