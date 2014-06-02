package com.cloudjay.cjay.adapter;

import uk.co.senab.photoview.PhotoView;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.CJayImage;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

public class PhotoPagerAdapter extends PagerAdapter {

	private Cursor cursor;
	private ImageLoader imageLoader;

	public PhotoPagerAdapter(Context context, Cursor c) {
		cursor = c;
		imageLoader = ImageLoader.getInstance();
	}
	
	 public void swapCursor(Cursor c) {
		 if (cursor == c)
			 return;
		  
		 this.cursor = c;
		 notifyDataSetChanged();
	 }

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
	}

	@Override
	public int getCount() {
		return cursor != null ? cursor.getCount() : 0;
	}

	@Override
	public View instantiateItem(ViewGroup container, int position) {
		final PhotoView photoView = new PhotoView(container.getContext());
		String url = null;
		
		if (cursor != null) {
			position = position % cursor.getCount();
		    cursor.moveToPosition(position);
			url = cursor.getString(cursor.getColumnIndexOrThrow(CJayImage.FIELD_URI));
		}
	    
		if (!TextUtils.isEmpty(url)) {
			imageLoader.loadImage(url, new SimpleImageLoadingListener() {
				@Override
				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
					photoView.setImageBitmap(loadedImage);
				}
			});
		} else {
			photoView.setImageResource(R.drawable.ic_app_circle);
		}

		// Now just add PhotoView to ViewPager and return it
		container.addView(photoView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		return photoView;
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}
}
