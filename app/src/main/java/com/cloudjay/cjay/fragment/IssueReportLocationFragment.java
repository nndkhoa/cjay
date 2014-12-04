package com.cloudjay.cjay.fragment;

import android.app.Activity;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.listener.AuditorIssueReportListener;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.util.Utils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

@EFragment(R.layout.fragment_report_issue_location)
public class IssueReportLocationFragment extends IssueReportFragment implements OnFocusChangeListener, OnClickListener {
	private String mLocationCodes[], mCodes[][];
	private AuditorIssueReportListener mCallback;
	private Button mCodeButtons[];
	private EditText mCodeEditTexts[];
	private EditText mFocusedEditText;
	private int mCurrentStep;
	private AuditItem mAuditItem;

	@ViewById(R.id.locationCode0)
	EditText mCode0EditText;
	@ViewById(R.id.locationCode1)
	EditText mCode1EditText;
	@ViewById(R.id.locationCode2)
	EditText mCode2EditText;
	@ViewById(R.id.locationCode3)
	EditText mCode3EditText;

	@ViewById(R.id.btnCode0)
	Button mCode0Button;
	@ViewById(R.id.btnCode1)
	Button mCode1Button;
	@ViewById(R.id.btnCode2)
	Button mCode2Button;
	@ViewById(R.id.btnCode3)
	Button mCode3Button;
	@ViewById(R.id.btnCode4)
	Button mCode4Button;
	@ViewById(R.id.btnCode5)
	Button mCode5Button;
	@ViewById(R.id.btnCode6)
	Button mCode6Button;
	@ViewById(R.id.btnCode7)
	Button mCode7Button;
	@ViewById(R.id.btnCode8)
	Button mCode8Button;
	@ViewById(R.id.btnCode9)
	Button mCode9Button;
	@ViewById(R.id.btnCode10)
	Button mCode10Button;
	@ViewById(R.id.btnCode11)
	Button mCode11Button;

    @ViewById(R.id.tv_code_fullname)
    TextView mLocationCodeTextView;

    @ViewById(R.id.tv_code_label)
    TextView mCodeLabelTextView;

	@AfterViews
	void AfterViews() {

        // Set text code label
        mCodeLabelTextView.setText(R.string.label_code_location);

		mCodeButtons = new Button[12];
		mCodeButtons[0] = mCode0Button;
		mCodeButtons[1] = mCode1Button;
		mCodeButtons[2] = mCode2Button;
		mCodeButtons[3] = mCode3Button;
		mCodeButtons[4] = mCode4Button;
		mCodeButtons[5] = mCode5Button;
		mCodeButtons[6] = mCode6Button;
		mCodeButtons[7] = mCode7Button;
		mCodeButtons[8] = mCode8Button;
		mCodeButtons[9] = mCode9Button;
		mCodeButtons[10] = mCode10Button;
		mCodeButtons[11] = mCode11Button;
		for (int i = 0; i < mCodeButtons.length; i++) {
			mCodeButtons[i].setOnClickListener(this);
		}

		mCodeEditTexts = new EditText[4];
		mCodeEditTexts[0] = mCode0EditText;
		mCodeEditTexts[1] = mCode1EditText;
		mCodeEditTexts[2] = mCode2EditText;
		mCodeEditTexts[3] = mCode3EditText;
		for (int i = 0; i < mCodeEditTexts.length; i++) {
			mCodeEditTexts[i].setInputType(InputType.TYPE_NULL);
			mCodeEditTexts[i].setOnFocusChangeListener(this);
		}

		mCodes = new String[4][];
		mCodes[0] = getResources().getStringArray(R.array.damage_location_code_0);
		mCodes[1] = getResources().getStringArray(R.array.damage_location_code_1);
		mCodes[2] = getResources().getStringArray(R.array.damage_location_code_2);
		mCodes[3] = getResources().getStringArray(R.array.damage_location_code_3);

		mFocusedEditText = null;
		mCurrentStep = -1;
		mLocationCodes = new String[4];

		// initialize with issue
		if (mAuditItem != null) {
			if (mAuditItem.getLocationCode() != null && mAuditItem.getLocationCode().length() == 4) {
				for (int i = 0; i < mLocationCodes.length; i++) {
					mLocationCodes[i] = mAuditItem.getLocationCode().substring(i, i + 1);
					mCodeEditTexts[i].setText(mLocationCodes[i]);
				}
			}
		}

        mLocationCodeTextView.setText(mAuditItem.getLocationCode());

		mCode1EditText.requestFocus();
	}

	private void configureControls(int step) {
		for (int i = 0; i < mCodeButtons.length; i++) {
			mCodeButtons[i].setText(mCodes[step][i]);
			mCodeButtons[i].setVisibility(mCodes[step][i].equals("") ? View.INVISIBLE : View.VISIBLE);
		}
		LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) mCodeButtons[5].getLayoutParams();
		switch (step) {
			case 0:
			case 1:
				p.weight = 0;
				mCodeButtons[5].setLayoutParams(p);
				mCodeButtons[11].setLayoutParams(p);
				break;
			case 2:
			case 3:
			default:
				p.weight = 1;
				mCodeButtons[5].setLayoutParams(p);
				mCodeButtons[11].setLayoutParams(p);
				break;
		}
	}

	private void goToNextInput(int step, String code) {
		if (step < mCodeEditTexts.length - 1) {
			// move to the next edit text
			mCodeEditTexts[step + 1].requestFocus();
		} else if (step == mCodeEditTexts.length - 1) {
			// move to the next tab
			mCallback.onReportPageCompleted(AuditorIssueReportListener.TAB_ISSUE_LOCATION);
		}
	}

	@Override
	public void hideKeyboard() {
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
		Button btn = (Button) v;
		String code = (String) btn.getText();
		if (mFocusedEditText != null && !code.equals("")) {
			mFocusedEditText.setText(code);
			mLocationCodes[mCurrentStep] = code;
            mLocationCodeTextView.setText(new StringBuilder()
                    .append(Utils.stripNull(mLocationCodes[0]))
                    .append(Utils.stripNull(mLocationCodes[1]))
                    .append(Utils.stripNull(mLocationCodes[2]))
                    .append(Utils.stripNull(mLocationCodes[3])).toString());
			goToNextInput(mCurrentStep, code);
		}
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (hasFocus && v != null) {
			mFocusedEditText = (EditText) v;
			mCurrentStep = Integer.parseInt(mFocusedEditText.getTag().toString());
			configureControls(mCurrentStep);
		}
	}

	@Override
	public void setAuditItem(AuditItem auditItem) {
		mAuditItem = auditItem;
	}

	@Override
	public void showKeyboard() {
	}

	@Override
	public boolean validateAndSaveData() {
		// save location code
		String locationCode = new StringBuilder()
                .append(Utils.stripNull(mLocationCodes[0]))
                .append(Utils.stripNull(mLocationCodes[1]))
                .append(Utils.stripNull(mLocationCodes[2]))
                .append(Utils.stripNull(mLocationCodes[3])).toString();

		if (locationCode.length() == 4) {
			mCode3EditText.setError(null);
			mCallback.onReportValueChanged(AuditorIssueReportListener.TYPE_LOCATION_CODE, locationCode);
			return true;

		} else {
			mCode3EditText.setError(getString(R.string.issue_code_missing_warning));
			return false;
		}
	}
}