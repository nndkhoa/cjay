package com.cloudjay.cjay.fragment;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.listener.OnReportPageCompleted;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ViewById;

@EFragment(R.layout.fragment_damage_repair_code)
public class AuditorDamageRepairFragment extends SherlockDialogFragment implements OnClickListener {
	private String mRepairCode;
	private OnReportPageCompleted mCallback;
	private Button mCodeButtons[];
	private Drawable mDefaultBackground;
	
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
	}
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (OnReportPageCompleted) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnHeadlineSelectedListener");
        }
    }

	@Override
	public void onClick(View v) {
		Button btnCode = (Button)v;
		for (int i = 0; i < mCodeButtons.length; i++) {
			mCodeButtons[i].setBackgroundDrawable(mDefaultBackground);
		}
		btnCode.setBackgroundResource(R.drawable.btn_code_selected);
		
		mRepairCode = (String)btnCode.getText();		
		handleReportPageCompleted();
	}
	
	private void handleReportPageCompleted() {
		// Send code to activity, and move to next tab
		String[] vals = {mRepairCode};
		mCallback.onReportPageCompleted(OnReportPageCompleted.TAB_DAMAGE_REPAIR, vals);
	}
}
