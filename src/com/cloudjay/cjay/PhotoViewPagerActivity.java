package com.cloudjay.cjay;

import java.sql.SQLException;
import java.util.ArrayList;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

import uk.co.senab.photoview.PhotoView;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.network.CJayClient;
import com.cloudjay.cjay.view.HackyViewPager;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

@EActivity(R.layout.activity_view_pager)
public class PhotoViewPagerActivity extends CJayActivity {

	public static final String CJAY_CONTAINER_SESSION_EXTRA = "cjay_container_session";
	public static final String CJAY_IMAGE_TYPE_EXTRA = "cjay_image_type";
	public static final String START_POSITION = "start_pos";
	
	@Extra(START_POSITION)
	int mStartPos = 0;
	
	@Extra(CJAY_CONTAINER_SESSION_EXTRA)
	String mContainerSessionUUID = "";

	@Extra(CJAY_IMAGE_TYPE_EXTRA)
	int mCJayImageType = CJayImage.TYPE_IMPORT;
	
	@Extra("title")
	String mTitle = "";
	
	@ViewById(R.id.view_pager)
	HackyViewPager mViewPager;
    
    @AfterViews
    void afterViews() {
		// Set Activity Title
		setTitle(mTitle);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		mViewPager.setAdapter(new PhotoPagerAdapter(this, mContainerSessionUUID, mCJayImageType));
		mViewPager.setCurrentItem(mStartPos);
    }

	@OptionsItem(android.R.id.home)
	void homeIconClicked() {
		finish();
	}
	
	static class PhotoPagerAdapter extends PagerAdapter {

		private ArrayList<CJayImage> mCJayImages;
		private ImageLoader mImageLoader;

		public PhotoPagerAdapter(Context ctx, String containerSessionUUID, int imageType) {
			try {
				ContainerSessionDaoImpl containerSessionDaoImpl = CJayClient.getInstance()
						.getDatabaseManager().getHelper(ctx)
						.getContainerSessionDaoImpl();
				ContainerSession containerSession = containerSessionDaoImpl.queryForId(containerSessionUUID);

				mImageLoader = ImageLoader.getInstance();
				mCJayImages = new ArrayList<CJayImage>();
				
				if (null != containerSession) {
					for (CJayImage cJayImage : containerSession.getCJayImages()) {
						if (cJayImage.getType() == imageType) {
							mCJayImages.add(cJayImage);
						}
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public int getCount() {
			return mCJayImages.size();
		}

		@Override
		public View instantiateItem(ViewGroup container, int position) {
			final PhotoView photoView = new PhotoView(container.getContext());
			
			mImageLoader.loadImage(mCJayImages.get(position).getUri(), new SimpleImageLoadingListener() {
			    @Override
			    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
			    	photoView.setImageBitmap(loadedImage);
			    }
			});

			// Now just add PhotoView to ViewPager and return it
			container.addView(photoView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

			return photoView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

	}

}
