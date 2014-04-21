package com.cloudjay.cjay;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
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
import com.cloudjay.cjay.util.Utils;
import com.cloudjay.cjay.view.CheckablePhotoItemLayout;

import de.greenrobot.event.EventBus;

@EActivity(R.layout.activity_photo_expandablelistview)
@OptionsMenu(R.menu.menu_photo_expandable_list_view)
public class PhotoExpandableListViewActivity extends CJayActivity implements LoaderCallbacks<Cursor> {

	public static final String LOG_TAG = "PhotoExpandableListViewActivity";
	public static final String CJAY_CONTAINER_SESSION_UUID_EXTRA = "cjay_container_session_uuid";
	public static final String CJAY_CONTAINER_ID_EXTRA = "cjay_container_id";
	public static final String CJAY_IMAGE_TYPE_1_EXTRA = "cjay_image_type1";
	public static final String CJAY_IMAGE_TYPE_2_EXTRA = "cjay_image_type2";
	public static final String CJAY_IMAGE_TYPE_COPY_TO_EXTRA = "cjay_image_typeCopyTo";
	public static final String VIEW_MODE_EXTRA = "view_mode";
	public static final String NUM_COLS_EXTRA = "num_columns";

	public static final int MODE_UPLOAD = 0;
	public static final int MODE_IMPORT = 1;

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

	@Extra(CJAY_IMAGE_TYPE_COPY_TO_EXTRA)
	int mCJayImageTypeCopyTo = -1;

	@Extra(CJAY_CONTAINER_ID_EXTRA)
	String mContainerId = "";

	@Extra(VIEW_MODE_EXTRA)
	int mViewMode = MODE_UPLOAD;

	@Extra(NUM_COLS_EXTRA)
	int mNumCols = 2;

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

		// init camera
		if (mViewMode == MODE_UPLOAD) {
			mAddButton.setVisibility(View.VISIBLE);
		} else {
			mAddButton.setVisibility(View.GONE);
		}

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

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		int imageType = -1;

		switch (id) {
			case CJayConstant.CURSOR_LOADER_ID_PHOTO_GD_1:
				imageType = mImageTypes[0];
				break;

			case CJayConstant.CURSOR_LOADER_ID_PHOTO_GD_2:
				imageType = mImageTypes[1];
				break;
		}

		final int cursorLoaderImageType = imageType;

		return new CJayCursorLoader(this) {
			@Override
			public Cursor loadInBackground() {

				Cursor cursor;
				if (mViewMode == MODE_IMPORT) {

					Logger.Log("mContainerSession: " + mContainerSessionUUID);
					Logger.Log("cursorLoaderImageType: " + cursorLoaderImageType);
					Logger.Log("mCJayImageTypeCopyTo: " + mCJayImageTypeCopyTo);

					cursor = DataCenter.getInstance().getCJayImagesCursorByContainerForCopy(getContext(),
																							mContainerSessionUUID,
																							cursorLoaderImageType,
																							mCJayImageTypeCopyTo);
				} else {
					cursor = DataCenter.getInstance().getCJayImagesCursorByContainer(getContext(),
																						mContainerSessionUUID,
																						cursorLoaderImageType);
				}

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
			case CJayConstant.CURSOR_LOADER_ID_PHOTO_GD_1:
				mCursorAdapters.get(Integer.valueOf(0)).swapCursor(null);
				break;

			case CJayConstant.CURSOR_LOADER_ID_PHOTO_GD_2:
				mCursorAdapters.get(Integer.valueOf(1)).swapCursor(null);
				break;
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		int adapterId = 0;

		switch (loader.getId()) {
			case CJayConstant.CURSOR_LOADER_ID_PHOTO_GD_1:
				adapterId = 0;
				break;

			case CJayConstant.CURSOR_LOADER_ID_PHOTO_GD_2:
				adapterId = 1;
				break;
		}

		if (mCursorAdapters.get(Integer.valueOf(adapterId)) == null) {
			mCursorAdapters.put(Integer.valueOf(adapterId),
								new PhotoGridViewCursorAdapter(this, mItemLayout, cursor, 0, mViewMode == MODE_IMPORT));
		} else {
			mCursorAdapters.get(Integer.valueOf(adapterId)).swapCursor(cursor);
		}

		final Context ctx = this;
		final int imageType = mImageTypes[adapterId];
		final String title = Utils.getImageTypeDescription(this, mImageTypes[adapterId]);

		GridView gridView = mListAdapter.getPhotoGridView(adapterId);
		gridView.setAdapter(mCursorAdapters.get(Integer.valueOf(adapterId)));
		gridView.setNumColumns(mNumCols);
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				if (mViewMode == MODE_IMPORT) {
					CheckablePhotoItemLayout layout = (CheckablePhotoItemLayout) v.findViewById(R.id.photo_layout);
					layout.toggle();
				} else {
					Intent intent = new Intent(ctx, PhotoViewPagerActivity_.class);
					intent.putExtra(PhotoViewPagerActivity.START_POSITION, position);
					intent.putExtra(PhotoViewPagerActivity.CJAY_CONTAINER_SESSION_EXTRA, mContainerSessionUUID);
					intent.putExtra(PhotoViewPagerActivity.CJAY_IMAGE_TYPE_EXTRA, imageType);
					intent.putExtra("title", title);
					ctx.startActivity(intent);
				}
			}
		});

		if (cursor.getCount() > 0) {
			LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) gridView.getLayoutParams();
			int gridViewWidth = gridView.getMeasuredWidth() > 0 ? gridView.getMeasuredWidth()
					: gridView.getEmptyView().getMeasuredWidth();
			p.height = gridViewWidth / mNumCols * ((cursor.getCount() + 1) / mNumCols);
			gridView.setLayoutParams(p);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mCursorAdapters != null && mNewImageCount > 0) {
			Logger.e("Refresh cursor loader");
			if (mCursorAdapters.get(Integer.valueOf(0)) != null) {
				getSupportLoaderManager().restartLoader(CJayConstant.CURSOR_LOADER_ID_PHOTO_GD_1, null, this);
			}
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.menu_select_all).setVisible(mViewMode == MODE_IMPORT);
		return super.onPrepareOptionsMenu(menu);
	}

	@OptionsItem(R.id.menu_select_all)
	void selectAll() {
		PhotoGridViewCursorAdapter adapter = mCursorAdapters.get(Integer.valueOf(0));

		if (adapter != null && mViewMode == MODE_IMPORT) {
			Cursor cursor = adapter.getCursor();
			ArrayList<String> cJayImageUuids;

			if (adapter.getCheckedCJayImageUuids().size() < cursor.getCount()) {
				// select all
				cJayImageUuids = new ArrayList<String>(cursor.getCount());

				if (cursor.moveToFirst()) {
					do {
						cJayImageUuids.add(cursor.getString(cursor.getColumnIndexOrThrow(CJayImage.FIELD_UUID)));
					} while (cursor.moveToNext());
				}

			} else {
				// select none
				cJayImageUuids = new ArrayList<String>();
			}

			adapter.setCheckedCJayImageUuids(cJayImageUuids);
			getSupportLoaderManager().restartLoader(CJayConstant.CURSOR_LOADER_ID_PHOTO_GD_1, null, this);
		}
	}

	@OptionsItem(R.id.menu_upload)
	void uploadMenuItemSelected() {
		if (null != mContainerSession) {

			if (mViewMode == MODE_UPLOAD) {
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

			} else if (mViewMode == MODE_IMPORT) {

				if (mCursorAdapters.get(Integer.valueOf(0)) != null) {
					SQLiteDatabase db = DataCenter.getDatabaseHelper(this.getApplicationContext())
													.getWritableDatabase();
					List<String> selectedCJayImageUuidsList = mCursorAdapters.get(Integer.valueOf(0))
																				.getCheckedCJayImageUuids();

					if (selectedCJayImageUuidsList != null) {
						for (String cJayImageUuid : selectedCJayImageUuidsList) {
							String newUuid = UUID.randomUUID().toString();
							String sql = "insert into cjay_image (containerSession_id, issue_id, _id, image_name, time_posted, state, id, uuid, type) "
									+ " select containerSession_id, issue_id, _id, image_name, time_posted, state, id, "
									+ Utils.sqlString(newUuid)
									+ ","
									+ mCJayImageTypeCopyTo
									+ " from cjay_image where uuid = " + Utils.sqlString(cJayImageUuid);
							db.execSQL(sql);
						}
					}
				}
				finish();
			}

		} else {
			showCrouton(R.string.alert_invalid_container);
		}
	}
}