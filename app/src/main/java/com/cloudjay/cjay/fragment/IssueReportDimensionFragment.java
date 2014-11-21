package com.cloudjay.cjay.fragment;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.listener.AuditorIssueReportListener;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.util.Logger;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

@EFragment(R.layout.fragment_report_issue_dimension)
public class IssueReportDimensionFragment extends IssueReportFragment {
	private AuditorIssueReportListener mCallback;
	private AuditItem mAuditItem;

	@ViewById(R.id.length)
	EditText mLengthEditText;

	@ViewById(R.id.height)
	EditText mHeightEditText;

    @ViewById(R.id.quantity)
    EditText mQuantityEditText;

    @ViewById(R.id.tv_length)
    TextView mLengthTextView;

    @ViewById(R.id.tv_height)
    TextView mHeightTextView;

    @ViewById(R.id.tv_quantity)
    TextView mQuantityTextView;

	@AfterViews
	void afterViews() {
//		mHeightEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//			@Override
//			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
//				if (id == EditorInfo.IME_ACTION_DONE) {
//					// move to next tab
//					mCallback.onReportPageCompleted(AuditorIssueReportListener.TAB_ISSUE_DIMENSION);
//					return true;
//				}
//				return false;
//			}
//		});

        mLengthEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mLengthTextView.setText(editable.toString());
            }
        });

        mHeightEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mHeightTextView.setText(editable.toString());
            }
        });

        mQuantityEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mQuantityTextView.setText(editable.toString());
            }
        });

        mQuantityEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE) {
                    // move to next tab
                    mCallback.onReportPageCompleted(AuditorIssueReportListener.TAB_ISSUE_DIMENSION);

                    // hide keyboard
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mQuantityEditText.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });

		// initialize with issue
		if (mAuditItem != null) {
			mLengthEditText.setText(String.valueOf(mAuditItem.getLength()));
			mHeightEditText.setText(String.valueOf(mAuditItem.getHeight()));
            mQuantityEditText.setText(String.valueOf(mAuditItem.getQuantity()));

            mLengthTextView.setText(String.valueOf(mAuditItem.getLength()));
            mHeightTextView.setText(String.valueOf(mAuditItem.getHeight()));
            mQuantityTextView.setText(String.valueOf(mAuditItem.getQuantity()));
		}
	}

	@Override
	public void hideKeyboard() {
		// show keyboard
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mLengthEditText.getWindowToken(), 0);
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
	public void setAuditItem(AuditItem auditItem) {
		mAuditItem = auditItem;
	}

	@Override
	public void showKeyboard() {
		// show keyboard
		mLengthEditText.requestFocus();
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(mLengthEditText, 0);
	}

	@Override
	public boolean validateAndSaveData() {
		// save data
		mCallback.onReportValueChanged(AuditorIssueReportListener.TYPE_LENGTH, mLengthEditText.getText().toString());
		mCallback.onReportValueChanged(AuditorIssueReportListener.TYPE_HEIGHT, mHeightEditText.getText().toString());
        mCallback.onReportValueChanged(AuditorIssueReportListener.TYPE_QUANTITY, mQuantityEditText.getText().toString());
		return true;
	}
}
