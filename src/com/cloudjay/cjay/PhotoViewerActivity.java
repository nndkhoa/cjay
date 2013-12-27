/*
 * Copyright 2013 Chris Banes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloudjay.cjay;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;
import android.view.animation.Animation;

import com.cloudjay.cjay.adapter.SelectedPhotosViewPagerAdapter;
import com.cloudjay.cjay.base.PhotupFragmentActivity;
import com.cloudjay.cjay.listener.OnSingleTapListener;
import com.cloudjay.cjay.model.TmpContainerSession;
import com.cloudjay.cjay.util.CursorPagerAdapter;
import com.cloudjay.cjay.util.MediaStoreCursorHelper;
import com.cloudjay.cjay.util.PhotupCursorLoader;
import com.cloudjay.cjay.view.MultiTouchImageView;
import com.cloudjay.cjay.view.PhotoTagItemLayout;

import de.greenrobot.event.EventBus;

public class PhotoViewerActivity extends PhotupFragmentActivity implements
		LoaderManager.LoaderCallbacks<Cursor> {

	public static final String EXTRA_POSITION = "extra_position";
	public static final String EXTRA_MODE = "extra_mode";
	public static final String EXTRA_BUCKET_ID = "extra_bucket_id";

	public static int MODE_ALL_VALUE = 100;
	public static int MODE_SELECTED_VALUE = 101;

	static final int REQUEST_CROP_PHOTO = 200;

	private ViewPager mViewPager;
	private PagerAdapter mAdapter;
	private ViewGroup mContentView;

	private Animation mFadeOutAnimation;
	private PhotoUploadController mController;

	private boolean mIgnoreFilterCheckCallback = false;

	private int mMode = MODE_SELECTED_VALUE;
	private String mBucketId;
	private int mRequestedPosition = -1;

	private void resetCurrentPhoto() {
		PhotoTagItemLayout currentView = getCurrentView();
		TmpContainerSession upload = currentView.getPhotoSelection();

		// upload.reset();
		reloadView(currentView);
	}

	public void onPageSelected(int position) {
		PhotoTagItemLayout currentView = getCurrentView();

		if (null != currentView) {
			TmpContainerSession upload = currentView.getPhotoSelection();

			if (null != upload) {
				getSupportActionBar().setTitle(upload.toString());
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_photo_viewer);
		mContentView = (ViewGroup) findViewById(R.id.fl_root);

		mController = PhotoUploadController.getFromContext(this);
		EventBus.getDefault().register(this);

		final Intent intent = getIntent();
		mMode = intent.getIntExtra(EXTRA_MODE, MODE_ALL_VALUE);

		if (mMode == MODE_ALL_VALUE) {
			mBucketId = intent.getStringExtra(EXTRA_BUCKET_ID);
		}

		mViewPager = (ViewPager) findViewById(R.id.vp_photos);
		mViewPager.setOffscreenPageLimit(2);
		mViewPager.setPageMargin(getResources().getDimensionPixelSize(
				R.dimen.viewpager_margin));

		mAdapter = new SelectedPhotosViewPagerAdapter(this,
				new OnSingleTapListener() {
					@Override
					public boolean onSingleTap() {
						return false;
					}
				});

		mViewPager.setAdapter(mAdapter);

		if (intent.hasExtra(EXTRA_POSITION)) {
			mRequestedPosition = intent.getIntExtra(EXTRA_POSITION, 0);
			mViewPager.setCurrentItem(mRequestedPosition);
		}

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(" ");

		// /**
		// * Nasty hack, basically we need to know when the ViewPager is laid
		// out,
		// * we then manually call onPageSelected. This is to fix onPageSelected
		// * not being called on the first item.
		// */
		// mViewPager.getViewTreeObserver().addOnGlobalLayoutListener(
		// new OnGlobalLayoutListener() {
		// @SuppressWarnings("deprecation")
		// public void onGlobalLayout() {
		// mViewPager.getViewTreeObserver()
		// .removeGlobalOnLayoutListener(this);
		// onPageSelected(mViewPager.getCurrentItem());
		// showTapToTagPrompt();
		// }
		// });
	}

	@Override
	protected void onDestroy() {
		EventBus.getDefault().unregister(this);
		// mController.updateDatabase();
		super.onDestroy();
	}

	// private PhotoUpload getCurrentUpload() {
	// PhotoTagItemLayout view = getCurrentView();
	// if (null != view) {
	// return view.getPhotoSelection();
	// }
	// return null;
	// }

	private PhotoTagItemLayout getCurrentView() {
		final int currentPos = mViewPager.getCurrentItem();

		for (int i = 0, z = mViewPager.getChildCount(); i < z; i++) {
			PhotoTagItemLayout child = (PhotoTagItemLayout) mViewPager
					.getChildAt(i);
			if (null != child && child.getPosition() == currentPos) {
				return child;
			}
		}

		return null;
	}

	private void reloadView(PhotoTagItemLayout currentView) {
		if (null != currentView) {
			MultiTouchImageView imageView = currentView.getImageView();
			TmpContainerSession selection = currentView.getPhotoSelection();
			imageView.requestFullSize(selection, true, false, null);
		}
	}

	public void onPhotoLoadStatusChanged(boolean finished) {
		// TODO Fix this setProgressBarIndeterminateVisibility(!finished);
	}

	public Loader<Cursor> onCreateLoader(int id, Bundle params) {
		String selection = null;
		String[] selectionArgs = null;
		if (null != mBucketId) {
			selection = Images.Media.BUCKET_ID + " = ?";
			selectionArgs = new String[] { mBucketId };
		}

		return new PhotupCursorLoader(this,
				MediaStoreCursorHelper.MEDIA_STORE_CONTENT_URI,
				MediaStoreCursorHelper.PHOTOS_PROJECTION, selection,
				selectionArgs, MediaStoreCursorHelper.PHOTOS_ORDER_BY, false);
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (mAdapter instanceof CursorPagerAdapter) {
			((CursorPagerAdapter) mAdapter).swapCursor(cursor);
		}

		if (mRequestedPosition != -1) {
			mViewPager.setCurrentItem(mRequestedPosition, false);
			mRequestedPosition = -1;
		}
	}

	public void onLoaderReset(Loader<Cursor> loader) {
		onLoadFinished(loader, null);
	}
}
