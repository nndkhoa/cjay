package com.cloudjay.cjay.fragment;

import android.app.Activity;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockFragment;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.listener.AuditorIssueReportListener;
import com.cloudjay.cjay.model.Issue;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ViewById;

@EFragment(R.layout.fragment_issue_dimension)
public class AuditorDamageDimensionFragment extends SherlockFragment {
	private double mLength, mHeight;
	private AuditorIssueReportListener mCallback;
	private Issue mIssue;
	
	@ViewById(R.id.length) EditText mLengthEditText;
	@ViewById(R.id.height) EditText mHeightEditText;
	
	@AfterViews
	void afterViews() {
		mHeightEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					handleReportPageCompleted();
				}
			}
		});
		mLengthEditText.requestFocus();
		mLength = 0f;
		mHeight = 0f;
	}
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (AuditorIssueReportListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnReportPageCompleted");
        }
    }
	
	public void setIssue(Issue issue) {
		mIssue = issue;
	}
    
	private void handleReportPageCompleted() {
		// Send code to activity, and move to next tab
		String[] vals = {String.valueOf(mLength), String.valueOf(mHeight)};
		mCallback.onReportPageCompleted(AuditorIssueReportListener.TAB_DAMAGE_DIMENSION);
	}
}
