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
package com.cloudjay.cjay.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.adapter.PhotoGridViewCursorAdapter;


public class CheckablePhotoItemLayout extends CheckableFrameLayout implements
		View.OnClickListener {

	private final SquareImageView mImageView;
	private final CheckableImageView mButton;
	
	private PhotoGridViewCursorAdapter mParentAdapter;
	private String mCJayImageUuid;

	public CheckablePhotoItemLayout(Context context, AttributeSet attrs) {
		super(context, attrs);

		LayoutInflater.from(context).inflate(R.layout.grid_item_checkable_layout,
				this);

		mImageView = (SquareImageView) findViewById(R.id.picture);
		mButton = (CheckableImageView) findViewById(R.id.check_button);
		mButton.setOnClickListener(this);
	}

	public SquareImageView getImageView() {
		return mImageView;
	}

	public void setShowCheckbox(boolean visible) {
		if (visible) {
			mButton.setVisibility(View.VISIBLE);
			mButton.setOnClickListener(this);
		} else {
			mButton.setVisibility(View.GONE);
			mButton.setOnClickListener(null);
		}
	}

	public void onClick(View v) {
		if (!TextUtils.isEmpty(mCJayImageUuid)) {
			// Toggle check to show new state
			toggle();

			// Update the controller
			if (isChecked()) {
				mParentAdapter.addCheckedCJayImage(mCJayImageUuid);
			} else {
				mParentAdapter.removeCheckedCJayImage(mCJayImageUuid);
			}
		}
	}

	@Override
	public void setChecked(final boolean b) {
		super.setChecked(b);
		if (View.VISIBLE == mButton.getVisibility()) {
			mButton.setChecked(b);
		}
	}
	
	public void setCJayImageUuid(String cJayImageUuid) {
		mCJayImageUuid = cJayImageUuid;
	}

	public String getCJayImageUuid() {
		return mCJayImageUuid;
	}
	
	public void setParentAdapter(PhotoGridViewCursorAdapter parentAdapter) {
		mParentAdapter = parentAdapter;
	}
	
	public PhotoGridViewCursorAdapter getParentAdapter() {
		return mParentAdapter;
	}
}
