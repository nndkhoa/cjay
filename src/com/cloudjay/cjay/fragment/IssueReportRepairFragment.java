package com.cloudjay.cjay.fragment;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.listener.AuditorIssueReportListener;
import com.cloudjay.cjay.model.Issue;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ViewById;

@EFragment(R.layout.fragment_issue_repair_code)
public class IssueReportRepairFragment extends IssueReportFragment implements OnClickListener {
	private String mRepairCode;
	private AuditorIssueReportListener mCallback;
	private Button mCodeButtons[];
	private Drawable mDefaultBackground;
	private Issue mIssue;
	
	@ViewById(R.id.btnCode0) Button mCode0Button;
	@ViewById(R.id.btnCode1) Button mCode1Button;
	@ViewById(R.id.btnCode2) Button mCode2Button;
	@ViewById(R.id.btnCode3) Button mCode3Button;
	@ViewById(R.id.btnCode4) Button mCode4Button;
	@ViewById(R.id.btnCode5) Button mCode5Button;
	
	@AfterViews
	void afterViews() {
		mCodeButtons = new Button[6];
		mCodeButtons[0] = mCode0Button;
		mCodeButtons[1] = mCode1Button;
		mCodeButtons[2] = mCode2Button;
		mCodeButtons[3] = mCode3Button;
		mCodeButtons[4] = mCode4Button;
		mCodeButtons[5] = mCode5Button;
		for (int i = 0; i < mCodeButtons.length; i++) {
			mCodeButtons[i].setOnClickListener(this);
		}
		mDefaultBackground = mCode0Button.getBackground();
		mRepairCode = "";
		
		// initialize with issue
		if (mIssue != null) {
			Button selectedBtn = null;
			mRepairCode = mIssue.getRepairCodeString();
			
			for (Button btn : mCodeButtons) {
				if (btn.getText().toString().equals(mRepairCode)) {
					selectedBtn = btn;
					break;
				}
			}
			if (selectedBtn != null) {
				highlightButton(selectedBtn);				
			}
		}
	}
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (AuditorIssueReportListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement AuditorIssueReportListener");
        }
    }

	@Override
	public void onClick(View v) {
		Button btnCode = (Button)v;
		highlightButton(btnCode);		
		mRepairCode = (String)btnCode.getText();
		mCallback.onReportPageCompleted(AuditorIssueReportListener.TAB_ISSUE_REPAIR);
	}
	
	@Override
	public void setIssue(Issue issue) {
		mIssue = issue;
	}

	@Override
	public void validateAndSaveData() {
		mCallback.onReportValueChanged(AuditorIssueReportListener.TYPE_REPAIR_CODE, mRepairCode);
	}
	
	private void highlightButton(Button btn) {
		for (int i = 0; i < mCodeButtons.length; i++) {
			mCodeButtons[i].setBackgroundDrawable(mDefaultBackground);
		}
		btn.setBackgroundResource(R.drawable.btn_code_selected);
	}
	
	@Override
	public void showKeyboard() {
	}
}
