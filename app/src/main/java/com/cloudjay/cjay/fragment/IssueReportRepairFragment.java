package com.cloudjay.cjay.fragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.ViewById;
import org.droidparts.widget.ClearableEditText;
import org.droidparts.widget.ClearableEditText.Listener;

import android.app.Activity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.TextView;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.adapter.IsoCodeAdapter;
import com.cloudjay.cjay.listener.AuditorIssueReportListener;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.model.IsoCode;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;

import java.util.List;

@EFragment(R.layout.fragment_report_issue_repair)
public class IssueReportRepairFragment extends IssueReportFragment {

	private AuditorIssueReportListener mCallback;
	private AuditItem mAuditItem;

	private String mRepairCode;
	private String mRepairName;
	private boolean ignoreSearch;

	@ViewById(R.id.repair_name)
	ClearableEditText mRepairEditText;

	@ViewById(R.id.lv_repair)
	ListView mRepairListView;

    @ViewById(R.id.tv_code_fullname)
    TextView mRepairNameTextView;

	@SystemService
	InputMethodManager inputMethodManager;

    @Bean
    DataCenter mDataCenter;

    private int mItemLayout = R.layout.item_code;

    IsoCodeAdapter mAdapter;

	@AfterViews
	void afterViews() {

		mRepairEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable arg0) {
				search(arg0.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
		});

		mRepairEditText.setListener(new Listener() {
			@Override
			public void didClearText() {
				mRepairCode = "";
				mRepairName = "";
                mRepairNameTextView.setText("");
			}
		});

		// initialize with issue
        IsoCode repairCode = null;
        if (mAuditItem != null && mAuditItem.getRepairCode() != null) {
            repairCode = mDataCenter.getIsoCode(getActivity().getApplicationContext(),
                    CJayConstant.PREFIX_REPAIR_CODE,
                    mAuditItem.getRepairCode());
        }
        if (repairCode != null) {
            mRepairCode = repairCode.getCode();
            mRepairName = repairCode.getFullName();
		} else {
            mRepairCode = "";
			mRepairName = "";
		}

		ignoreSearch = true;
		mRepairEditText.setText(mRepairName);
        mRepairNameTextView.setText(mRepairName);
		ignoreSearch = false;

        // refresh repair list
        List<IsoCode> repairCodes = mDataCenter.getListIsoCodes(getActivity().getApplicationContext(),
                CJayConstant.PREFIX_REPAIR_CODE);
        mAdapter = new IsoCodeAdapter(getActivity().getApplicationContext(), mItemLayout, repairCodes);

        mRepairListView.setAdapter(mAdapter);
        mRepairListView.setTextFilterEnabled(true);
        mRepairListView.setScrollingCacheEnabled(false);

        mAdapter.notifyDataSetChanged();
	}

	@Override
	public void hideKeyboard() {
		// hide keyboard

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

    @ItemClick(R.id.lv_repair)
    void isoCodeListViewItemClicked(int position) {

        IsoCode item = mAdapter.getItem(position);
        Logger.e(position + " " + item.getId() + " " + item.getCode());
        mRepairCode = item.getCode();
        mRepairName = item.getFullName();
        ignoreSearch = true;
        mRepairEditText.setText(mRepairName);
        mRepairNameTextView.setText(mRepairName);
        ignoreSearch = false;

        // hide keyboard
        inputMethodManager.hideSoftInputFromWindow(mRepairEditText.getWindowToken(), 0);

        // move to next tab
        mCallback.onReportPageCompleted(AuditorIssueReportListener.TAB_ISSUE_REPAIR);
    }

	private void search(String searchText) {
		if (searchText.equals("") || ignoreSearch) {

		} else {
			if (mAdapter != null) {
				mAdapter.getFilter().filter(searchText);
			}
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
		if (!TextUtils.isEmpty(mRepairCode)) {
			mRepairEditText.setError(null);
			mCallback.onReportValueChanged(AuditorIssueReportListener.TYPE_REPAIR_CODE, String.valueOf(mRepairCode));
			return true;

		} else {
			mRepairEditText.setError(getString(R.string.issue_code_missing_warning));
			return false;
		}
	}
}
