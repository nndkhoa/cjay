package com.cloudjay.cjay.fragment;

import android.support.v4.app.Fragment;

import com.cloudjay.cjay.model.AuditItem;

public abstract class IssueReportFragment extends Fragment {
	public abstract void hideKeyboard();

	public abstract void setAuditItem(AuditItem auditItem);

	public abstract void showKeyboard();

	public abstract boolean validateAndSaveData();

}
