package com.cloudjay.cjay.model;

import java.util.List;

public class ContainerSessionResult {
	String next;

	public String getNext() {
		return next;
	}

	public void setNext(String next) {
		this.next = next;
	}

	List<TmpContainerSession> results;

	public List<TmpContainerSession> getResults() {
		return results;
	}

	public void setResults(List<TmpContainerSession> results) {
		this.results = results;
	}

}
