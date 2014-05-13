package com.cloudjay.cjay.fragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.adapter.UploadCursorAdapter;
import com.cloudjay.cjay.events.ContainerSessionEnqueueEvent;
import com.cloudjay.cjay.events.ContainerSessionUpdatedEvent;
import com.cloudjay.cjay.events.ListItemChangedEvent;
import com.cloudjay.cjay.events.LogUserActivityEvent;
import com.cloudjay.cjay.events.UploadStateRestoredEvent;
import com.cloudjay.cjay.model.Container;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.CJayCursorLoader;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.UploadState;
import com.example.android.swipedismiss.SwipeDismissListViewTouchListener;
import com.example.android.swipedismiss.SwipeDismissListViewTouchListener.OnDismissCallback;

import de.greenrobot.event.EventBus;

@EFragment(R.layout.fragment_uploads)
@OptionsMenu(R.menu.menu_upload)
public class UploadsFragment extends SherlockFragment implements OnDismissCallback, OnItemClickListener,
														LoaderCallbacks<Cursor> {

	public UploadsFragment() {
	}

	@Override
	public boolean canDismiss(AbsListView listView, int position) {
		try {

			Cursor cursor = (Cursor) listView.getItemAtPosition(position);
			UploadState uploadState = UploadState.values()[cursor.getInt(cursor.getColumnIndexOrThrow(ContainerSession.FIELD_STATE))];
			return uploadState == UploadState.COMPLETED;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	@OptionsItem(R.id.menu_clear_uploaded)
	void clearUploadsMenuItemSelected() {

		DataCenter.getInstance().clearListUpload(DataCenter.getDatabaseHelper(getActivity()).getWritableDatabase());
		DataCenter.getDatabaseHelper(getActivity()).addUsageLog("Clear list #upload");
		refresh();

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		EventBus.getDefault().register(this);
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onResume() {
		if (cursorAdapter != null) {
			refresh();
		}
		super.onResume();
	}

	@ViewById(android.R.id.list)
	ListView mListView;

	@ViewById(android.R.id.empty)
	TextView mEmptyElement;

	@AfterViews
	void initListView() {
		getLoaderManager().initLoader(LOADER_ID, null, this);

		SwipeDismissListViewTouchListener swipeListener = new SwipeDismissListViewTouchListener(mListView, this);
		mListView.setOnTouchListener(swipeListener);
		mListView.setOnScrollListener(swipeListener.makeScrollListener());
		mListView.setSelector(R.drawable.selectable_background_photup);
		mListView.setEmptyView(mEmptyElement);
	}

	@Override
	public void onDestroy() {
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}

	@Override
	public void onDismiss(AbsListView listView, int[] reverseSortedPositions) {
		Logger.Log("onSwipeDismiss");

		// set item Cleared = true then call refresh()
		try {
			for (int i = 0, z = reverseSortedPositions.length; i < z; i++) {

				Cursor cursor = (Cursor) listView.getItemAtPosition(reverseSortedPositions[i]);
				String containerId = cursor.getString(cursor.getColumnIndexOrThrow(ContainerSession.FIELD_UUID));
				DataCenter.getInstance()
							.removeContainerFromListUpload(	DataCenter.getDatabaseHelper(getActivity())
																		.getWritableDatabase(), containerId);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		refresh();
	}

	public void onEvent(ContainerSessionEnqueueEvent event) {
		Logger.Log("onEvent ContainerSessionEnqueueEvent");
		refresh();
	}

	public void onEvent(ContainerSessionUpdatedEvent event) {
		Logger.Log("onEvent ContainerSessionUpdatedEvent | "
				+ UploadState.values()[event.getTarget().getUploadState()].name());
		refresh();
	}

	// public void onEvent(UploadStateChangedEvent event) {
	// Logger.Log("onEvent UploadStateChangedEvent | "
	// + UploadState.values()[event.getTarget().getUploadState()].name());
	// refresh();
	// }

	public void onEvent(UploadStateRestoredEvent event) {
		refresh();
	}

	@Override
	public void onItemClick(AdapterView<?> l, View view, int position, long id) {
		int viewId = view.getId();

		Cursor cursor = (Cursor) mListView.getItemAtPosition(position);

		// int uploadState = cursor.getInt(cursor.getColumnIndexOrThrow(ContainerSession.FIELD_STATE));
		UploadState uploadState = UploadState.values()[cursor.getInt(cursor.getColumnIndexOrThrow(ContainerSession.FIELD_STATE))];
		String containerId = cursor.getString(cursor.getColumnIndexOrThrow(Container.CONTAINER_ID));
		String containerUuid = cursor.getString(cursor.getColumnIndexOrThrow(ContainerSession.FIELD_UUID));

		switch (viewId) {
			case R.id.iv_upload_result:
				Logger.Log("User click on retry button");

				DataCenter.getDatabaseHelper(getActivity()).addUsageLog(containerId + " | User #retry manually");
				if (uploadState == UploadState.ERROR) {
					DataCenter.getInstance().rollback(	DataCenter.getDatabaseHelper(getActivity())
																	.getWritableDatabase(), containerUuid);

					// TODO: post new Event to refresh other fragment
					refresh();
				}
				break;

			default:
				break;
		}

	}

	@Override
	public void onStop() {
		super.onStop();
	}

	private final static int LOADER_ID = CJayConstant.CURSOR_LOADER_ID_UPLOAD;
	int totalItems = 0;
	UploadCursorAdapter cursorAdapter;
	private final int mItemLayout = R.layout.item_list_upload;

	public void refresh() {
		getLoaderManager().restartLoader(LOADER_ID, null, this);
	}

	void setTotalItems(int val) {
		totalItems = val;
		EventBus.getDefault().post(new ListItemChangedEvent(1, totalItems));
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {

		Context context = getActivity();

		return new CJayCursorLoader(context) {
			@Override
			public Cursor loadInBackground() {
				Cursor cursor = DataCenter.getInstance().getUploadContainerSessionCursor(getContext());

				if (cursor != null) {
					// Ensure the cursor window is filled
					// setTotalItems(cursor.getCount());
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
			cursorAdapter = new UploadCursorAdapter(getActivity(), mItemLayout, cursor, 0);
			mListView.setAdapter(cursorAdapter);

		} else {
			cursorAdapter.swapCursor(cursor);
		}
	}
}
