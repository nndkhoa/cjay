package com.cloudjay.cjay.model;

import java.util.List;

public class ContainerSessionResult {
	int next;
	List<TmpContainerSession> results;

	public int getNext() {
		return next;
	}

	public void setNext(int next) {
		this.next = next;
	}

	public List<TmpContainerSession> getResults() {
		return results;
	}

	public void setResults(List<TmpContainerSession> results) {
		this.results = results;
	}

}
