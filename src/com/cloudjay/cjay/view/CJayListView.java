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

	public void clearHighlighting() {
		setItemChecked(-1, true);
	}

	public ArrayList<T> getFeeds() {
		return mFeeds;
	}

	public FunDapter<T> getFeedsAdapter() {
		return mFeedsAdapter;
	}

	public ImageLoader getImageLoader() {
		return mImageLoader;
	}

	public T getItem(int position) {
		if (mFeedsAdapter != null) return mFeedsAdapter.getItem(position);
		return null;
	}

	public void highlightAt(int position) {
		setItemChecked(position, true);
	}

	private void init() {
		mImageLoader = ImageLoader.getInstance();
	}

	public abstract void initAdapter();

	public void setFeeds(ArrayList<T> feeds) {
		mFeeds = feeds;
		if (mFeedsAdapter != null) {
			mFeedsAdapter.updateData(feeds);
		}
	}

	public void setFeedsAdapter(FunDapter<T> feedsAdapter) {
		mFeedsAdapter = feedsAdapter;
		setAdapter(feedsAdapter);
	}
}
