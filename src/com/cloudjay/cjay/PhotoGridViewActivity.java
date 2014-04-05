package com.cloudjay.cjay;

import java.sql.SQLException;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import android.widget.ExpandableListView;

import com.cloudjay.cjay.adapter.PhotoExpandableListAdapter;
import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.fragment.GateImportListFragment;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.network.CJayClient;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.StringHelper;

@EActivity(R.layout.activity_photo_gridview)
@OptionsMenu(R.menu.menu_photo_grid_view)
public class PhotoGridViewActivity extends CJayActivity {
	
	public static final String CJAY_CONTAINER_SESSION_UUID_EXTRA = "cjay_container_session_uuid";
	public static final String CJAY_CONTAINER_ID_EXTRA = "cjay_container_id";
	public static final String CJAY_IMAGE_TYPE_1_EXTRA = "cjay_image_type1";
	public static final String CJAY_IMAGE_TYPE_2_EXTRA = "cjay_image_type2";
	
//	ArrayList<PhotoGridViewCursorAdapter> mCursorAdapters;
	PhotoExpandableListAdapter mListAdapter;
	ContainerSession mContainerSession;
	int mItemLayout;
	ContainerSessionDaoImpl containerSessionDaoImpl;

	@Extra(CJAY_CONTAINER_SESSION_UUID_EXTRA)
	String mContainerSessionUUID = "";

	@Extra(CJAY_IMAGE_TYPE_1_EXTRA)
	int mCJayImageTypeA = CJayImage.TYPE_IMPORT;
	
	@Extra(CJAY_IMAGE_TYPE_2_EXTRA)
	int mCJayImageTypeB = -1;

	@Extra(CJAY_CONTAINER_ID_EXTRA)
	String mContainerId = "";

//	@ViewById(R.id.gridview)
//	GridView mGridView;
	
	@ViewById(R.id.expandable_listview)
	ExpandableListView mListView;

	@Extra("tag")
	String sourceTag = "";

	@OptionsItem(android.R.id.home)
	void homeIconClicked() {
		finish();
	}

	@AfterViews
	void afterViews() {

		// Set Activity Title
		setTitle(mContainerId);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mItemLayout = R.layout.grid_item_image;
		
		// init expandable list adapter
		int[] imageTypes;
		if (mCJayImageTypeB < 0) {
			 imageTypes = new int[1];
			 imageTypes[0] = mCJayImageTypeA;
		} else {
			 imageTypes = new int[2];
			 imageTypes[0] = mCJayImageTypeA;
			 imageTypes[1] = mCJayImageTypeB;
		}
		mListAdapter = new PhotoExpandableListAdapter(this, mContainerSessionUUID, imageTypes);
		mListView.setAdapter(mListAdapter);
		mListView.setEmptyView(findViewById(android.R.id.empty));
		
		// init cursor adapters
//		mCursorAdapters = new ArrayList<PhotoGridViewCursorAdapter>();
//		if (mCJayImageTypeA >= 0) {
//			getSupportLoaderManager().initLoader(CJayConstant.CURSOR_LOADER_ID_PHOTO_GRIDVIEW_1, null, this);
//			mCursorAdapters.add(null);
//		}
//		if (mCJayImageTypeB >= 0) {
//			getSupportLoaderManager().initLoader(CJayConstant.CURSOR_LOADER_ID_PHOTO_GRIDVIEW_2, null, this);
//			mCursorAdapters.add(null);
//		}

		for (int i = 0; i < imageTypes.length; i++) {
			mListView.expandGroup(i);
		}
		
		try {
			containerSessionDaoImpl = CJayClient.getInstance()
					.getDatabaseManager().getHelper(this)
					.getContainerSessionDaoImpl();

			mContainerSession = containerSessionDaoImpl
					.queryForId(mContainerSessionUUID);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@OptionsItem(R.id.menu_upload)
	void uploadMenuItemSelected() {
		if (null != mContainerSession) {

			if (sourceTag.equals(GateImportListFragment.LOG_TAG)) {

				mContainerSession.setUploadType(ContainerSession.TYPE_IN);
				mContainerSession.setOnLocal(false);
			} else {
				mContainerSession.setUploadType(ContainerSession.TYPE_OUT);
				mContainerSession
						.setCheckOutTime(StringHelper
								.getCurrentTimestamp(CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE));
			}

			CJayApplication.uploadContainerSesison(context, mContainerSession);

			finish();
		} else {
			showCrouton(R.string.alert_invalid_container);
		}
	}

//	@Override
//	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
//		Context context = this;
//		int imageType = -1;
//
//		switch (id) {
//		case CJayConstant.CURSOR_LOADER_ID_PHOTO_GRIDVIEW_1:
//			imageType = mCJayImageTypeA;
//			break;
//
//		case CJayConstant.CURSOR_LOADER_ID_PHOTO_GRIDVIEW_2:
//			imageType = mCJayImageTypeB;
//			break;
//		}
//		
//		final int cursorLoaderImageType = imageType;
//		
//		return new CJayCursorLoader(context) {
//			@Override
//			public Cursor loadInBackground() {
//				Cursor cursor = DataCenter.getInstance()
//						.getCJayImagesByContainer(getContext(),
//								mContainerSessionUUID, cursorLoaderImageType);
//
//				if (cursor != null) {
//					// Ensure the cursor window is filled
//					cursor.registerContentObserver(mObserver);
//				}
//
//				return cursor;
//			}
//		};
//	}
//
//	@Override
//	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
//		final Context context = this;
//		int adapterId = 0;
//		
//		switch (loader.getId()) {
//		case CJayConstant.CURSOR_LOADER_ID_PHOTO_GRIDVIEW_1:
//			adapterId = 0;		
//			break;
//
//		case CJayConstant.CURSOR_LOADER_ID_PHOTO_GRIDVIEW_2:
//			adapterId = 1;		
//			break;
//		}
//		
//		if (mCursorAdapters.get(adapterId) == null) {
//			mCursorAdapters.set(adapterId, new PhotoGridViewCursorAdapter(context,
//					mItemLayout, cursor, 0));
//			
//			GridView gridView = mListAdapter.getPhotoGridView(adapterId);
//			gridView.setAdapter(mCursorAdapters.get(adapterId));
//			
//		} else {
//			mCursorAdapters.get(adapterId).swapCursor(cursor);
//		}
//	}
//
//	@Override
//	public void onLoaderReset(Loader<Cursor> loader) {
//		
//		switch (loader.getId()) {
//		case CJayConstant.CURSOR_LOADER_ID_PHOTO_GRIDVIEW_1:
//			mCursorAdapters.get(0).swapCursor(null);			
//			break;
//
//		case CJayConstant.CURSOR_LOADER_ID_PHOTO_GRIDVIEW_2:
//			mCursorAdapters.get(1).swapCursor(null);			
//			break;
//		}
//	}
}