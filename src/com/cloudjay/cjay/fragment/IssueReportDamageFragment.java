package com.cloudjay.cjay.fragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.ViewById;
import org.droidparts.widget.ClearableEditText;
import org.droidparts.widget.ClearableEditText.Listener;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.adapter.CodeCursorAdapter;
import com.cloudjay.cjay.listener.AuditorIssueReportListener;
import com.cloudjay.cjay.model.DamageCode;
import com.cloudjay.cjay.model.Issue;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.CJayCursorLoader;
import com.cloudjay.cjay.util.DataCenter;

@EFragment(R.layout.fragment_issue_damage_code)
public class IssueReportDamageFragment extends IssueReportFragment implements
		LoaderCallbacks<Cursor> {

	private AuditorIssueReportListener mCallback;
	private Issue mIssue;

	private String mDamageCode;
	private String mDamageName;

	private boolean ignoreSearch;

	@ViewById(R.id.damage_name)
	ClearableEditText mDamageEditText;

	@ViewById(R.id.damage_list)
	ListView mDamageListView;

	@ItemClick(R.id.damage_list)
	void damageItemClicked(int position) {

		Cursor cursor = (Cursor) cursorAdapter.getItem(position);
		
		mDamageCode = cursor.getString(cursor
				.getColumnIndexOrThrow(DamageCode.CODE));
		mDamageName = cursor.getString(cursor
				.getColumnIndexOrThrow(DamageCode.DISPLAY_NAME));

		ignoreSearch = true;
		mDamageEditText.setText(mDamageName);
		ignoreSearch = false;

		// hide keyboard
		inputMethodManager.hideSoftInputFromWindow(
				mDamageEditText.getWindowToken(), 0);

		// move to next tab
		mCallback
				.onReportPageCompleted(AuditorIssueReportListener.TAB_ISSUE_DAMAGE);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mCallback = (AuditorIssueReportListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement AuditorIssueReportListener");
		}
	}

	@Override
	public void setIssue(Issue issue) {
		mIssue = issue;
	}

	@Override
	public boolean validateAndSaveData() {
		if (!TextUtils.isEmpty(mDamageCode)) {
			mDamageEditText.setError(null);
			mCallback.onReportValueChanged(
					AuditorIssueReportListener.TYPE_DAMAGE_CODE, mDamageCode);
			return true;

		} else {
			mDamageEditText
					.setError(getString(R.string.issue_code_missing_warning));
			return false;
		}
	}

	private void search(String searchText) {
		if (searchText.equals("") || ignoreSearch) {
			// do nothing
		} else {
			if (cursorAdapter != null) {
				cursorAdapter.getFilter().filter(searchText);
			}
		}
	}

	@SystemService
	InputMethodManager inputMethodManager;

	private int mItemLayout = R.layout.list_item_issue_code;
	private final static int LOADER_ID = CJayConstant.CURSOR_LOADER_ID_GATE_EXPORT;
	CodeCursorAdapter cursorAdapter;

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

		getLoaderManager().initLoader(LOADER_ID, null, this);
		mDamageListView.setTextFilterEnabled(true);
		mDamageListView.setScrollingCacheEnabled(false);
		mDamageListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState != 0) {
					((CodeCursorAdapter) mDamageListView.getAdapter()).isScrolling = true;
				} else {
					((CodeCursorAdapter) mDamageListView.getAdapter()).isScrolling = false;
					((CodeCursorAdapter) mDamageListView.getAdapter())
							.notifyDataSetChanged();
				}

				inputMethodManager.hideSoftInputFromWindow(
						mDamageEditText.getWindowToken(), 0);
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}
		});
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {

		Context context = getActivity();

		return new CJayCursorLoader(context) {
			@Override
			public Cursor loadInBackground() {
				Cursor cursor = DataCenter.getInstance().getDamageCodesCursor(
						getContext());

				if (cursor != null) {
					// Ensure the cursor window is filled
					cursor.registerContentObserver(mObserver);
				}

				return cursor;
			}
		};
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {

		if (cursorAdapter == null) {
			cursorAdapter = new CodeCursorAdapter(getActivity(), mItemLayout,
					cursor, 0);

			cursorAdapter.setFilterQueryProvider(new FilterQueryProvider() {
				@Override
				public Cursor runQuery(CharSequence constraint) {
					return DataCenter.getInstance().filterDamageCodeCursor(
							getActivity(), constraint);
				}
			});

			mDamageListView.setAdapter(cursorAdapter);

		} else {
			cursorAdapter.swapCursor(cursor);
		}

	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		cursorAdapter.swapCursor(null);
	}

	@Override
	public void showKeyboard() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hideKeyboard() {
		// TODO Auto-generated method stub

	}
}
