package com.cloudjay.cjay.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.TextView;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.adapter.IsoCodeAdapter;
import com.cloudjay.cjay.event.isocode.IsoCodeGotEvent;
import com.cloudjay.cjay.event.isocode.IsoCodesGotEvent;
import com.cloudjay.cjay.listener.AuditorIssueReportListener;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.model.IsoCode;
import com.cloudjay.cjay.task.command.isocode.GetIsoCodeCommand;
import com.cloudjay.cjay.task.command.isocode.GetListIsoCodesCommand;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.droidparts.widget.ClearableEditText;
import org.droidparts.widget.ClearableEditText.Listener;

import java.util.List;

import de.greenrobot.event.EventBus;

@EFragment(R.layout.fragment_report_issue_component)
public class IssueReportComponentFragment extends IssueReportFragment {

	private AuditorIssueReportListener mCallback;
	private AuditItem mAuditItem;

    private String mComponentCode;
	private String mComponentName;

	private boolean ignoreSearch;

	@ViewById(R.id.component_name)
	ClearableEditText mComponentEditText;

	@ViewById(R.id.lv_component)
	ListView mComponentListView;

	@SystemService
	InputMethodManager inputMethodManager;

    @Bean
    DataCenter mDataCenter;

	private int mItemLayout = R.layout.item_code;

	IsoCodeAdapter mAdapter;
    List<IsoCode> componentCodes;

	@AfterViews
	void afterViews() {

		mComponentEditText.addTextChangedListener(new TextWatcher() {
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

		mComponentEditText.setListener(new Listener() {
			@Override
			public void didClearText() {
                mComponentCode = "";
				mComponentName = "";
			}
		});

        // refresh component list
        mDataCenter.add(new GetListIsoCodesCommand(getActivity().getApplicationContext(),
                CJayConstant.PREFIX_COMPONENT_CODE));
	}

	@ItemClick(R.id.lv_component)
    void isoCodeListViewItemClicked(int position) {

        IsoCode item = mAdapter.getItem(position);
        Logger.e(position + " " + item.getId() + " " + item.getCode());
        mComponentCode = item.getCode();
        mComponentName = item.getFullName();
        ignoreSearch = true;
        mComponentEditText.setText(mComponentName);
        ignoreSearch = false;

        // hide keyboard
        inputMethodManager.hideSoftInputFromWindow(mComponentEditText.getWindowToken(), 0);

        // move to next tab
        mCallback.onReportPageCompleted(AuditorIssueReportListener.TAB_ISSUE_COMPONENT);
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
		if (!TextUtils.isEmpty(mComponentCode)) {
			mComponentEditText.setError(null);
			mCallback.onReportValueChanged(AuditorIssueReportListener.TYPE_COMPONENT_CODE, String.valueOf(mComponentCode));
			return true;

		} else {
			mComponentEditText.setError(getString(R.string.issue_code_missing_warning));
			return false;
		}
	}

    void updateData(List<IsoCode> isoCodes) {
        if (isoCodes != null) {
            mAdapter = new IsoCodeAdapter(getActivity().getApplicationContext(),
                    mItemLayout, componentCodes);

            mComponentListView.setAdapter(mAdapter);
            mComponentListView.setTextFilterEnabled(true);
            mComponentListView.setScrollingCacheEnabled(false);

            mAdapter.notifyDataSetChanged();
        }
    }

	@UiThread
	public void onEvent(IsoCodeGotEvent event) {
		IsoCode componentCode = event.getIsoCode();
		if (event.getPrefix().equals(CJayConstant.PREFIX_COMPONENT_CODE)) {
			if (componentCode != null) {
				mComponentCode = componentCode.getCode();
				mComponentName = componentCode.getFullName();
			} else {
				mComponentCode = "";
				mComponentName = "";
			}

			ignoreSearch = true;
			mComponentEditText.setText(mComponentName);
			ignoreSearch = false;
		}
	}

    @UiThread
    public void onEvent(IsoCodesGotEvent event) {

        String prefix = event.getPrefix();

        if (prefix.equals(CJayConstant.PREFIX_COMPONENT_CODE)) {

            componentCodes = event.getListIsoCodes();
            updateData(componentCodes);

            // initialize with issue
            if (mAuditItem != null && mAuditItem.getComponentCode() != null) {
                mDataCenter.add(new GetIsoCodeCommand(getActivity().getApplicationContext(),
                        CJayConstant.PREFIX_COMPONENT_CODE, mAuditItem.getComponentCode()));
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
