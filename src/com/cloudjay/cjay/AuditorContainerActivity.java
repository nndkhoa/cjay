package com.cloudjay.cjay;

import java.sql.SQLException;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ItemLongClick;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.cloudjay.cjay.adapter.IssueItemCursorAdapter;
import com.cloudjay.cjay.dao.CJayImageDaoImpl;
import com.cloudjay.cjay.dao.ComponentCodeDaoImpl;
import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.dao.DamageCodeDaoImpl;
import com.cloudjay.cjay.dao.IssueDaoImpl;
import com.cloudjay.cjay.dao.RepairCodeDaoImpl;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.Issue;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.CJayCustomCursorLoader;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.UploadType;
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

	private ContainerSessionDaoImpl containerSessionDaoImpl = null;
	private CJayImageDaoImpl cJayImageDaoImpl = null;
	private IssueDaoImpl issueDaoImpl = null;

	private int mItemLayout = R.layout.list_item_issue;
	private IssueItemCursorAdapter mCursorAdapter;
	private final static int LOADER_ID = CJayConstant.CURSOR_LOADER_ID_ISSUE_ITEM;

	private MenuItem avMenuItem;

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

		// Set Activity Title
		containerIdTextView.setText(mContainerSession.getContainerId());
		setTitle(mContainerSession.getContainerId());
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
		hideMenuItems();

		// go to camera
		mNewImageCount = 0;
		mNewImageUUID = "";
		CJayApplication.openCamera(this, mContainerSession, CJayImage.TYPE_AUDIT, LOG_TAG);
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

	@ItemClick(R.id.feeds)
	void imageItemClicked(int position) {

		// refresh highlighting and clear current selection
		hideMenuItems();

		Cursor cursor = (Cursor) mCursorAdapter.getItem(position);
		mSelectedCJayImageUuid = cursor.getString(cursor.getColumnIndexOrThrow("uuid"));

		String issueId = cursor.getString(cursor.getColumnIndexOrThrow("issue_id"));

		if (!TextUtils.isEmpty(issueId)) {
			CJayApplication.openIssueReport(this, mSelectedCJayImageUuid);
		} else {
			CJayApplication.openReportDialog(this, mSelectedCJayImageUuid, mContainerSessionUUID);
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
			Issue issue = mLongClickedCJayImage.getIssue();
			if (issue != null) {
				issue.equals(issue);
			}
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
																						CJayImage.TYPE_AUDIT);

				if (cursor != null) {
					// Ensure the cursor window is filled
					cursor.registerContentObserver(mObserver);
				}

				return cursor;
			}
		};
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
		menu.findItem(R.id.menu_upload).setVisible(mContainerSession.isValidForUpload(this, CJayImage.TYPE_AUDIT));

		avMenuItem = menu.findItem(R.id.menu_av);
		// avMenuItem.setIcon(mContainerSession.isAvailable() ? R.drawable.ic_action_good : R.drawable.ic_action_bad);
		avMenuItem.setIcon(mContainerSession.isAvailable() ? R.drawable.ic_menu_av : R.drawable.ic_menu_no_av);

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

			if (mCursorAdapter != null) {
				// otherwise refresh the image list
				refresh();
			}
		}
	}

	@UiThread
	public void refresh() {
		try {
			containerSessionDaoImpl.refresh(mContainerSession);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		getLoaderManager().restartLoader(LOADER_ID, null, this);
		supportInvalidateOptionsMenu();
	}

	@OptionsItem(R.id.menu_av)
	void avMenuItemClicked() {

		if (avMenuItem != null) {
			mContainerSession.setAvailable(!mContainerSession.isAvailable());
			mContainerSession.updateField(	this, ContainerSession.FIELD_AV,
											Integer.toString(Utils.toInt(mContainerSession.isAvailable())));

			// avMenuItem.setIcon(mContainerSession.isAvailable() ? R.drawable.ic_action_good :
			// R.drawable.ic_action_bad);
			avMenuItem.setIcon(mContainerSession.isAvailable() ? R.drawable.ic_menu_av : R.drawable.ic_menu_no_av);
		}

	}

	@OptionsItem(R.id.menu_import)
	void importMenuItemClicked() {

		Logger.Log("container session id: " + mContainerSessionUUID);

		hideMenuItems();
		CJayApplication.openPhotoGridViewForImport(	this, mContainerSessionUUID, mContainerSession.getContainerId(),
													CJayImage.TYPE_IMPORT, CJayImage.TYPE_AUDIT, LOG_TAG);
	}

	@OptionsItem(R.id.menu_trash)
	void trashMenuItemClicked() {
		long startTime = System.currentTimeMillis();

		if (mLongClickedCJayImage != null) {
			Issue issue = mLongClickedCJayImage.getIssue();

			// update records in db
			try {
				cJayImageDaoImpl.delete(mLongClickedCJayImage);
				if (issue != null) {
					issueDaoImpl.refresh(issue);
					if (issue.getCJayImages().size() == 0) {
						issueDaoImpl.delete(issue);
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

			// refresh image list
			refresh();

			// hide menu items
			hideMenuItems();
		}

		long difference = System.currentTimeMillis() - startTime;
		Logger.w("---> Total time: " + Long.toString(difference));
	}

	@OptionsItem(R.id.menu_upload)
	void uploadMenuItemClicked() {
		Logger.Log("Menu upload item clicked");

		if (mContainerSession.isValidForUpload(this, CJayImage.TYPE_AUDIT)) {

			mContainerSession.setUploadType(UploadType.AUDIT);
			CJayApplication.uploadContainerSesison(getApplicationContext(), mContainerSession);

		} else {

			Crouton.cancelAllCroutons();
			Crouton.makeText(this, R.string.alert_invalid_container, Style.ALERT).show();
		}

		// go back
		onBackPressed();
	}

	private void hideMenuItems() {
		mLongClickedCJayImage = null;
		mFeedListView.setItemChecked(-1, true);
		supportInvalidateOptionsMenu();
	}
}
