package com.cloudjay.cjay.model;

import com.google.gson.annotations.Expose;

import javax.annotation.Generated;


@Generated("org.jsonschema2pojo")
public class GateImage{

	@Expose
	private long id;
	@Expose
	private long type;
	@Expose
	private String url;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Expose
    private String name;

    private boolean uploaded;

    public boolean isUploaded() {
        return uploaded;
    }

    public void setUploaded(boolean isUploaded) {
        this.uploaded = isUploaded;
    }

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public GateImage withId(long id) {
		this.id = id;
		return this;
	}

	public long getType() {
		return type;
	}

	public void setType(long type) {
		this.type = type;
	}

	public GateImage withType(long type) {
		this.type = type;
		return this;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public GateImage withUrl(String url) {
		this.url = url;
		return this;
	}

}