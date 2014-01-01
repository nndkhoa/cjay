package com.cloudjay.cjay.fragment;

import com.cloudjay.cjay.model.Issue;

import com.actionbarsherlock.app.SherlockFragment;

public abstract class AuditorIssueReportFragment extends SherlockFragment {
	public abstract void setIssue(Issue issue);
	public abstract void validateAndSaveData();
}
