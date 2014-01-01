package com.cloudjay.cjay.fragment;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.listener.AuditorIssueReportListener;
import com.cloudjay.cjay.model.Issue;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ViewById;

@EFragment(R.layout.fragment_issue_quantity)
public class AuditorDamageQuantityFragment extends SherlockFragment {
	private int mQuantity;
	private boolean mQuantityChanged;
	private AuditorIssueReportListener mCallback;
	private Issue mIssue;
	
	@ViewById(R.id.quantity) EditText mQuantityEditText;
	
	@AfterViews
	void afterViews() {
		mQuantityEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id,
					KeyEvent keyEvent) {
				if (id == EditorInfo.IME_ACTION_DONE) {
					// move to next tab
					mCallback.onReportPageCompleted(AuditorIssueReportListener.TAB_DAMAGE_QUANTITY);
					return true;
				}
				return false;
			}
		});
		mQuantityEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus && mQuantityChanged) {
					// save data
					mQuantity = Integer.parseInt(mQuantityEditText.getText().toString());
					mCallback.onReportValueChanged(AuditorIssueReportListener.TYPE_QUANTITY, String.valueOf(mQuantity));
				}
			}
		});
		mQuantityEditText.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void afterTextChanged(Editable s) {
				mQuantityChanged = true;
			}
		});
		mQuantityEditText.requestFocus();
		mQuantity = 0;
		mQuantityChanged = false;
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
}
