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
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;

import com.ami.fundapter.BindDictionary;
import com.ami.fundapter.FunDapter;
import com.ami.fundapter.extractors.StringExtractor;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.listener.AuditorIssueReportListener;
import com.cloudjay.cjay.model.DamageCode;
import com.cloudjay.cjay.model.Issue;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.Utils;

@EFragment(R.layout.fragment_issue_damage_code)
public class IssueReportDamageFragment extends IssueReportFragment  {
	private AuditorIssueReportListener mCallback;
	private Issue mIssue;

	private ArrayList<DamageCode> mDamageCodes;
	private FunDapter<DamageCode> mDamagesAdapter;
	private String mDamageCode;
	private String mDamageName;
	private boolean ignoreSearch;
	
	@ViewById(R.id.damage_name) ClearableEditText mDamageEditText;
	@ViewById(R.id.damage_list) ListView mDamageListView;
	
	@AfterViews
	void afterViews() {
		mDamageEditText.addTextChangedListener(new TextWatcher() {
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
		mDamageEditText.setListener(new Listener() {
			@Override
			public void didClearText() {
				mDamageCode = "";
				mDamageName = "";
			}
		});
		
		mDamageCodes = (ArrayList<DamageCode>) DataCenter.getInstance().getListDamageCodes(getActivity());
		initDamagesAdapter(mDamageCodes);
		
		// initialize with issue
		if (mIssue != null && mIssue.getDamageCode() != null) {
			mDamageCode = mIssue.getDamageCode().getCode();
			mDamageName = mIssue.getDamageCode().getName();
		} else {
			mDamageCode = "";
			mDamageName = "";
		}
		ignoreSearch = true;
		mDamageEditText.setText(mDamageName);
		ignoreSearch = false;
	}
	
	@ItemClick(R.id.damage_list)
	void damageItemClicked(int position) {
		mDamageCode = mDamagesAdapter.getItem(position).getCode();
		mDamageName = mDamagesAdapter.getItem(position).getName();
		ignoreSearch = true;
		mDamageEditText.setText(mDamageName);
		ignoreSearch = false;
		
		// hide keyboard
		hideKeyboard();
		
		// move to next tab
		mCallback.onReportPageCompleted(AuditorIssueReportListener.TAB_ISSUE_DAMAGE);
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
	public void validateAndSaveData() {
		// save data
		mCallback.onReportValueChanged(AuditorIssueReportListener.TYPE_DAMAGE_CODE, mDamageCode);		
	}
	
	@Override
	public void showKeyboard() {
	}
	
	@Override
	public void hideKeyboard() {
		// hide keyboard
		InputMethodManager imm = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mDamageEditText.getWindowToken(), 0);
	}
	
	private void search(String searchText) {
		if (searchText.equals("") || ignoreSearch) {
			mDamagesAdapter.updateData(mDamageCodes);
		} else {
			ArrayList<DamageCode> searchFeeds = new ArrayList<DamageCode>();
			for (DamageCode damageCode : mDamageCodes) {
				if (damageCode.getName().toLowerCase(Locale.US).contains(searchText.toLowerCase(Locale.US)) ||
					damageCode.getCode().toLowerCase(Locale.US).contains(searchText.toLowerCase(Locale.US))) {
					
					searchFeeds.add(damageCode);
				}
			}
			// refresh list
			mDamagesAdapter.updateData(searchFeeds);
		}
	}

	private void initDamagesAdapter(ArrayList<DamageCode> damageCodes) {
		BindDictionary<DamageCode> damageDict = new BindDictionary<DamageCode>();
		damageDict.addStringField(R.id.name,
				new StringExtractor<DamageCode>() {
					@Override
					public String getStringValue(DamageCode item, int position) {
						return Utils.stripNull(item.getName());
					}
				});
		mDamagesAdapter = new FunDapter<DamageCode>(getActivity(), damageCodes,
				R.layout.list_item_issue_code, damageDict);
		mDamageListView.setAdapter(mDamagesAdapter);
	}
}


//package com.cloudjay.cjay.fragment;
//
//import android.app.Activity;
//import android.graphics.drawable.Drawable;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.widget.Button;
//
//import com.cloudjay.cjay.R;
//import com.cloudjay.cjay.listener.AuditorIssueReportListener;
//import com.cloudjay.cjay.model.Issue;
//import org.androidannotations.annotations.AfterViews;
//import org.androidannotations.annotations.EFragment;
//import org.androidannotations.annotations.ViewById;
//
//@EFragment(R.layout.fragment_issue_damage_code)
//public class IssueReportDamageFragment extends IssueReportFragment implements OnClickListener {
//	private String mDamageCode;
//	private AuditorIssueReportListener mCallback;
//	private Button mCodeButtons[];
//	private Drawable mDefaultBackground;
//	private Issue mIssue;
//	
//	@ViewById(R.id.btnCode0) Button mCode0Button;
//	@ViewById(R.id.btnCode1) Button mCode1Button;
//	@ViewById(R.id.btnCode2) Button mCode2Button;
//	@ViewById(R.id.btnCode3) Button mCode3Button;
//	@ViewById(R.id.btnCode4) Button mCode4Button;
//	@ViewById(R.id.btnCode5) Button mCode5Button;
//	
//	@AfterViews
//	void afterViews() {
//		mCodeButtons = new Button[6];
//		mCodeButtons[0] = mCode0Button;
//		mCodeButtons[1] = mCode1Button;
//		mCodeButtons[2] = mCode2Button;
//		mCodeButtons[3] = mCode3Button;
//		mCodeButtons[4] = mCode4Button;
//		mCodeButtons[5] = mCode5Button;
//		for (int i = 0; i < mCodeButtons.length; i++) {
//			mCodeButtons[i].setOnClickListener(this);
//		}
//		mDefaultBackground = mCode0Button.getBackground();
//		mDamageCode = "";
//		
//		// initialize with issue
//		if (mIssue != null) {
//			Button selectedBtn = null;
//			mDamageCode = mIssue.getDamageCodeString();
//			
//			for (Button btn : mCodeButtons) {
//				if (btn.getText().toString().equals(mDamageCode)) {
//					selectedBtn = btn;
//					break;
//				}
//			}
//			if (selectedBtn != null) {
//				highlightButton(selectedBtn);				
//			}
//		}
//	}
//	
//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//        try {
//            mCallback = (AuditorIssueReportListener) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString() + " must implement AuditorIssueReportListener");
//        }
//    }
//
//	@Override
//	public void onClick(View v) {
//		Button btnCode = (Button)v;
//		highlightButton(btnCode);
//		mDamageCode = (String)btnCode.getText();	
//		mCallback.onReportPageCompleted(AuditorIssueReportListener.TAB_ISSUE_DAMAGE);
//	}
//	
//	@Override
//	public void setIssue(Issue issue) {
//		mIssue = issue;
//	}
//
//	@Override
//	public void validateAndSaveData() {
//		mCallback.onReportValueChanged(AuditorIssueReportListener.TYPE_DAMAGE_CODE, mDamageCode);
//	}
//	
//	private void highlightButton(Button btn) {
//		for (int i = 0; i < mCodeButtons.length; i++) {
//			mCodeButtons[i].setBackgroundDrawable(mDefaultBackground);
//		}
//		btn.setBackgroundResource(R.drawable.btn_code_selected);
//	}
//	
//	@Override
//	public void showKeyboard() {
//	}
//	
//	@Override
//	public void hideKeyboard() {
//	}
//}
