package com.cloudjay.cjay.model;

import java.util.List;

public class ContainerSessionResult {
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

}
