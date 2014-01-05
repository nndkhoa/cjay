package com.cloudjay.cjay.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.List;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.view.UploadItemLayout;
import com.nostra13.universalimageloader.core.ImageLoader;

public class UploadsListBaseAdapter extends BaseAdapter {

	private List<ContainerSession> mItems;

	private final Context mContext;
	private final LayoutInflater mLayoutInflater;
	private ImageLoader imageLoader;

	public UploadsListBaseAdapter(Context context) {
		mContext = context;
		mLayoutInflater = LayoutInflater.from(mContext);
		mItems = DataCenter.getInstance().getListUploadContainerSessions(
				mContext);
		imageLoader = ImageLoader.getInstance();
	}

	public UploadsListBaseAdapter(Context context,
			List<ContainerSession> listItems) {
		mContext = context;
		mLayoutInflater = LayoutInflater.from(mContext);
		mItems = listItems;
		imageLoader = ImageLoader.getInstance();
	}

	public void setContainerSessions(List<ContainerSession> listItems) {
		this.mItems = listItems;
	}

	public int getCount() {
		return null != mItems ? mItems.size() : 0;
	}

	public long getItemId(int position) {
		return position;
	}

	public ContainerSession getItem(int position) {
		return mItems.get(position);
	}

	public View getView(int position, View view, ViewGroup parent) {

		if (null == view) {
			view = mLayoutInflater.inflate(R.layout.item_list_upload, parent,
					false);
		}

		ContainerSession containerSession = getItem(position);

		UploadItemLayout layout = (UploadItemLayout) view;
		layout.setContainerSession(containerSession);

		return view;
	}

	// @Override
	// public void notifyDataSetChanged() {
	//
	// // Update list items
	// if (null != mContext)
	// mItems = DataCenter.getInstance().getListUploadContainerSessions(
	// mContext);
	//
	// super.notifyDataSetChanged();
	// }
}
