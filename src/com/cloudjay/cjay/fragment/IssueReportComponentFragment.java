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
import android.widget.AbsListView.OnScrollListener;
import android.widget.FilterQueryProvider;
import android.widget.ListView;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.adapter.CodeCursorAdapter;
import com.cloudjay.cjay.listener.AuditorIssueReportListener;
import com.cloudjay.cjay.model.DamageCode;
import com.cloudjay.cjay.model.Issue;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.CJayCursorLoader;
import com.cloudjay.cjay.util.DataCenter;

@EFragment(R.layout.fragment_issue_component_code)
public class IssueReportComponentFragment extends IssueReportFragment implements LoaderCallbacks<Cursor> {
	private AuditorIssueReportListener mCallback;
	private Issue mIssue;

	private String mComponentCode;
	private String mComponentName;
	private boolean ignoreSearch;

	@ViewById(R.id.component_name)
	ClearableEditText mComponentEditText;

	@ViewById(R.id.component_list)
	ListView mComponentListView;

	@SystemService
	InputMethodManager inputMethodManager;

	private int mItemLayout = R.layout.list_item_issue_code;

	private final static int LOADER_ID = CJayConstant.CURSOR_LOADER_ID_GATE_EXPORT;

	CodeCursorAdapter cursorAdapter;

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

		getLoaderManager().initLoader(LOADER_ID, null, this);
		mComponentListView.setTextFilterEnabled(true);
		mComponentListView.setScrollingCacheEnabled(false);
		mComponentListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState != 0) {
					((CodeCursorAdapter) mComponentListView.getAdapter()).isScrolling = true;
				} else {
					((CodeCursorAdapter) mComponentListView.getAdapter()).isScrolling = false;
					((CodeCursorAdapter) mComponentListView.getAdapter()).notifyDataSetChanged();
				}

				inputMethodManager.hideSoftInputFromWindow(mComponentListView.getWindowToken(), 0);
			}
		});
	}

	@ItemClick(R.id.component_list)
	void componentItemClicked(int position) {

		Cursor cursor = (Cursor) cursorAdapter.getItem(position);

		mComponentCode = cursor.getString(cursor.getColumnIndexOrThrow(DamageCode.CODE));
		mComponentName = cursor.getString(cursor.getColumnIndexOrThrow(DamageCode.DISPLAY_NAME));

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

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {

		Context context = getActivity();

		return new CJayCursorLoader(context) {
			@Override
			public Cursor loadInBackground() {
				Cursor cursor = DataCenter.getInstance().getComponentCodesCursor(getContext());

				if (cursor != null) {
					// Ensure the cursor window is filled
					cursor.registerContentObserver(mObserver);
				}

				return cursor;
			}
		};

	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		cursorAdapter.swapCursor(null);

	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {

		if (cursorAdapter == null) {
			cursorAdapter = new CodeCursorAdapter(getActivity(), mItemLayout, cursor, 0);

			cursorAdapter.setFilterQueryProvider(new FilterQueryProvider() {
				@Override
				public Cursor runQuery(CharSequence constraint) {
					return DataCenter.getInstance().filterComponentCodeCursor(getActivity(), constraint);
				}
			});

			mComponentListView.setAdapter(cursorAdapter);

		} else {
			cursorAdapter.swapCursor(cursor);
		}

	}

	private void search(String searchText) {
		if (searchText.equals("") || ignoreSearch) {

		} else {
			if (cursorAdapter != null) {
				cursorAdapter.getFilter().filter(searchText);
			}
		}
	}

	@Override
	public void setIssue(Issue issue) {
		mIssue = issue;
	}

	@Override
	public void showKeyboard() {
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
}
