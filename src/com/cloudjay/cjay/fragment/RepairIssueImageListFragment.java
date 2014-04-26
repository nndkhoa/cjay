package com.cloudjay.cjay.fragment;

import java.sql.SQLException;
import java.util.ArrayList;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageButton;

import com.actionbarsherlock.app.SherlockFragment;
import com.cloudjay.cjay.CJayApplication;
import com.cloudjay.cjay.*;
import com.cloudjay.cjay.adapter.PhotoGridViewCursorAdapter;
import com.cloudjay.cjay.dao.IssueDaoImpl;
import com.cloudjay.cjay.events.CJayImageAddedEvent;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.Issue;
import com.cloudjay.cjay.network.CJayClient;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.CJayCursorLoader;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.Utils;

import de.greenrobot.event.EventBus;

@EFragment(R.layout.fragment_repair_issue_image_list)
public class RepairIssueImageListFragment extends SherlockFragment implements LoaderCallbacks<Cursor> {

	private final String LOG_TAG = "RepairIssueImageListFragment";
	private final static int LOADER_ID = CJayConstant.CURSOR_LOADER_ID_PHOTO_GD_1;

	public final static String CJAY_ISSUE_UUID = "issueUUID";
	public final static String CJAY_IMAGE_TYPE = "imageType";

	private ArrayList<CJayImage> mTakenImages;
	private Issue mIssue;
	private String mIssueUUID;
	private int mType;
	private int mItemLayout;
	private PhotoGridViewCursorAdapter mCursorAdapter;

	@ViewById(R.id.gridview)
	GridView mGridView;

	@ViewById(R.id.btn_add_new)
	ImageButton mCameraButton;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mIssueUUID = getArguments().getString(CJAY_ISSUE_UUID);
		mType = getArguments().getInt(CJAY_IMAGE_TYPE);
		return null;
	}

	@AfterViews
	void afterViews() {
		// show or hide camera button
		if (mType == CJayImage.TYPE_REPORT) {
			mCameraButton.setVisibility(View.GONE);
		} else {
			mCameraButton.setVisibility(View.VISIBLE);
		}

		try {
			IssueDaoImpl issueDaoImpl = CJayClient.getInstance().getDatabaseManager().getHelper(getActivity())
													.getIssueDaoImpl();
			mIssue = issueDaoImpl.queryForId(mIssueUUID);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		getLoaderManager().initLoader(LOADER_ID, null, this);

		mItemLayout = R.layout.grid_item_image;

		final Context ctx = getActivity();
		mGridView.setEmptyView(getActivity().findViewById(android.R.id.empty));
		mGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				Intent intent = new Intent(ctx, PhotoViewPagerActivity_.class);

				intent.putExtra(PhotoViewPagerActivity_.START_POSITION, position);
				intent.putExtra(PhotoViewPagerActivity_.CJAY_CONTAINER_SESSION_EXTRA, mIssue.getContainerSession()
																							.getUuid());
				intent.putExtra(PhotoViewPagerActivity_.CJAY_ISSUE_TYPE_EXTRA, mIssueUUID);
				intent.putExtra(PhotoViewPagerActivity_.CJAY_IMAGE_TYPE_EXTRA, mType);
				intent.putExtra("title", Utils.getImageTypeDescription(ctx, mType));
				ctx.startActivity(intent);
			}
		});
	}

	@Click(R.id.btn_add_new)
	void cameraClicked() {
		if (mTakenImages == null) {
			mTakenImages = new ArrayList<CJayImage>();
		}

		CJayApplication.openCamera(getActivity(), mIssue.getContainerSession(), mType, LOG_TAG);
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

	public void onEvent(CJayImageAddedEvent event) {
		if (event == null) {
			Logger.Log("Event is null");
		} else {
			// retrieve image
			try {
				if (event.getTag().equals(LOG_TAG) && mTakenImages != null) {
					mTakenImages.add(event.getCJayImage());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onResume() {
		// update new images and database
		Logger.Log("onResume");
		if (mTakenImages != null && mTakenImages.size() > 0) {

			for (CJayImage cJayImage : mTakenImages) {
				SQLiteDatabase db = DataCenter.getDatabaseHelper(getActivity().getApplicationContext())
												.getWritableDatabase();
				ContentValues values = new ContentValues();
				values.put("issue_id", mIssueUUID);
				values.put("containerSession_id", mIssue.getContainerSession().getUuid());
				db.update("cjay_image", values, "uuid LIKE ? ", new String[] { cJayImage.getUuid() });
				// Logger.Log("updated cjayimage issue_id: " + mIssueUUID);
				// Logger.Log("updated cjayimage containerSession_id: " + mIssue.getContainerSession().getUuid());
				// Logger.Log("updated cjayimage cjay_image: " + cJayImage.getUuid());
				// Logger.Log("updated cjayimage: " + rows);
			}

			// refresh menu
			getActivity().supportInvalidateOptionsMenu();

			mTakenImages.clear();
			mTakenImages = null;
		}

		// refresh list
		if (mCursorAdapter != null) {
			getLoaderManager().restartLoader(LOADER_ID, null, this);
		}
		super.onResume();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		return new CJayCursorLoader(getActivity()) {
			@Override
			public Cursor loadInBackground() {
				Cursor cursor = DataCenter.getInstance().getCJayImagesCursorByIssue(getContext(), mIssueUUID, mType);
				
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
			mCursorAdapter = new PhotoGridViewCursorAdapter(getActivity(), mItemLayout, cursor, 0);
			mGridView.setAdapter(mCursorAdapter);
		} else {
			mCursorAdapter.swapCursor(cursor);
		}
	}
}
