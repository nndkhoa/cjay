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

@EFragment(R.layout.fragment_issue_repair_code)
public class IssueReportRepairFragment extends IssueReportFragment implements
		LoaderCallbacks<Cursor> {

	private AuditorIssueReportListener mCallback;
	private Issue mIssue;

	private String mRepairCode;
	private String mRepairName;
	private boolean ignoreSearch;

	@ViewById(R.id.repair_name)
	ClearableEditText mRepairEditText;

	@ViewById(R.id.repair_list)
	ListView mRepairListView;

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

		getLoaderManager().initLoader(LOADER_ID, null, this);
		mRepairListView.setTextFilterEnabled(true);
		mRepairListView.setScrollingCacheEnabled(false);
		mRepairListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState != 0) {
					((CodeCursorAdapter) mRepairListView.getAdapter()).isScrolling = true;
				} else {
					((CodeCursorAdapter) mRepairListView.getAdapter()).isScrolling = false;
					((CodeCursorAdapter) mRepairListView.getAdapter())
							.notifyDataSetChanged();
				}

				inputMethodManager.hideSoftInputFromWindow(
						mRepairListView.getWindowToken(), 0);
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}
		});
	}

	@ItemClick(R.id.repair_list)
	void repairItemClicked(int position) {

		Cursor cursor = (Cursor) cursorAdapter.getItem(position);

		mRepairCode = cursor.getString(cursor
				.getColumnIndexOrThrow(DamageCode.CODE));
		mRepairName = cursor.getString(cursor
				.getColumnIndexOrThrow(DamageCode.DISPLAY_NAME));

		ignoreSearch = true;
		mRepairEditText.setText(mRepairName);
		ignoreSearch = false;

		// hide keyboard
		inputMethodManager.hideSoftInputFromWindow(
				mRepairEditText.getWindowToken(), 0);

		// move to next tab
		mCallback
				.onReportPageCompleted(AuditorIssueReportListener.TAB_ISSUE_REPAIR);
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
		if (!TextUtils.isEmpty(mRepairCode)) {
			mRepairEditText.setError(null);
			mCallback.onReportValueChanged(
					AuditorIssueReportListener.TYPE_REPAIR_CODE, mRepairCode);
			return true;

		} else {
			mRepairEditText
					.setError(getString(R.string.issue_code_missing_warning));
			return false;
		}
	}

	@Override
	public void showKeyboard() {
	}

	@Override
	public void hideKeyboard() {
		// hide keyboard

	}

	private void search(String searchText) {
		if (searchText.equals("") || ignoreSearch) {

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

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		Context context = getActivity();

		return new CJayCursorLoader(context) {
			@Override
			public Cursor loadInBackground() {
				Cursor cursor = DataCenter.getInstance().getRepairCodesCursor(
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
					return DataCenter.getInstance().filterRepairCodeCursor(
							getActivity(), constraint);
				}
			});

			mRepairListView.setAdapter(cursorAdapter);

		} else {
			cursorAdapter.swapCursor(cursor);
		}

	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		cursorAdapter.swapCursor(null);
	}
}
