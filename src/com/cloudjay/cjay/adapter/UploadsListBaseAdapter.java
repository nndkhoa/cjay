package com.cloudjay.cjay.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.view.UploadItemLayout;

public class UploadsListBaseAdapter extends BaseAdapter {

	private List<ContainerSession> mItems;

	private final Context mContext;
	private final LayoutInflater mLayoutInflater;

	public UploadsListBaseAdapter(Context context) {
		mContext = context;
		mLayoutInflater = LayoutInflater.from(mContext);
		mItems = DataCenter.getInstance().getListUploadContainerSessions(mContext);
	}

	public UploadsListBaseAdapter(Context context, List<ContainerSession> listItems) {
		mContext = context;
		mLayoutInflater = LayoutInflater.from(mContext);
		mItems = listItems;

	}

	@Override
	public int getCount() {
		return null != mItems ? mItems.size() : 0;
	}

	@Override
	public ContainerSession getItem(int position) {
		return mItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {

		if (null == view) {
			view = mLayoutInflater.inflate(R.layout.item_list_upload, parent, false);
		}

		ContainerSession containerSession = getItem(position);

		UploadItemLayout layout = (UploadItemLayout) view;
		layout.setContainerSession(containerSession);

		return view;
	}

	public void setContainerSessions(List<ContainerSession> listItems) {
		mItems = listItems;
	}
}
