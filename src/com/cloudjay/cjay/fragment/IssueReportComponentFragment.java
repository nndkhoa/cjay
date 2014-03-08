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
import com.cloudjay.cjay.model.ComponentCode;
import com.cloudjay.cjay.model.Issue;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.Utils;

@EFragment(R.layout.fragment_issue_component_code)
public class IssueReportComponentFragment extends IssueReportFragment  {
	private AuditorIssueReportListener mCallback;
	private Issue mIssue;

	private ArrayList<ComponentCode> mComponents;
	private FunDapter<ComponentCode> mComponentsAdapter;
	private String mComponentCode;
	private String mComponentName;
	private boolean ignoreSearch;
	
	@ViewById(R.id.component_name) ClearableEditText mComponentEditText;
	@ViewById(R.id.component_list) ListView mComponentListView;
	
	@AfterViews
	void afterViews() {
		mComponentEditText.addTextChangedListener(new TextWatcher() {
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
		mComponentEditText.setListener(new Listener() {
			@Override
			public void didClearText() {
				mComponentCode = "";
				mComponentName = "";
			}
		});
		
		mComponents = (ArrayList<ComponentCode>) DataCenter.getInstance().getListComponents(getActivity());
		initComponentsAdapter(mComponents);
		
		// initialize with issue
		if (mIssue != null && mIssue.getComponentCode() != null) {
			mComponentCode = mIssue.getComponentCode().getCode();
			mComponentName = mIssue.getComponentCode().getName();
		} else {
			mComponentCode = "";
			mComponentName = "";
		}
		ignoreSearch = true;
		mComponentEditText.setText(mComponentName);
		ignoreSearch = false;
	}
	
	@ItemClick(R.id.component_list)
	void componentItemClicked(int position) {
		mComponentCode = mComponentsAdapter.getItem(position).getCode();
		mComponentName = mComponentsAdapter.getItem(position).getName();
		ignoreSearch = true;
		mComponentEditText.setText(mComponentName);
		ignoreSearch = false;
		
		// hide keyboard
		hideKeyboard();
		
		// move to next tab
		mCallback.onReportPageCompleted(AuditorIssueReportListener.TAB_ISSUE_COMPONENT);
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
		if (!TextUtils.isEmpty(mComponentCode)) {
			mComponentEditText.setError(null);
			mCallback.onReportValueChanged(AuditorIssueReportListener.TYPE_COMPONENT_CODE, mComponentCode);
			return true;
			
		} else {
			mComponentEditText.setError(getString(R.string.issue_code_missing_warning));	
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
		imm.hideSoftInputFromWindow(mComponentEditText.getWindowToken(), 0);
	}
	
	private void search(String searchText) {
		if (searchText.equals("") || ignoreSearch) {
			mComponentsAdapter.updateData(mComponents);
		} else {
			ArrayList<ComponentCode> searchFeeds = new ArrayList<ComponentCode>();
			for (ComponentCode component : mComponents) {
				if (component.getName().toLowerCase(Locale.US).contains(searchText.toLowerCase(Locale.US)) ||
					component.getCode().toLowerCase(Locale.US).contains(searchText.toLowerCase(Locale.US))) {
					
					searchFeeds.add(component);
				}
			}
			// refresh list
			mComponentsAdapter.updateData(searchFeeds);
		}
	}

	private void initComponentsAdapter(ArrayList<ComponentCode> components) {
		BindDictionary<ComponentCode> componentDict = new BindDictionary<ComponentCode>();
		componentDict.addStringField(R.id.name,
				new StringExtractor<ComponentCode>() {
					@Override
					public String getStringValue(ComponentCode item, int position) {
						return Utils.replaceNullBySpace(item.getName());
					}
				});
		mComponentsAdapter = new FunDapter<ComponentCode>(getActivity(), components,
				R.layout.list_item_issue_code, componentDict);
		mComponentListView.setAdapter(mComponentsAdapter);
	}
}
