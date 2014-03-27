package com.cloudjay.cjay;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import com.cloudjay.cjay.adapter.UserLogCursorAdapter;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.UploadCursorLoader;

import android.os.Bundle;
import android.content.Loader;
import android.database.Cursor;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;

@EActivity(R.layout.activity_user_log)
public class UserLogActivity extends CJayActivity implements
		android.app.LoaderManager.LoaderCallbacks<Cursor> {

	@ViewById(R.id.editText_search_content)
	EditText searchEditText;

	@ViewById(R.id.lv_user_log)
	ListView listView;

	UserLogCursorAdapter cursorAdapter;
	private int mItemLayout = R.layout.list_item_user_log;
	private final static int LOADER_ID = CJayConstant.CURSOR_LOADER_ID_USER_LOG;

	@Override
	protected void onResume() {

		if (cursorAdapter != null) {
			refresh();
		}

		super.onResume();
	}

	public void refresh() {
		getLoaderManager().restartLoader(LOADER_ID, null, this);
	}

	@AfterViews
	void initialize() {

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		listView.setAdapter(cursorAdapter);

		getLoaderManager().initLoader(LOADER_ID, null, this);

		searchEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable arg0) {

				if (cursorAdapter != null) {
					cursorAdapter.getFilter().filter(arg0.toString());
				}

			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		});
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {

		return new UploadCursorLoader(this) {

			@Override
			public Cursor loadInBackground() {

				Cursor cursor = DataCenter.getInstance().getUserLogCursor(
						getContext());

				if (cursor != null) {

					// Ensure the cursor window is filled
					cursor.getCount();
					cursor.registerContentObserver(mObserver);
				}

				return cursor;
			}
		};
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		if (cursorAdapter == null) {
			cursorAdapter = new UserLogCursorAdapter(this, mItemLayout, arg1, 0);

			cursorAdapter.setFilterQueryProvider(new FilterQueryProvider() {

				@Override
				public Cursor runQuery(CharSequence constraint) {

					return DataCenter.getInstance().filterUserLogCursor(
							UserLogActivity.this, constraint);
				}

			});

			listView.setAdapter(cursorAdapter);

		} else {
			cursorAdapter.swapCursor(arg1);
		}

	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		cursorAdapter.swapCursor(null);

	}
}
