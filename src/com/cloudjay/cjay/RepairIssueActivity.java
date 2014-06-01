package com.cloudjay.cjay;

import java.sql.SQLException;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.widget.GridView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.cloudjay.cjay.adapter.PhotoGridViewCursorAdapter;
import com.cloudjay.cjay.dao.IssueDaoImpl;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.Issue;
import com.cloudjay.cjay.network.CJayClient;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.CJayCustomCursorLoader;
import com.cloudjay.cjay.util.DataCenter;

/**
 * 
 * Danh sách hình của một issue
 * 
 * @author quocvule
 * 
 */
@EActivity(R.layout.activity_repair_issue)
@OptionsMenu(R.menu.menu_repair_issue)
public class RepairIssueActivity extends CJayActivity implements LoaderCallbacks<Cursor> {

	public static final String CJAY_ISSUE_EXTRA = "issue";
	private final static int LOADER_ID = CJayConstant.CURSOR_LOADER_ID_PHOTO_GD_1;
	
	private int mItemLayout = R.layout.grid_item_image;
	private PhotoGridViewCursorAdapter mCursorAdapter;

	private Issue mIssue;
	private IssueDaoImpl mIssueDaoImpl;

	@ViewById
	ViewPager pager;

	@ViewById(R.id.issue_textview)
	TextView issueTextView;

	@Extra(CJAY_ISSUE_EXTRA)
	String mIssueUUID = "";

	@ViewById(R.id.gridview)
	GridView mGridView;
	
	@AfterViews
	void afterViews() {
		try {
			mIssueDaoImpl = CJayClient.getInstance().getDatabaseManager().getHelper(this).getIssueDaoImpl();
			mIssue = mIssueDaoImpl.queryForId(mIssueUUID);

			if (null != mIssue) {
				setTitle(mIssue.getContainerSession().getContainerId());
				getSupportActionBar().setDisplayHomeAsUpEnabled(true);
				
				issueTextView.setText(mIssue.getLocationCode() + " " + mIssue.getDamageCodeString() + " "
						+ mIssue.getRepairCodeString());
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		getLoaderManager().initLoader(LOADER_ID, null, this);
	}

	@OptionsItem(R.id.menu_check)
	void checkMenuItemSelected() {
		// set fixed to true
		mIssue.setFixed(true);

		// save db records
		try {
			IssueDaoImpl issueDaoImpl = CJayClient.getInstance().getDatabaseManager().getHelper(this).getIssueDaoImpl();
			issueDaoImpl.createOrUpdate(mIssue);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// go back
		onBackPressed();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// only show check menu item if has TYPE_REPAIRED images
		boolean hasRepaired = false;

		if (null != mIssue) {
			try {
				mIssueDaoImpl.refresh(mIssue);
			} catch (SQLException e) {
				e.printStackTrace();
			}

			for (CJayImage cJayImage : mIssue.getCJayImages()) {
				if (cJayImage.getType() == CJayImage.TYPE_REPAIRED) {
					hasRepaired = true;
					break;
				}
			}
		}
		menu.findItem(R.id.menu_check).setVisible(hasRepaired);

		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public void onResume() {
		// refresh list
		if (mCursorAdapter != null) {
			getLoaderManager().restartLoader(LOADER_ID, null, this);
		}
		super.onResume();
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		return new CJayCustomCursorLoader(this) {
			@Override
			public Cursor loadInBackground() {
				Cursor cursor = DataCenter.getInstance().getCJayImagesCursorByIssue(getContext(), mIssueUUID);
				
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
		if (mCursorAdapter == null) {
			mCursorAdapter = new PhotoGridViewCursorAdapter(this, mItemLayout, cursor, 0);
			mGridView.setAdapter(mCursorAdapter);
		} else {
			mCursorAdapter.swapCursor(cursor);
		}
	}
}
