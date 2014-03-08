package com.cloudjay.cjay.fragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

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

@EFragment(R.layout.fragment_issue_dimension)
public class IssueReportDimensionFragment extends IssueReportFragment {
	private AuditorIssueReportListener mCallback;
	private Issue mIssue;

	@ViewById(R.id.length)
	EditText mLengthEditText;
	@ViewById(R.id.height)
	EditText mHeightEditText;

	@AfterViews
	void afterViews() {
		mHeightEditText
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == EditorInfo.IME_ACTION_DONE) {
							// move to next tab
							mCallback
									.onReportPageCompleted(AuditorIssueReportListener.TAB_ISSUE_DIMENSION);
							return true;
						}
						return false;
					}
				});

		// initialize with issue
		if (mIssue != null) {
			mLengthEditText.setText(mIssue.getLength());
			mHeightEditText.setText(mIssue.getHeight());
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mCallback = (AuditorIssueReportListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement AuditorIssueReportListener");
		}
	}

	@Override
	public void setIssue(Issue issue) {
		mIssue = issue;
	}

	@Override
	public boolean validateAndSaveData() {
		// save data
		mCallback.onReportValueChanged(AuditorIssueReportListener.TYPE_LENGTH,
				mLengthEditText.getText().toString());
		mCallback.onReportValueChanged(AuditorIssueReportListener.TYPE_HEIGHT,
				mHeightEditText.getText().toString());
		return true;
	}
	
	@Override
	public void showKeyboard() {
		// show keyboard
		mLengthEditText.requestFocus();
		InputMethodManager imm = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(mLengthEditText, 0);
	}
	
	@Override
	public void hideKeyboard() {
		// show keyboard
		InputMethodManager imm = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mLengthEditText.getWindowToken(), 0);
	}
}
