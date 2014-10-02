package com.cloudjay.cjay.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Operator {

	@Expose
	private long id;
	@SerializedName("operator_code")
	@Expose
	private String operatorCode;
	@SerializedName("operator_name")
	@Expose
	private String operatorName;
	@SerializedName("created_at")
	@Expose
	private String createdAt;
	@SerializedName("modified_at")
	@Expose
	private String modifiedAt;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Operator withId(long id) {
		this.id = id;
		return this;
	}

	public String getOperatorCode() {
		return operatorCode;
	}

	public void setOperatorCode(String operatorCode) {
		this.operatorCode = operatorCode;
	}

	public Operator withOperatorCode(String operatorCode) {
		this.operatorCode = operatorCode;
		return this;
	}

	public String getOperatorName() {
		return operatorName;
	}

	public void setOperatorName(String operatorName) {
		this.operatorName = operatorName;
	}

	public Operator withOperatorName(String operatorName) {
		this.operatorName = operatorName;
		return this;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public Operator withCreatedAt(String createdAt) {
		this.createdAt = createdAt;
		return this;
	}

	public String getModifiedAt() {
		return modifiedAt;
	}

	public void setModifiedAt(String modifiedAt) {
		this.modifiedAt = modifiedAt;
	}

	public Operator withModifiedAt(String modifiedAt) {
		this.modifiedAt = modifiedAt;
		return this;
	}

}