package com.cloudjay.cjay;

import java.sql.SQLException;
import java.util.Hashtable;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cloudjay.cjay.adapter.PhotoExpandableListAdapter;
import com.cloudjay.cjay.adapter.PhotoGridViewCursorAdapter;
import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.events.CJayImageAddedEvent;
import com.cloudjay.cjay.events.LogUserActivityEvent;
import com.cloudjay.cjay.fragment.GateImportListFragment;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.network.CJayClient;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.CJayCursorLoader;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.StringHelper;
import com.cloudjay.cjay.util.UploadType;

import de.greenrobot.event.EventBus;

@EActivity(R.layout.activity_photo_expandablelistview)
@OptionsMenu(R.menu.menu_photo_grid_view)
public class PhotoExpandableListViewActivity extends CJayActivity implements LoaderCallbacks<Cursor> {

	public static final String LOG_TAG = "PhotoExpandableListViewActivity";
	public static final String CJAY_CONTAINER_SESSION_UUID_EXTRA = "cjay_container_session_uuid";
	public static final String CJAY_CONTAINER_ID_EXTRA = "cjay_container_id";

	public static final String CJAY_IMAGE_TYPE_1_EXTRA = "cjay_image_type1";
	public static final String CJAY_IMAGE_TYPE_2_EXTRA = "cjay_image_type2";

	public static final String SOURCE_TAG_EXTRA = "tag";

	PhotoExpandableListAdapter mListAdapter;
	ContainerSession mContainerSession;
	int mItemLayout;
	int mNewImageCount = 0;
	int[] mImageTypes;
	ContainerSessionDaoImpl containerSessionDaoImpl;
	private Hashtable<Integer, PhotoGridViewCursorAdapter> mCursorAdapters;

	@Extra(CJAY_CONTAINER_SESSION_UUID_EXTRA)
	String mContainerSessionUUID = "";

	@Extra(CJAY_IMAGE_TYPE_1_EXTRA)
	int mCJayImageTypeA = CJayImage.TYPE_IMPORT;

	@Extra(CJAY_IMAGE_TYPE_2_EXTRA)
	int mCJayImageTypeB = -1;

	@Extra(CJAY_CONTAINER_ID_EXTRA)
	String mContainerId = "";

	@ViewById(R.id.expandable_listview)
	ExpandableListView mListView;

	@ViewById(R.id.btn_add_new)
	ImageButton mAddButton;

	@ViewById(android.R.id.empty)
	TextView mEmptyElement;

	@Extra(SOURCE_TAG_EXTRA)
	String sourceTag = "";

	@AfterViews
	void afterViews() {

		// Set Activity Title
		setTitle(mContainerId);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// init expandable list adapter
		if (mCJayImageTypeB < 0) {
			mImageTypes = new int[1];
			mImageTypes[0] = mCJayImageTypeA;

		} else {

			mImageTypes = new int[2];
			mImageTypes[0] = mCJayImageTypeA;
			mImageTypes[1] = mCJayImageTypeB;
		}

		mItemLayout = R.layout.grid_item_image;
		mCursorAdapters = new Hashtable<Integer, PhotoGridViewCursorAdapter>();
		mListAdapter = new PhotoExpandableListAdapter(this, mContainerSessionUUID, mImageTypes);
		mListView.setAdapter(mListAdapter);
		mListView.setEmptyView(findViewById(android.R.id.empty));

		for (int i = 0; i < mImageTypes.length; i++) {
			mListView.expandGroup(i);
		}

		try {
			containerSessionDaoImpl = CJayClient.getInstance().getDatabaseManager().getHelper(this)
												.getContainerSessionDaoImpl();

			mContainerSession = containerSessionDaoImpl.queryForId(mContainerSessionUUID);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Click(R.id.btn_add_new)
	void cameraClicked() {
		// go to camera
		mNewImageCount = 0;
		CJayApplication.gotoCamera(this, mContainerSession, mCJayImageTypeA, LOG_TAG);
	}

	@OptionsItem(android.R.id.home)
	void homeIconClicked() {
		finish();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		int imageType = -1;

		switch (id) {
			case CJayConstant.CURSOR_LOADER_ID_PHOTO_GRIDVIEW_1:
				imageType = mImageTypes[0];
				break;

			case CJayConstant.CURSOR_LOADER_ID_PHOTO_GRIDVIEW_2:
				imageType = mImageTypes[1];
				break;
		}

		final int cursorLoaderImageType = imageType;

		return new CJayCursorLoader(this) {

			@Override
			public Cursor loadInBackground() {
				Cursor cursor = DataCenter.getInstance().getCJayImagesCursorByContainer(getContext(),
																						mContainerSessionUUID,
																						cursorLoaderImageType);

				if (cursor != null) {
					// Ensure the cursor window is filled
					cursor.registerContentObserver(mObserver);
				}

				return cursor;
			}

		};
	}

	public void onEvent(CJayImageAddedEvent event) {

		if (event.getTag().equals(LOG_TAG)) {
			mNewImageCount++;
		}

	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {

		switch (loader.getId()) {
			case CJayConstant.CURSOR_LOADER_ID_PHOTO_GRIDVIEW_1:
				mCursorAdapters.get(Integer.valueOf(0)).swapCursor(null);
				break;

			case CJayConstant.CURSOR_LOADER_ID_PHOTO_GRIDVIEW_2:
				mCursorAdapters.get(Integer.valueOf(1)).swapCursor(null);
				break;
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		int adapterId = 0;

		switch (loader.getId()) {
			case CJayConstant.CURSOR_LOADER_ID_PHOTO_GRIDVIEW_1:
				adapterId = 0;
				break;

			case CJayConstant.CURSOR_LOADER_ID_PHOTO_GRIDVIEW_2:
				adapterId = 1;
				break;
		}

		if (mCursorAdapters.get(Integer.valueOf(adapterId)) == null) {
			mCursorAdapters.put(Integer.valueOf(adapterId),
								new PhotoGridViewCursorAdapter(this, mItemLayout, cursor, 0));
		} else {
			mCursorAdapters.get(Integer.valueOf(adapterId)).swapCursor(cursor);
		}

		GridView gridView = mListAdapter.getPhotoGridView(adapterId);
		gridView.setAdapter(mCursorAdapters.get(Integer.valueOf(adapterId)));

		if (cursor.getCount() > 0) {
			LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) gridView.getLayoutParams();
			int gridViewWidth = gridView.getMeasuredWidth() > 0 ? gridView.getMeasuredWidth() : gridView.getEmptyView().getMeasuredWidth();
			p.height = gridViewWidth / gridView.getNumColumns() * ((cursor.getCount() + 1) / 2);
			gridView.setLayoutParams(p);
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mCursorAdapters != null && mNewImageCount > 0) {

			Logger.e("Refresh cursor loader");
			if (mCursorAdapters.get(Integer.valueOf(0)) != null) {
				getSupportLoaderManager().restartLoader(CJayConstant.CURSOR_LOADER_ID_PHOTO_GRIDVIEW_1, null, this);
			}
		}

	}

	@OptionsItem(R.id.menu_upload)
	void uploadMenuItemSelected() {
		if (null != mContainerSession) {

			if (sourceTag.equals(GateImportListFragment.LOG_TAG)) {
				mContainerSession.setUploadType(UploadType.IN);
				mContainerSession.setOnLocal(false);

				EventBus.getDefault().post(	new LogUserActivityEvent("Prepare to add #IN container with ID "
													+ mContainerSession.getContainerId() + "to upload queue"));

			} else {
				String checkOutTime = StringHelper.getCurrentTimestamp(CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE);

				mContainerSession.setUploadType(UploadType.OUT);
				mContainerSession.setCheckOutTime(checkOutTime);

				Logger.Log("Prepare to upload EXPORT container " + mContainerSession.getContainerId());
				EventBus.getDefault().post(	new LogUserActivityEvent("Prepare to add #OUT container with ID "
													+ mContainerSession.getContainerId() + "to upload queue"));
			}

			CJayApplication.uploadContainerSesison(context, mContainerSession);
			finish();

		} else {
			showCrouton(R.string.alert_invalid_container);
		}
	}
}