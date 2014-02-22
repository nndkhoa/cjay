package com.cloudjay.cjay.model;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

import com.cloudjay.cjay.dao.OperatorDaoImpl;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Danh sách hãng tàu. Dùng để load list operators lúc tạo Container.
 * 
 * @author tieubao
 * 
 */
@SuppressLint("ParcelCreator")
@DatabaseTable(tableName = "operator", daoClass = OperatorDaoImpl.class)
public class Operator implements Parcelable {

	public static final String ID = "id";
	public static final String CODE = "operator_code";
	public static final String NAME = "operator_name";

	@DatabaseField(id = true, columnName = ID)
	int id;

	@DatabaseField(columnName = CODE, index = true)
	String operator_code;

	@DatabaseField(columnName = NAME)
	String operator_name;

	// @ForeignCollectionField(eager = true)
	// private ForeignCollection<Container> containers;

	public String getName() {
		return operator_name;
	}

	public void setName(String name) {
		this.operator_name = name;
	}

	public String getCode() {
		return operator_code;
	}

	public void setCode(String code) {
		this.operator_code = code;
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
		dest.writeString(operator_code);
		dest.writeString(operator_name);
	}

	private void readFromParcel(Parcel in) {
		this.id = in.readInt();
		this.operator_code = in.readString();
		this.operator_name = in.readString();
	}

	public static final Parcelable.Creator<Operator> CREATOR = new Parcelable.Creator<Operator>() {
		public Operator createFromParcel(Parcel source) {
			return new Operator(source);
		}

		public Operator[] newArray(int size) {
			return new Operator[size];
		}
	};

	public Operator(Parcel in) {
		readFromParcel(in);
	}

	public Operator() {

	}

	public Operator(String operatorCode, String operatorName) {
		this.operator_code = operatorCode;
		this.operator_name = operatorName;
	}

	// public void setContainers(Collection<Container> listContainers) {
	// this.containers = (ForeignCollection<Container>) listContainers;
	// }
	//
	// public Collection<Container> getContainers() {
	// return containers;
	// }
}
