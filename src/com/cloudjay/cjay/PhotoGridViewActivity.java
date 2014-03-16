package com.cloudjay.cjay;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.cloudjay.cjay.adapter.PhotoGridViewCursorAdapter;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.CJayCursorLoader;
import com.cloudjay.cjay.util.DataCenter;

@EActivity(R.layout.activity_photo_gridview)
public class PhotoGridViewActivity extends CJayActivity implements
		LoaderCallbacks<Cursor> {

	private final static int LOADER_ID = CJayConstant.CURSOR_LOADER_ID_PHOTO_GRIDVIEW;
	public static final String CJAY_CONTAINER_SESSION_EXTRA = "cjay_container_session";
	public static final String CJAY_IMAGE_TYPE_EXTRA = "cjay_image_type";

	PhotoGridViewCursorAdapter mCursorAdapter;
	int mItemLayout;

	@Extra(CJAY_CONTAINER_SESSION_EXTRA)
	String mContainerSessionUUID = "";

	@Extra(CJAY_IMAGE_TYPE_EXTRA)
	int mCJayImageType = CJayImage.TYPE_IMPORT;

	@ViewById(R.id.gridview)
	GridView mGridView;

	@AfterViews
	void afterViews() {
		mItemLayout = R.layout.grid_item_image;
		getSupportLoaderManager().initLoader(LOADER_ID, null, this);
		
		final Context ctx = this;
		mGridView.setOnItemClickListener(new OnItemClickListener() {
	        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	    		Intent intent = new Intent(ctx, PhotoViewPagerActivity_.class);
	    		intent.putExtra(PhotoViewPagerActivity_.START_POSITION, position);
	    		intent.putExtra(PhotoViewPagerActivity_.CJAY_CONTAINER_SESSION_EXTRA,
	    				mContainerSessionUUID);
	    		intent.putExtra(PhotoViewPagerActivity_.CJAY_IMAGE_TYPE_EXTRA,
	    				mCJayImageType);
	    		startActivity(intent);
	        }
	    });
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		Context context = this;

		return new CJayCursorLoader(context) {
			@Override
			public Cursor loadInBackground() {
				Cursor cursor = DataCenter.getInstance()
						.getCJayImagesByContainer(getContext(),
								mContainerSessionUUID, mCJayImageType);

				if (cursor != null) {
					// Ensure the cursor window is filled
					cursor.getCount();
					cursor.registerContentObserver(mObserver);
				}

				return cursor;
			}
		};
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		final Context context = this;

		if (mCursorAdapter == null) {
			mCursorAdapter = new PhotoGridViewCursorAdapter(context,
					mItemLayout, cursor, 0);
			mGridView.setAdapter(mCursorAdapter);
		} else {
			mCursorAdapter.swapCursor(cursor);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		mCursorAdapter.swapCursor(null);
	}
}