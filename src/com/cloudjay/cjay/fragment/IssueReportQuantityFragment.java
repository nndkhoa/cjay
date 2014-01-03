package com.cloudjay.cjay.fragment;

import android.app.Activity;
import android.content.Context;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.listener.AuditorIssueReportListener;
import com.cloudjay.cjay.model.Issue;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ViewById;

@EFragment(R.layout.fragment_issue_quantity)
public class IssueReportQuantityFragment extends IssueReportFragment {
	private AuditorIssueReportListener mCallback;
	private Issue mIssue;

	@ViewById(R.id.quantity)
	EditText mQuantityEditText;

	@AfterViews
	void afterViews() {
		mQuantityEditText
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == EditorInfo.IME_ACTION_DONE) {
							// move to next tab
							mCallback
									.onReportPageCompleted(AuditorIssueReportListener.TAB_ISSUE_QUANTITY);

							// hide keyboard
							InputMethodManager imm = (InputMethodManager) getActivity()
									.getSystemService(
											Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(
									mQuantityEditText.getWindowToken(), 0);
							return true;
						}
						return false;
					}
				});

		// initialize with issue
		if (mIssue != null) {
			mQuantityEditText.setText(mIssue.getQuantity());
		}

		mQuantityEditText.requestFocus();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mCallback = (AuditorIssueReportListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnReportPageCompleted");
		}
	}

	@Override
	public void setIssue(Issue issue) {
		mIssue = issue;
	}

	@Override
	public void validateAndSaveData() {
		// save data
		mCallback.onReportValueChanged(
				AuditorIssueReportListener.TYPE_QUANTITY, mQuantityEditText
						.getText().toString());
	}

	@Override
	public void showKeyboard() {
		// show keyboard
		InputMethodManager imm = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(mQuantityEditText, 0);
	}
	
	@Override
	public void hideKeyboard() {
		// show keyboard
		InputMethodManager imm = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mQuantityEditText.getWindowToken(), 0);
	}
}