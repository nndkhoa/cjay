package com.cloudjay.cjay.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;


@Generated("org.jsonschema2pojo")
public class IsoCode {

	// key = CJayConstant.PREFIX_DAMAGE_CODE + "DB"
	@Expose
	private long id;
	@Expose
	private String code;
	@SerializedName("full_name")
	@Expose
	private String fullName;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public IsoCode withId(long id) {
		this.id = id;
		return this;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public IsoCode withCode(String code) {
		this.code = code;
		return this;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public IsoCode withFullName(String fullName) {
		this.fullName = fullName;
		return this;
	}
}