package com.cloudjay.cjay.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class SessionResult {

	@Expose
	private long count;
	@Expose
	private String next;
	@Expose
	private String previous;
	@SerializedName("request_time")
	@Expose
	private String requestTime;
	@Expose
	private List<Session> sessions = new ArrayList<Session>();

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public SessionResult withCount(long count) {
		this.count = count;
		return this;
	}

	public String getNext() {
		return next;
	}

	public void setNext(String next) {
		this.next = next;
	}

	public SessionResult withNext(String next) {
		this.next = next;
		return this;
	}

	public String getPrevious() {
		return previous;
	}

	public void setPrevious(String previous) {
		this.previous = previous;
	}

	public SessionResult withPrevious(String previous) {
		this.previous = previous;
		return this;
	}

	public String getRequestTime() {
		return requestTime;
	}

	public void setRequestTime(String requestTime) {
		this.requestTime = requestTime;
	}

	public SessionResult withRequestTime(String requestTime) {
		this.requestTime = requestTime;
		return this;
	}

	public List<Session> getSessions() {
		return sessions;
	}

	public void setSessions(List<Session> sessions) {
		this.sessions = sessions;
	}

	public SessionResult withResults(List<Session> results) {
		this.sessions = results;
		return this;
	}

}
