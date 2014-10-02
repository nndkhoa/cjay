package com.cloudjay.cjay.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class User {

	@Expose
	private long id;
	@SerializedName("first_name")
	@Expose
	private String firstName;
	@SerializedName("last_name")
	@Expose
	private String lastName;
	@Expose
	private String phone;
	@SerializedName("dialing_code")
	@Expose
	private long dialingCode;
	@Expose
	private String username;
	@Expose
	private String email;
	@SerializedName("full_name")
	@Expose
	private String fullName;
	@SerializedName("avatar_url")
	@Expose
	private String avatarUrl;
	@Expose
	private long role;
	@SerializedName("role_name")
	@Expose
	private String roleName;
	@SerializedName("depot_code")
	@Expose
	private String depotCode;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public User withId(long id) {
		this.id = id;
		return this;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public User withFirstName(String firstName) {
		this.firstName = firstName;
		return this;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public User withLastName(String lastName) {
		this.lastName = lastName;
		return this;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public User withPhone(String phone) {
		this.phone = phone;
		return this;
	}

	public long getDialingCode() {
		return dialingCode;
	}

	public void setDialingCode(long dialingCode) {
		this.dialingCode = dialingCode;
	}

	public User withDialingCode(long dialingCode) {
		this.dialingCode = dialingCode;
		return this;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public User withUsername(String username) {
		this.username = username;
		return this;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public User withEmail(String email) {
		this.email = email;
		return this;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public User withFullName(String fullName) {
		this.fullName = fullName;
		return this;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	public User withAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
		return this;
	}

	public long getRole() {
		return role;
	}

	public void setRole(long role) {
		this.role = role;
	}

	public User withRole(long role) {
		this.role = role;
		return this;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public User withRoleName(String roleName) {
		this.roleName = roleName;
		return this;
	}

	public String getDepotCode() {
		return depotCode;
	}

	public void setDepotCode(String depotCode) {
		this.depotCode = depotCode;
	}

	public User withDepotCode(String depotCode) {
		this.depotCode = depotCode;
		return this;
	}

}