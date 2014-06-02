package com.cloudjay.cjay;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;

import com.cloudjay.cjay.adapter.PhotoPagerAdapter;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.CJayCustomCursorLoader;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.view.HackyViewPager;

@EActivity(R.layout.activity_view_pager)
public class PhotoViewPagerActivity extends CJayActivity implements LoaderCallbacks<Cursor> {

	public static final String CJAY_CONTAINER_SESSION_EXTRA = "cjay_container_session";
	public static final String CJAY_ISSUE_TYPE_EXTRA = "cjay_issue";
	public static final String CJAY_IMAGE_TYPE_EXTRA = "cjay_image_type";
	public static final String START_POSITION = "start_pos";
	
	private final static int LOADER_ID = CJayConstant.CURSOR_LOADER_ID_PHOTO_PAGER;

	@Extra(START_POSITION)
	int mStartPos = 0;

	@Extra(CJAY_ISSUE_TYPE_EXTRA)
	String mIssueUUID = "";

	@Extra(CJAY_CONTAINER_SESSION_EXTRA)
	String mContainerSessionUUID = "";

	@Extra(CJAY_IMAGE_TYPE_EXTRA)
	int mImageType = CJayImage.TYPE_IMPORT;

	@Extra("title")
	String mTitle = "";

	@ViewById(R.id.view_pager)
	HackyViewPager mViewPager;
	
	PhotoPagerAdapter mPagerAdapter;

	@AfterViews
	void afterViews() {
		// Set Activity Title
		setTitle(mTitle);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// init cursor 
		getLoaderManager().initLoader(LOADER_ID, null, this);
	}

	@OptionsItem(android.R.id.home)
	void homeIconClicked() {
		finish();
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		return new CJayCustomCursorLoader(this) {
			@Override
			public Cursor loadInBackground() {
				Cursor cursor;
				if (!TextUtils.isEmpty(mIssueUUID)) {
					cursor = DataCenter.getInstance().getCJayImagesCursorByContainer(getContext(), mContainerSessionUUID, 
																						mIssueUUID, mImageType);
				} else {
					cursor = DataCenter.getInstance().getCJayImagesCursorByContainer(getContext(), mContainerSessionUUID, 
																						mImageType);
				}
				
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
		mPagerAdapter.swapCursor(null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (mPagerAdapter == null) {
			mPagerAdapter = new PhotoPagerAdapter(this, cursor);
			mViewPager.setAdapter(mPagerAdapter);
		} else {
			mPagerAdapter.swapCursor(cursor);
		}
		mViewPager.setCurrentItem(mStartPos);
	}
	
}