package com.cloudjay.cjay.fragment;

import java.util.ArrayList;
import java.util.Locale;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ViewById;
import org.droidparts.widget.ClearableEditText;
import org.droidparts.widget.ClearableEditText.Listener;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;

import com.ami.fundapter.BindDictionary;
import com.ami.fundapter.FunDapter;
import com.ami.fundapter.extractors.StringExtractor;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.listener.AuditorIssueReportListener;
import com.cloudjay.cjay.model.Issue;
import com.cloudjay.cjay.model.RepairCode;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.Utils;

@EFragment(R.layout.fragment_issue_repair_code)
public class IssueReportRepairFragment extends IssueReportFragment  {
	private AuditorIssueReportListener mCallback;
	private Issue mIssue;

	private ArrayList<RepairCode> mRepairCodes;
	private FunDapter<RepairCode> mRepairsAdapter;
	private String mRepairCode;
	private String mRepairName;
	private boolean ignoreSearch;
	
	@ViewById(R.id.repair_name) ClearableEditText mRepairEditText;
	@ViewById(R.id.repair_list) ListView mRepairListView;
	
	@AfterViews
	void afterViews() {
		mRepairEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable arg0) {
				search(arg0.toString());
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		});
		mRepairEditText.setListener(new Listener() {
			@Override
			public void didClearText() {
				mRepairCode = "";
				mRepairName = "";
			}
		});
		
		mRepairCodes = (ArrayList<RepairCode>) DataCenter.getInstance().getListRepairCodes(getActivity());
		initRepairsAdapter(mRepairCodes);
		
		// initialize with issue
		if (mIssue != null && mIssue.getRepairCode() != null) {
			mRepairCode = mIssue.getRepairCode().getCode();
			mRepairName = mIssue.getRepairCode().getName();
		} else {
			mRepairCode = "";
			mRepairName = "";
		}
		ignoreSearch = true;
		mRepairEditText.setText(mRepairName);
		ignoreSearch = false;
	}
	
	@ItemClick(R.id.repair_list)
	void repairItemClicked(int position) {
		mRepairCode = mRepairsAdapter.getItem(position).getCode();
		mRepairName = mRepairsAdapter.getItem(position).getName();
		ignoreSearch = true;
		mRepairEditText.setText(mRepairName);
		ignoreSearch = false;
		
		// hide keyboard
		hideKeyboard();
		
		// move to next tab
		mCallback.onReportPageCompleted(AuditorIssueReportListener.TAB_ISSUE_REPAIR);
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
	public void setIssue(Issue issue) {
		mIssue = issue;
	}

	@Override
	public boolean validateAndSaveData() {
		if (!TextUtils.isEmpty(mRepairCode)) {
			mRepairEditText.setError(null);			
			mCallback.onReportValueChanged(AuditorIssueReportListener.TYPE_REPAIR_CODE, mRepairCode);
			return true;
			
		} else {
			mRepairEditText.setError(getString(R.string.issue_code_missing_warning));
			return false;
		}
	}
	
	@Override
	public void showKeyboard() {
	}
	
	@Override
	public void hideKeyboard() {
		// hide keyboard
		InputMethodManager imm = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mRepairEditText.getWindowToken(), 0);
	}
	
	private void search(String searchText) {
		if (searchText.equals("") || ignoreSearch) {
			mRepairsAdapter.updateData(mRepairCodes);
		} else {
			ArrayList<RepairCode> searchFeeds = new ArrayList<RepairCode>();
			for (RepairCode repairCode : mRepairCodes) {
				if (repairCode.getName().toLowerCase(Locale.US).contains(searchText.toLowerCase(Locale.US)) ||
					repairCode.getCode().toLowerCase(Locale.US).contains(searchText.toLowerCase(Locale.US))) {
					
					searchFeeds.add(repairCode);
				}
			}
			// refresh list
			mRepairsAdapter.updateData(searchFeeds);
		}
	}

	private void initRepairsAdapter(ArrayList<RepairCode> repairCodes) {
		BindDictionary<RepairCode> repairDict = new BindDictionary<RepairCode>();
		repairDict.addStringField(R.id.name,
				new StringExtractor<RepairCode>() {
					@Override
					public String getStringValue(RepairCode item, int position) {
						return Utils.replaceNullBySpace(item.getName());
					}
				});
		mRepairsAdapter = new FunDapter<RepairCode>(getActivity(), repairCodes,
				R.layout.list_item_issue_code, repairDict);
		mRepairListView.setAdapter(mRepairsAdapter);
	}
}
