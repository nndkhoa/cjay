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
package com.cloudjay.cjay.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

import com.cloudjay.cjay.CJayApplication;
import com.cloudjay.cjay.PhotoUploadController;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.TmpContainerSession;
import com.cloudjay.cjay.view.UploadItemLayout;

public class UploadsListBaseAdapter extends BaseAdapter {

	private List<TmpContainerSession> mItems;

	private final Context mContext;
	private final LayoutInflater mLayoutInflater;
	private final PhotoUploadController mController;

	public UploadsListBaseAdapter(Context context) {
		mContext = context;
		mLayoutInflater = LayoutInflater.from(mContext);

		CJayApplication app = CJayApplication.getApplication(context);
		mController = app.getPhotoUploadController();
		mItems = mController.getUploadingUploads();
	}

	public int getCount() {
		return null != mItems ? mItems.size() : 0;
	}

	public long getItemId(int position) {
		return position;
	}

	public TmpContainerSession getItem(int position) {
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
		mItems = mController.getUploadingUploads();
		super.notifyDataSetChanged();
	}

}
