package com.cloudjay.cjay.fragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.droidparts.widget.ClearableEditText;
import org.droidparts.widget.ClearableEditText.Listener;

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
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;

import java.util.List;

import de.greenrobot.event.EventBus;

@EFragment(R.layout.fragment_report_issue_damage)
public class IssueReportDamageFragment extends IssueReportFragment {

	private AuditorIssueReportListener mCallback;
	private AuditItem mAuditItem;

	private String mDamageCode;
	private String mDamageName;

	private boolean ignoreSearch;

	@ViewById(R.id.damage_name)
	ClearableEditText mDamageEditText;

	@ViewById(R.id.lv_damage)
	ListView mDamageListView;

    @ViewById(R.id.tv_code_fullname)
    TextView mDamageNameTextView;

    @ViewById(R.id.tv_code_label)
    TextView mCodeLabelTextView;

	@SystemService
	InputMethodManager inputMethodManager;

    @Bean
    DataCenter mDataCenter;

	private int mItemLayout = R.layout.item_code;

    IsoCodeAdapter mAdapter;
    List<IsoCode> damageCodes;

	@AfterViews
	void afterViews() {

        // Set text code label
        mCodeLabelTextView.setText(R.string.label_code_damage);

		mDamageEditText.addTextChangedListener(new TextWatcher() {
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

		mDamageEditText.setListener(new Listener() {

			@Override
			public void didClearText() {
				mDamageCode = "";
				mDamageName = "";
                mDamageNameTextView.setText("");
			}
		});

        // refresh damage list
        mDataCenter.getListIsoCodes(getActivity().getApplicationContext(),
                CJayConstant.PREFIX_DAMAGE_CODE);
	}

    @ItemClick(R.id.lv_damage)
    void isoCodeListViewItemClicked(int position) {

        IsoCode item = mAdapter.getItem(position);
        Logger.e(position + " " + item.getId() + " " + item.getCode());
        mDamageCode = item.getCode();
        mDamageName = item.getFullName();
        ignoreSearch = true;
        mDamageEditText.setText(mDamageName);
        mDamageNameTextView.setText(mDamageName);
        ignoreSearch = false;

        // hide keyboard
        inputMethodManager.hideSoftInputFromWindow(mDamageEditText.getWindowToken(), 0);

        // move to next tab
        mCallback.onReportPageCompleted(AuditorIssueReportListener.TAB_ISSUE_DAMAGE);
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
			// do nothing
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
		// TODO Auto-generated method stub

	}

	@Override
	public boolean validateAndSaveData() {
		if (!TextUtils.isEmpty(mDamageCode)) {
			mDamageEditText.setError(null);
			mCallback.onReportValueChanged(AuditorIssueReportListener.TYPE_DAMAGE_CODE, String.valueOf(mDamageCode));
			return true;

		} else {
			mDamageEditText.setError(getString(R.string.issue_code_missing_warning));
			return false;
		}
	}

    @UiThread
    public void onEvent(IsoCodeGotEvent event) {
        Logger.Log("event damageCode");
        IsoCode damageCode = event.getIsoCode();

        if (event.getPrefix().equals(CJayConstant.PREFIX_DAMAGE_CODE)) {
            if (damageCode != null) {
                mDamageCode = damageCode.getCode();
                mDamageName = damageCode.getFullName();
            } else {
                mDamageCode = "";
                mDamageName = "";
            }

            ignoreSearch = true;
            mDamageEditText.setText(mDamageName);
            mDamageNameTextView.setText(mDamageName);
            ignoreSearch = false;
        }
    }

    void updateData(List<IsoCode> isoCodes) {
        if (isoCodes != null) {
            mAdapter = new IsoCodeAdapter(getActivity().getApplicationContext(),
                    mItemLayout, damageCodes);

            mDamageListView.setAdapter(mAdapter);
            mDamageListView.setTextFilterEnabled(true);
            mDamageListView.setScrollingCacheEnabled(false);

            mAdapter.notifyDataSetChanged();
        }
    }

    @UiThread
    public void onEvent(IsoCodesGotEvent event) {

        String prefix = event.getPrefix();
        if (prefix.equals(CJayConstant.PREFIX_DAMAGE_CODE)) {
            damageCodes = event.getListIsoCodes();
            updateData(damageCodes);

            // initialize with issue
            if (mAuditItem != null && mAuditItem.getComponentCode() != null) {
                mDataCenter.getIsoCode(getActivity().getApplicationContext(),
                        CJayConstant.PREFIX_DAMAGE_CODE, mAuditItem.getDamageCode());
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
