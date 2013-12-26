package com.cloudjay.cjay.fragment;

import android.app.Activity;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.listener.OnReportPageCompleteListener;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ViewById;

@EFragment(R.layout.fragment_damage_quantity)
public class AuditorDamageQuantityFragment extends SherlockDialogFragment {
	private int mQuantity;
	private OnReportPageCompleteListener mCallback;
	
	@ViewById(R.id.quantity) EditText mQuantityEditText;
	
	@AfterViews
	void afterViews() {
		mQuantityEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id,
					KeyEvent keyEvent) {
				if (id == EditorInfo.IME_ACTION_DONE) {
					handleReportPageCompleted();
					return true;
				}
				return false;
			}
		});
		mQuantityEditText.requestFocus();
		mQuantity = 0;
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
		String[] vals = {String.valueOf(mQuantity)};
		mCallback.onReportPageCompleted(OnReportPageCompleteListener.TAB_DAMAGE_QUANTITY, vals);
	}
}
