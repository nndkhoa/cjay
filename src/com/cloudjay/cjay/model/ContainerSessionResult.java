package com.cloudjay.cjay.model;

import java.util.List;

public class ContainerSessionResult {

	private String requested_time;
	String next;
	List<TmpContainerSession> results;

	public String getNext() {
		return next;
	}

	public List<TmpContainerSession> getResults() {
		return results;
	}

	public void setNext(String next) {
		this.next = next;
	}

	public void setResults(List<TmpContainerSession> results) {
		this.results = results;
	}

	public String getRequestedTime() {
		return requested_time;
	}

	public void setRequestedTime(String requested_time) {
		this.requested_time = requested_time;
	}

}
