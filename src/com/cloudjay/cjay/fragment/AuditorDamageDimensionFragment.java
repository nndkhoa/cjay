package com.cloudjay.cjay.fragment;

import android.app.Activity;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.listener.OnReportPageCompleteListener;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ViewById;

@EFragment(R.layout.fragment_damage_dimension)
public class AuditorDamageDimensionFragment extends SherlockFragment {
	private double mLength, mHeight;
	private OnReportPageCompleteListener mCallback;
	
	@ViewById(R.id.length) EditText mLengthEditText;
	@ViewById(R.id.height) EditText mHeightEditText;
	
	@AfterViews
	void afterViews() {
		mHeightEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
				if (id == EditorInfo.IME_ACTION_DONE) {
					handleReportPageCompleted();
					return true;
				}
				return false;
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
            mCallback = (OnReportPageCompleteListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnReportPageCompleted");
        }
    }
    
	private void handleReportPageCompleted() {
		// Send code to activity, and move to next tab
		String[] vals = {String.valueOf(mLength), String.valueOf(mHeight)};
		mCallback.onReportPageCompleted(OnReportPageCompleteListener.TAB_DAMAGE_DIMENSION, vals);
	}
}
