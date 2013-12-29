package com.cloudjay.cjay.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.view.UploadItemLayout;

public class UploadsListBaseAdapter extends BaseAdapter {

	private List<ContainerSession> mItems;

	private final Context mContext;
	private final LayoutInflater mLayoutInflater;

	public UploadsListBaseAdapter(Context context) {
		mContext = context;
		mLayoutInflater = LayoutInflater.from(mContext);

		// TODO: TIEUBAO query for list item
		// mItems = mController.getUploadingUploads();
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

		UploadItemLayout layout = (UploadItemLayout) view;
		layout.setPhotoSelection(getItem(position));

		return view;
	}

	@Override
	public void notifyDataSetChanged() {

		// TODO: TIEUBAO Update list items
		// mItems = mController.getUploadingUploads();

		super.notifyDataSetChanged();
	}

}
