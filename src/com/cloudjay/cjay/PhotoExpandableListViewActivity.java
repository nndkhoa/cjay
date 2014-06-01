package com.cloudjay.cjay;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.cloudjay.cjay.adapter.PhotoExpandableListAdapter;
import com.cloudjay.cjay.adapter.PhotoGridViewCursorAdapter;
import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.dao.IssueDaoImpl;
import com.cloudjay.cjay.events.CJayImageAddedEvent;
import com.cloudjay.cjay.fragment.GateImportListFragment;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.Issue;
import com.cloudjay.cjay.network.CJayClient;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.CJayCursorLoader;
import com.cloudjay.cjay.util.ContainerState;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.StringHelper;
import com.cloudjay.cjay.util.UploadType;
import com.cloudjay.cjay.util.Utils;
import com.cloudjay.cjay.view.CheckablePhotoGridItemLayout;

import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

@EActivity(R.layout.activity_photo_expandablelistview)
@OptionsMenu(R.menu.menu_photo_expandable_list_view)
public class PhotoExpandableListViewActivity extends CJayActivity implements LoaderCallbacks<Cursor> {

	public static final String LOG_TAG = "PhotoExpandableListViewActivity";
	public static final String CJAY_CONTAINER_SESSION_UUID_EXTRA = "cjay_container_session_uuid";
	public static final String CJAY_CONTAINER_ID_EXTRA = "cjay_container_id";
	public static final String CJAY_ISSUE_UUID_EXTRA = "cjay_issue_uuid";
	public static final String CJAY_ISSUE_ID_EXTRA = "cjay_issue_id";
	public static final String CJAY_IMAGE_TYPE_1_EXTRA = "cjay_image_type1";
	public static final String CJAY_IMAGE_TYPE_2_EXTRA = "cjay_image_type2";
	public static final String CJAY_IMAGE_TYPE_COPY_TO_EXTRA = "cjay_image_typeCopyTo";
	public static final String VIEW_MODE_EXTRA = "view_mode";
	public static final String NUM_COLS_EXTRA = "num_columns";

	public static final int MODE_UPLOAD = 0;
	public static final int MODE_IMPORT = 1;
	public static final int MODE_ISSUE = 2;

	public static final String SOURCE_TAG_EXTRA = "tag";

	int mItemLayout;
	MenuItem avMenuItem;
	Crouton mLoadingCrouton;

	int mNewImageCount = 0;
	int[] mImageTypes;
	
	ContainerSessionDaoImpl mContainerSessionDaoImpl;
	IssueDaoImpl mIssueDaoImpl;
	ContainerSession mContainerSession;
	Issue mIssue;
	
	PhotoExpandableListAdapter mListAdapter;
	Hashtable<Integer, PhotoGridViewCursorAdapter> mCursorAdapters;

	@Extra(CJAY_CONTAINER_SESSION_UUID_EXTRA)
	String mContainerSessionUUID = "";
	
	@Extra(CJAY_ISSUE_UUID_EXTRA)
	String mIssueUUID = "";

	@Extra(CJAY_IMAGE_TYPE_1_EXTRA)
	int mCJayImageTypeA = CJayImage.TYPE_IMPORT;

	@Extra(CJAY_IMAGE_TYPE_2_EXTRA)
	int mCJayImageTypeB = -1;

	@Extra(CJAY_IMAGE_TYPE_COPY_TO_EXTRA)
	int mCJayImageTypeCopyTo = -1;

	@Extra(CJAY_CONTAINER_ID_EXTRA)
	String mContainerId = "";
	
	@Extra(CJAY_ISSUE_ID_EXTRA)
	String mIssueId = "";

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

	@ViewById(R.id.non_av_textview)
	TextView mNonAvTextView;

	@Extra(SOURCE_TAG_EXTRA)
	String sourceTag = "";

	@AfterViews
	void afterViews() {
		// Set Activity Title
		setTitle(mViewMode == MODE_ISSUE ? mIssueId : mContainerId);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// Load async
		mLoadingCrouton = makeCrouton("Loading...", Style.INFO, Configuration.DURATION_INFINITE, false);
		mLoadingCrouton.show();
		
		loadAdapters();
		loadData();
	}
	
	@Background
	public void loadAdapters() {
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

		for (int i = 0; i < mImageTypes.length; i++) {
			mListView.expandGroup(i);
		}
	}
	
	@Background
	public void loadData() {		
		try {
			mContainerSessionDaoImpl = CJayClient.getInstance().getDatabaseManager().getHelper(this)
												.getContainerSessionDaoImpl();
			mContainerSession = mContainerSessionDaoImpl.queryForId(mContainerSessionUUID);
			
			if (mViewMode == MODE_ISSUE && !TextUtils.isEmpty(mIssueUUID)) {
				mIssueDaoImpl = CJayClient.getInstance().getDatabaseManager().getHelper(this)
										.getIssueDaoImpl();
				mIssue = mIssueDaoImpl.findByUuid(mIssueUUID);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		afterLoad();
	}
	
	@UiThread
	public void afterLoad() {
		// TODO: để điều kiện này thì bên trong GateImport cũng sẽ thấy?
		// if (mViewMode == MODE_UPLOAD && mContainerSession.getServerContainerState() == ContainerState.AVAILABLE)
		// {
//		if (mContainerSession.getServerContainerState() == ContainerState.AVAILABLE || mCJayImageTypeB < 0) {
//			mNonAvTextView.setVisibility(View.GONE);
//		} else {
//			mNonAvTextView.setVisibility(View.VISIBLE);
//		}
		
		
		// show/hide screen controls
		boolean containerAV = mContainerSession.getServerContainerState() == ContainerState.AVAILABLE;
		switch (mViewMode) {
		case MODE_UPLOAD:
			mAddButton.setVisibility(View.VISIBLE);
			mNonAvTextView.setVisibility(containerAV ? View.GONE : View.VISIBLE);
			break;
			
		case MODE_ISSUE:
			mAddButton.setVisibility(View.VISIBLE);	
			mNonAvTextView.setVisibility(View.GONE);		
			break;
		
		case MODE_IMPORT:
			mAddButton.setVisibility(View.GONE);
			mNonAvTextView.setVisibility(View.GONE);
			break;
		}
		
		Crouton.hide(mLoadingCrouton);
		
		// refresh menu
		supportInvalidateOptionsMenu();
	}
	
	@Click(R.id.btn_add_new)
	void cameraClicked() {
		// go to camera
		if (mContainerSession != null) {
			mNewImageCount = 0;
			switch (mViewMode) {
			case MODE_UPLOAD:
			case MODE_IMPORT:
				CJayApplication.openCamera(this, mContainerSessionUUID, mCJayImageTypeA, LOG_TAG);				
				break;

			case MODE_ISSUE:
				CJayApplication.openCamera(this, mContainerSessionUUID, mIssueUUID, mCJayImageTypeA, LOG_TAG);	
				break;
			}
		}
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

		CJayCursorLoader cJayCursorLoader = new CJayCursorLoader(this) {
			@Override
			public Cursor loadInBackground() {

				Cursor cursor;
				switch (mViewMode) {
				case MODE_IMPORT:
					cursor = DataCenter.getInstance().getCJayImagesCursorByContainerForCopy(getContext(),
																							mContainerSessionUUID,
																							cursorLoaderImageType,
																							mCJayImageTypeCopyTo);
					break;

				case MODE_ISSUE:
					cursor = DataCenter.getInstance().getCJayImagesCursorByIssue(getContext(), mIssueUUID,
																					cursorLoaderImageType);
					break;
					
				case MODE_UPLOAD:
				default:
					cursor = DataCenter.getInstance().getCJayImagesCursorByContainer(getContext(), mContainerSessionUUID,
																						cursorLoaderImageType);
					break;
				}

				if (cursor != null) {
					// Ensure the cursor window is filled
					cursor.registerContentObserver(mObserver);
				}
				return cursor;
			}
		};

		return cJayCursorLoader;
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
				Intent intent;
				switch (mViewMode) {
				case MODE_IMPORT:
					CheckablePhotoGridItemLayout layout = (CheckablePhotoGridItemLayout) v.findViewById(R.id.photo_layout);
					layout.toggle();
					break;

				case MODE_ISSUE:
					intent = new Intent(ctx, PhotoViewPagerActivity_.class);
					intent.putExtra(PhotoViewPagerActivity.START_POSITION, position);
					intent.putExtra(PhotoViewPagerActivity.CJAY_CONTAINER_SESSION_EXTRA, mContainerSessionUUID);
					intent.putExtra(PhotoViewPagerActivity.CJAY_ISSUE_TYPE_EXTRA, mIssueUUID);
					intent.putExtra(PhotoViewPagerActivity.CJAY_IMAGE_TYPE_EXTRA, imageType);
					intent.putExtra("title", title);
					ctx.startActivity(intent);
					break;
					
				case MODE_UPLOAD:
					intent = new Intent(ctx, PhotoViewPagerActivity_.class);
					intent.putExtra(PhotoViewPagerActivity.START_POSITION, position);
					intent.putExtra(PhotoViewPagerActivity.CJAY_CONTAINER_SESSION_EXTRA, mContainerSessionUUID);
					intent.putExtra(PhotoViewPagerActivity.CJAY_IMAGE_TYPE_EXTRA, imageType);
					intent.putExtra("title", title);
					ctx.startActivity(intent);
					break;
				}
			}
		});

		if (cursor.getCount() > 0) {
			LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) gridView.getLayoutParams();
			int gridViewWidth = gridView.getMeasuredWidth() > 0 ? gridView.getMeasuredWidth()
					: gridView.getEmptyView().getMeasuredWidth();
			p.height = gridViewWidth / mNumCols * (int) (1.0 * (cursor.getCount()) / mNumCols + 0.5);
			gridView.setLayoutParams(p);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mCursorAdapters != null && mNewImageCount > 0) {
			
			// update issue to fixed if needed
			if (mViewMode == MODE_ISSUE) {
				if (mIssue != null) {
					// save db records
					mIssue.setFixed(true);
					mIssue.updateField(getApplicationContext(), Issue.FIELD_FIXED, Integer.toString(Utils.toInt(mIssue.isFixed())));
				}
			}
			
			Logger.e("Refresh cursor loader");
			if (mCursorAdapters.get(Integer.valueOf(0)) != null) {
				getSupportLoaderManager().restartLoader(CJayConstant.CURSOR_LOADER_ID_PHOTO_GD_1, null, this);
			}
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		
		menu.findItem(R.id.menu_select_all).setVisible(false);
		menu.findItem(R.id.menu_import).setVisible(false);
		menu.findItem(R.id.menu_upload).setVisible(false);
		menu.findItem(R.id.menu_av).setVisible(false);
		
		if (mContainerSession != null) {
			switch (mViewMode) {
			case MODE_UPLOAD:
				menu.findItem(R.id.menu_upload).setVisible(true);				
				break;

			case MODE_IMPORT:
				menu.findItem(R.id.menu_select_all).setVisible(true);
				menu.findItem(R.id.menu_import).setVisible(true);
				break;
			}
			
			// config AV menu item
			avMenuItem = menu.findItem(R.id.menu_av);
			if (sourceTag.equals(GateImportListFragment.LOG_TAG)) {
				avMenuItem.setVisible(true);
				avMenuItem.setIcon(mContainerSession.isAvailable() ? R.drawable.ic_menu_av : R.drawable.ic_menu_no_av);
			}
		}

		return super.onPrepareOptionsMenu(menu);
	}

	@OptionsItem(R.id.menu_av)
	void avCheck() {
		if (avMenuItem != null && mContainerSession != null) {
			mContainerSession.setAvailable(!mContainerSession.isAvailable());
			mContainerSession.updateField(	this, ContainerSession.FIELD_AV,
											Integer.toString(Utils.toInt(mContainerSession.isAvailable())));

			avMenuItem.setIcon(mContainerSession.isAvailable() ? R.drawable.ic_menu_av : R.drawable.ic_menu_no_av);
		}
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

	@OptionsItem(R.id.menu_import)
	void importImages() {
		if (null != mContainerSession) {

			if (mCursorAdapters.get(Integer.valueOf(0)) != null) {
				SQLiteDatabase db = DataCenter.getDatabaseHelper(this.getApplicationContext()).getWritableDatabase();
				List<String> selectedCJayImageUuidsList = mCursorAdapters.get(Integer.valueOf(0))
																			.getCheckedCJayImageUuids();

				if (selectedCJayImageUuidsList != null) {
					for (String cJayImageUuid : selectedCJayImageUuidsList) {

						String newUuid = UUID.randomUUID().toString();
						String sql = "insert into cjay_image (containerSession_id, image_name, time_posted, state, _id, uuid, type) "
								+ " select containerSession_id, image_name, time_posted, state, _id, "
								+ Utils.sqlString(newUuid)
								+ ","
								+ mCJayImageTypeCopyTo
								+ " from cjay_image where uuid = " + Utils.sqlString(cJayImageUuid);
						db.execSQL(sql);

					}
				}
			}
			finish();

		} else {
			showCrouton(R.string.alert_invalid_container);
		}
	}

	@OptionsItem(R.id.menu_upload)
	void uploadMenuItemSelected() {

		try {
			mContainerSessionDaoImpl.refresh(mContainerSession);
		} catch (SQLException e) {
			e.printStackTrace();
			showCrouton(R.string.alert_try_again);
		}

		if (null != mContainerSession) {

			if (sourceTag.equals(GateImportListFragment.LOG_TAG)) {

				if (mContainerSession.isValidForUpload(context, CJayImage.TYPE_IMPORT)) {

					// mContainerSession.setUploadType(UploadType.IN);
					mContainerSession.updateField(	context, ContainerSession.FIELD_UPLOAD_TYPE,
													Integer.toString(UploadType.IN.getValue()));
					// mContainerSession.setOnLocal(false);
					DataCenter.getDatabaseHelper(this).addUsageLog(	"Prepare to add #IN container with ID "
																			+ mContainerSession.getContainerId()
																			+ " to upload queue");

				} else {
					showCrouton(R.string.alert_no_issue_container);
					return;
				}

			} else {

				if (mContainerSession.isValidForUpload(context, CJayImage.TYPE_EXPORT)) {

					// mContainerSession.setUploadType(UploadType.OUT);
					// mContainerSession.setCheckOutTime(StringHelper.getCurrentTimestamp(CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE));

					String currentTime = StringHelper.getCurrentTimestamp(CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE);
					mContainerSession.updateField(	context, ContainerSession.FIELD_UPLOAD_TYPE,
													Integer.toString(UploadType.OUT.getValue()));
					mContainerSession.updateField(context, ContainerSession.FIELD_CHECK_OUT_TIME, currentTime);

					Logger.Log("Prepare to upload EXPORT container " + mContainerSession.getContainerId());
					DataCenter.getDatabaseHelper(this).addUsageLog(	"Prepare to add #OUT container with ID "
																			+ mContainerSession.getContainerId()
																			+ " to upload queue");

				} else {
					showCrouton(R.string.alert_no_issue_container);
					return;
				}
			}

			CJayApplication.uploadContainerSesison(context, mContainerSession);
			finish();

		} else {
			showCrouton(R.string.alert_invalid_container);
		}
	}
}