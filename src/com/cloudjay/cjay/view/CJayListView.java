package com.cloudjay.cjay.view;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

import com.ami.fundapter.FunDapter;
import com.nostra13.universalimageloader.core.ImageLoader;

public abstract class CJayListView<T> extends ListView {
	private ArrayList<T> mFeeds;
	private FunDapter<T> mFeedsAdapter;
	private ImageLoader mImageLoader;
	
	public CJayListView(Context context) {
		super(context);
		init();
	}

	public CJayListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public CJayListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	private void init() {
		mImageLoader = ImageLoader.getInstance();
	}

	public abstract void initAdapter();
	
	public ImageLoader getImageLoader() {
		return mImageLoader;
	}
	
	public void setFeedsAdapter(FunDapter<T> feedsAdapter) {
		mFeedsAdapter = feedsAdapter;
		this.setAdapter(feedsAdapter);
	}
	
	public FunDapter<T> getFeedsAdapter() {
		return mFeedsAdapter;
	}
	
	public void setFeeds(ArrayList<T> feeds) {
		mFeeds = feeds;
		if (mFeedsAdapter != null) {
			mFeedsAdapter.updateData(feeds);
		}
	}
	
	public ArrayList<T> getFeeds() {
		return mFeeds;
	}
	
	public T getItem(int position) {
		if (mFeedsAdapter != null) {
			return mFeedsAdapter.getItem(position);
		}
		return null;
	}
	
	public void highlightAt(int position) {
		this.setItemChecked(position, true);
	}
	
	public void clearHighlighting() {
		this.setItemChecked(-1, true);
	}
}
