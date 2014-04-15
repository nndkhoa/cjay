package com.cloudjay.cjay;

import java.sql.SQLException;
import java.util.UUID;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ItemLongClick;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.Trace;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.cloudjay.cjay.adapter.IssueItemCursorAdapter;
import com.cloudjay.cjay.dao.CJayImageDaoImpl;
import com.cloudjay.cjay.dao.ComponentCodeDaoImpl;
import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.dao.DamageCodeDaoImpl;
import com.cloudjay.cjay.dao.IssueDaoImpl;
import com.cloudjay.cjay.dao.RepairCodeDaoImpl;
import com.cloudjay.cjay.events.CJayImageAddedEvent;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.Issue;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.CJayCustomCursorLoader;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.Utils;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

// slide 15

@EActivity(R.layout.activity_auditor_container)
@OptionsMenu(R.menu.menu_auditor_container)
public class AuditorContainerActivity extends CJayActivity implements android.app.LoaderManager.LoaderCallbacks<Cursor> {

	private static final String LOG_TAG = "AuditorContainerActivity";
	public static final String CJAY_CONTAINER_SESSION_EXTRA = "cjay_container_session";
	public static final String START_CAMERA_EXTRA = "start_camera";

	private ContainerSession mContainerSession;
	private CJayImage mLongClickedCJayImage;
	private int mNewImageCount;
	private String mNewImageUUID;

	private String mSelectedCJayImageUuid;
	private String mLongClickedCJayImageUuid;

	ContainerSessionDaoImpl containerSessionDaoImpl = null;
	CJayImageDaoImpl cJayImageDaoImpl = null;
	IssueDaoImpl issueDaoImpl = null;

	int mItemLayout = R.layout.list_item_issue;
	IssueItemCursorAdapter mCursorAdapter;
	private final static int LOADER_ID = CJayConstant.CURSOR_LOADER_ID_ISSUE_ITEM;

	@ViewById(R.id.btn_add_new)
	ImageButton mAddButton;

	@ViewById(R.id.feeds)
	ListView mFeedListView;

	@ViewById(R.id.container_id_textview)
	TextView containerIdTextView;

	@Extra(CJAY_CONTAINER_SESSION_EXTRA)
	String mContainerSessionUUID = "";

	@Extra(START_CAMERA_EXTRA)
	boolean mStartCamera = false;

	DamageCodeDaoImpl damageCodeDaoImpl = null;

	RepairCodeDaoImpl repairCodeDaoImpl = null;
	ComponentCodeDaoImpl componentCodeDaoImpl = null;

	@AfterViews
	void afterViews() {

		try {

			if (null == containerSessionDaoImpl) {
				containerSessionDaoImpl = DataCenter.getDatabaseHelper(context).getContainerSessionDaoImpl();
			}

			mContainerSession = containerSessionDaoImpl.queryForId(mContainerSessionUUID);

			if (null == cJayImageDaoImpl) {
				cJayImageDaoImpl = DataCenter.getDatabaseHelper(context).getCJayImageDaoImpl();
			}

			if (null == issueDaoImpl) {
				issueDaoImpl = DataCenter.getDatabaseHelper(context).getIssueDaoImpl();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		containerIdTextView.setText(mContainerSession.getContainerId());
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		getLoaderManager().initLoader(LOADER_ID, null, this);

		mLongClickedCJayImage = null;
		mNewImageCount = 0;

		getOtherDao();

		if (mStartCamera) {
			cameraClicked();
		}
	}

	@Click(R.id.btn_add_new)
	void cameraClicked() {
		// refresh highlighting and clear current selection
		mFeedListView.setItemChecked(-1, true);
		mLongClickedCJayImage = null;

		supportInvalidateOptionsMenu();

		// go to camera
		mNewImageCount = 0;
		mNewImageUUID = "";
		CJayApplication.gotoCamera(this, mContainerSession, CJayImage.TYPE_REPORT, LOG_TAG);
	}

	@Background
	void getOtherDao() {
		try {
			damageCodeDaoImpl = DataCenter.getDatabaseHelper(context).getDamageCodeDaoImpl();
			repairCodeDaoImpl = DataCenter.getDatabaseHelper(context).getRepairCodeDaoImpl();
			componentCodeDaoImpl = DataCenter.getDatabaseHelper(context).getComponentCodeDaoImpl();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@OptionsItem(android.R.id.home)
	void homeIconClicked() {
		finish();
	}

	@ItemClick(R.id.feeds)
	void imageItemClicked(int position) {

		// refresh highlighting
		mFeedListView.setItemChecked(position, false);

		// clear current selection
		mLongClickedCJayImage = null;
		supportInvalidateOptionsMenu();

		Cursor cursor = (Cursor) mCursorAdapter.getItem(position);
		mSelectedCJayImageUuid = cursor.getString(cursor.getColumnIndexOrThrow("uuid"));

		String issueId = cursor.getString(cursor.getColumnIndexOrThrow("issue_id"));

		Logger.e("Click on " + issueId);

		if (!TextUtils.isEmpty(issueId)) {
			showIssueReport(mSelectedCJayImageUuid);
		} else {
			showReportDialog();
		}

	}

	@ItemLongClick(R.id.feeds)
	void imageItemLongClicked(int position) {

		// refresh highlighting
		mFeedListView.setItemChecked(position, true);

		// refresh menu
		Cursor cursor = (Cursor) mCursorAdapter.getItem(position);
		mLongClickedCJayImageUuid = cursor.getString(cursor.getColumnIndexOrThrow("uuid"));

		try {
			mLongClickedCJayImage = cJayImageDaoImpl.findByUuid(mLongClickedCJayImageUuid);
		} catch (SQLException e) {

			e.printStackTrace();
		}

		supportInvalidateOptionsMenu();
	}

	@Override
	public android.content.Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {

		Context context = this;
		return new CJayCustomCursorLoader(context) {

			@Override
			public Cursor loadInBackground() {
				Cursor cursor = DataCenter.getInstance().getIssueItemCursorByContainer(getContext(),
																						mContainerSessionUUID,
																						CJayImage.TYPE_REPORT);

				if (cursor != null) {
					// Ensure the cursor window is filled
					cursor.getCount();
					cursor.registerContentObserver(mObserver);
				}

				return cursor;
			}
		};
	}

	public void onEvent(CJayImageAddedEvent event) {
		Logger.Log("issue_report - " + event.getCJayImage().getUuid() + " - cjayimage added - " + event.getTag());
		if (event.getTag().equals(LOG_TAG)) {
			mNewImageCount++;
			mNewImageUUID = event.getCJayImage().getUuid();

			Logger.Log("issue_report - isRunning=" + isRunning());
			if (isRunning()) {
				if (mCursorAdapter != null) {
					Logger.Log("issue_report - refresh");
					refresh();
				}

				Logger.Log("issue_report - " + mNewImageUUID + " - start - imageCount=" + mNewImageCount);
				showReportDialogForNewImage();
			}
		}
	}

	@Override
	public void onLoaderReset(android.content.Loader<Cursor> loader) {
		mCursorAdapter.swapCursor(null);
	}

	@Override
	public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor cursor) {

		final Context context = this;

		if (mCursorAdapter == null) {
			mCursorAdapter = new IssueItemCursorAdapter(context, mItemLayout, cursor, 0);
			mFeedListView.setAdapter(mCursorAdapter);

		} else {
			mCursorAdapter.swapCursor(cursor);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean isDisplayed = !(mLongClickedCJayImage == null);

		menu.findItem(R.id.menu_trash).setVisible(isDisplayed);
		menu.findItem(R.id.menu_upload).setVisible(mContainerSession.isValidForUpload(CJayImage.TYPE_REPORT));

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onResume() {

		super.onResume();

		Logger.Log("issue_report - " + mNewImageUUID + " - start - imageCount=" + mNewImageCount);

		mSelectedCJayImageUuid = "";

		if (mNewImageCount > 1) {
			// when more than one images were taken continuously,
			// then go back to container list
			mNewImageCount = 0;
			onBackPressed();

		} else {
			Logger.Log("issue_report - refresh");

			try {
				containerSessionDaoImpl.refresh(mContainerSession);
			} catch (SQLException e) {
				e.printStackTrace();
			}

			if (mCursorAdapter != null) {
				// otherwise refresh the image list
				refresh();
			}

			if (mNewImageCount == 1 && !TextUtils.isEmpty(mNewImageUUID)) {
				// go to report issue after taking one picture
				showReportDialogForNewImage();
			}
		}
	}

	@UiThread
	public void refresh() {
		getLoaderManager().restartLoader(LOADER_ID, null, this);
		supportInvalidateOptionsMenu();
	}

	@Background
	void setWWContainer() {
		long startTime = System.currentTimeMillis();
		Logger.Log("*** Create water wash issue***");

		SQLiteDatabase db = DataCenter.getDatabaseHelper(context).getWritableDatabase();

		Cursor damageCursor = db.rawQuery("select id as _id from damage_code where code = ?", new String[] { "DB" });

		int damageId = 0;
		if (damageCursor.moveToFirst()) {
			damageId = damageCursor.getInt(damageCursor.getColumnIndexOrThrow("_id"));
		}

		Cursor repairCursor = db.rawQuery("select id as _id from repair_code where code = ?", new String[] { "WW" });

		int repairId = 0;
		if (repairCursor.moveToFirst()) {
			repairId = repairCursor.getInt(repairCursor.getColumnIndexOrThrow("_id"));
		}

		Cursor componentCursor = db.rawQuery("select id as _id from component_code where code = ?",	new String[] { "FWA" });

		int componentId = 0;
		if (componentCursor.moveToFirst()) {
			componentId = componentCursor.getInt(componentCursor.getColumnIndexOrThrow("_id"));
		}
		
		String issueId;
		String sql = "select _id from issue where componentCode_id = " + componentId + " and damageCode_id = " + damageId
				+ " and repairCode_id = " + repairId + " and locationCode = ? and containerSession_id = ?";
		Cursor issueCursor = db.rawQuery(sql, new String[] { "BXXX", mContainerSessionUUID });
		if (issueCursor.moveToFirst()) {
			// a WW issue already exists. Update quantity
			issueId = issueCursor.getString(repairCursor.getColumnIndexOrThrow("_id"));
			sql = "update issue set quantity = quantity + 1 where _id = " + Utils.sqlString(issueId);
			db.execSQL(sql);
		} else {
			// create a new WW issue
			issueId = UUID.randomUUID().toString();
			sql = "insert into issue "
					+ "(componentCode_id, containerSession_id, damageCode_id, _id, height, repairCode_id, length, locationCode, quantity, id, fixed) "
					+ " VALUES " + "(" + componentId + ", '" + mContainerSessionUUID + "', " + damageId + ", " + Utils.sqlString(issueId)
					+ ", NULL, " + repairId + ", NULL, 'BXXX', 1, 0, 0)";
			db.execSQL(sql);
		}
		
		// link issue to cjayimage
		sql = "UPDATE cjay_image SET issue_id = '" + issueId + "' WHERE uuid = '" + mSelectedCJayImageUuid + "'";
		db.execSQL(sql);
		
		try {
			containerSessionDaoImpl.refresh(mContainerSession);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		refresh();

		// cost 50ms - Vu: updated cost: ~150-170ms
		long difference = System.currentTimeMillis() - startTime;
		Logger.w("---> Total time: " + Long.toString(difference));
	}

	private void showIssueAssigment(String imageUuid) {

		Intent intent = new Intent(this, AuditorIssueAssigmentActivity_.class);
		intent.putExtra(AuditorIssueAssigmentActivity.CJAY_IMAGE_EXTRA, imageUuid);
		startActivity(intent);

	}

	private void showIssueReport(String imageUuid) {

		Intent intent = new Intent(this, AuditorIssueReportActivity_.class);
		intent.putExtra(AuditorIssueReportActivity.CJAY_IMAGE_EXTRA, imageUuid);
		startActivity(intent);

	}

	@Trace(level = Log.INFO)
	void showReportDialog() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this)
			.setMessage(R.string.dialog_report_message)
			.setTitle(R.string.dialog_report_title)
			.setPositiveButton(	R.string.dialog_report_no,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int id) {
	
										// Issue not reported, report issue
										showIssueReport(mSelectedCJayImageUuid);
									}
								})
			.setNegativeButton(	R.string.dialog_report_yes,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int id) {
	
										// The issue already reported, assign this image to that issue
										showIssueAssigment(mSelectedCJayImageUuid);
									}
								})
			.setNeutralButton(R.string.dialog_report_neutral,
								new OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
	
										setWWContainer();
									}
								});
		builder.show();
	}

	private void showReportDialogForNewImage() {
		if (isRunning() && mNewImageCount == 1 && !TextUtils.isEmpty(mNewImageUUID)) {
			Logger.Log("issue_report");

			// go to report issue after taking one picture
			mSelectedCJayImageUuid = mNewImageUUID;
			mNewImageCount = 0;
			mNewImageUUID = "";

			showReportDialog();
		}
	}

	@OptionsItem(R.id.menu_trash)
	void trashMenuItemClicked() {
		long startTime = System.currentTimeMillis();

		if (mLongClickedCJayImage != null) {
			Issue issue = mLongClickedCJayImage.getIssue();

			// update records in db
			try {

				cJayImageDaoImpl.delete(mLongClickedCJayImage);
				containerSessionDaoImpl.refresh(mContainerSession);
				issueDaoImpl.refresh(issue);
				if (issue.getCJayImages().size() == 0) {
					issueDaoImpl.delete(issue);
				}

			} catch (SQLException e) {
				e.printStackTrace();
			}

			// refresh image list
			refresh();

			// hide menu items
			mLongClickedCJayImage = null;
			mFeedListView.setItemChecked(-1, true);
			supportInvalidateOptionsMenu();
		}
		
		long difference = System.currentTimeMillis() - startTime;
		Logger.w("---> Total time: " + Long.toString(difference));
	}

	@OptionsItem(R.id.menu_upload)
	void uploadMenuItemClicked() {
		Logger.Log("Menu upload item clicked");

		if (mContainerSession.isValidForUpload(CJayImage.TYPE_REPORT)) {

			mContainerSession.setUploadType(ContainerSession.TYPE_AUDIT);
			CJayApplication.uploadContainerSesison(getApplicationContext(), mContainerSession);

		} else {

			Crouton.cancelAllCroutons();
			Crouton.makeText(this, R.string.alert_invalid_container, Style.ALERT).show();
		}

		// go back
		onBackPressed();
	}
}
