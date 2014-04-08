package com.cloudjay.cjay.events;

import java.util.ArrayList;
import java.util.List;

import com.cloudjay.cjay.model.Issue;

/**
 * 
 * Trigger when an issue is repaired.
 * 
 * @author quocvule
 * 
 */

public class IssueRepairedEvent {

	private final List<Issue> listIssues;

	public IssueRepairedEvent(Issue Issue) {
		listIssues = new ArrayList<Issue>();
		listIssues.add(Issue);
	}

	public IssueRepairedEvent(List<Issue> Issues) {
		listIssues = Issues;
	}

	public Issue getTarget() {
		if (isSingleChange())
			return listIssues.get(0);
		else
			throw new IllegalStateException("Can only call this when isSingleChange returns true");
	}

	public List<Issue> getTargets() {
		return listIssues;
	}

	public boolean isSingleChange() {
		return listIssues.size() == 1;
	}

}
