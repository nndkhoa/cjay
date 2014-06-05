package com.cloudjay.cjay.fragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ViewById;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.cloudjay.cjay.CJayApplication;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.adapter.IssueItemCursorAdapter;
import com.cloudjay.cjay.events.CJayImageAddedEvent;
import com.cloudjay.cjay.events.ContainerSessionChangedEvent;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.Issue;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.CJayCursorLoader;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.QueryHelper;
import com.cloudjay.cjay.util.Utils;

import de.greenrobot.event.EventBus;

@EFragment(R.layout.fragment_repair_issue_pending)
public class RepairIssuePendingListFragment extends SherlockFragment implements LoaderCallbacks<Cursor> {

	public final static String LOG_TAG = "RepairIssuePendingListFragment";
	private static final int LOADER_ID = CJayConstant.CURSOR_LOADER_ID_REPAIR_ISSUE_PENDING;

	private String mContainerSessionUuid;
	private IssueItemCursorAdapter mCursorAdapter;
	private int mNewImageCount = 0;
	private String mSelectedIssueUUID;
	private int mItemLayout = R.layout.list_item_issue;

	@ViewById(R.id.feeds)
	ListView mFeedListView;

	@ViewById(android.R.id.empty)
	FrameLayout emptyElement;

	@AfterViews
	void afterViews() {
		mFeedListView.setEmptyView(emptyElement);
		getLoaderManager().initLoader(LOADER_ID, null, this);
	}

	@ItemClick(R.id.feeds)
	void imageItemClicked(int position) {
		mFeedListView.setItemChecked(-1, true);

		// get selected issue uuid
		Cursor cursor = (Cursor) mCursorAdapter.getItem(position);
		mSelectedIssueUUID = cursor.getString(cursor.getColumnIndexOrThrow("issue_id"));

		// show camera
		mNewImageCount = 0;
		CJayApplication.openCamera(	getActivity(), mContainerSessionUuid, mSelectedIssueUUID, CJayImage.TYPE_REPAIRED,
									LOG_TAG);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		EventBus.getDefault().register(this);
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onDestroy() {
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}

	public void onEventMainThread(ContainerSessionChangedEvent event) {
		Logger.Log("onEvent ContainerSessionChangedEvent");
		refresh();
	}

	public void onEvent(CJayImageAddedEvent event) {
		if (event.getTag().equals(LOG_TAG)) {
			mNewImageCount++;
		}
	}

	@Override
	public void onResume() {

		if (mCursorAdapter != null && mNewImageCount > 0) {

			// update issue to fixed if needed
			if (!TextUtils.isEmpty(mSelectedIssueUUID)) {
				QueryHelper.update(	getActivity(), "issue", Issue.FIELD_FIXED, "1",
									Issue.FIELD_UUID + " = " + Utils.sqlString(mSelectedIssueUUID));
			}

			refresh();
		}
		super.onResume();
	}

	public void refresh() {
		getLoaderManager().restartLoader(LOADER_ID, null, this);
	}

	public void setContainerSessionUUID(String containerSessionUUID) {
		mContainerSessionUuid = containerSessionUUID;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		Context context = getActivity();

		return new CJayCursorLoader(context) {
			@Override
			public Cursor loadInBackground() {
				Cursor cursor = DataCenter.getInstance().getPendingIssueItemCursorByContainer(getContext(),
																								mContainerSessionUuid);

				if (cursor != null) {
					// Ensure the cursor window is filled
					cursor.registerContentObserver(mObserver);
				}

				return cursor;
			}
		};
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mCursorAdapter.swapCursor(null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

		final Context context = getActivity();

		if (mCursorAdapter == null) {
			mCursorAdapter = new IssueItemCursorAdapter(context, mItemLayout, cursor, 0);
			mFeedListView.setAdapter(mCursorAdapter);

		} else {
			mCursorAdapter.swapCursor(cursor);
		}
	}
}