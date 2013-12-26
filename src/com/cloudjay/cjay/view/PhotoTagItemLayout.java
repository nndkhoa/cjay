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

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;

import com.cloudjay.cjay.PhotoUploadController;
import com.cloudjay.cjay.model.TmpContainerSession;

@SuppressLint("ViewConstructor")
@SuppressWarnings("deprecation")
public class PhotoTagItemLayout extends FrameLayout {

	static final String LOG_TAG = "PhotoTagItemLayout";

	private final MultiTouchImageView mImageView;

	private final AbsoluteLayout mTagLayout;

	private int mPosition;
	private final TmpContainerSession mUpload;

	public PhotoTagItemLayout(Context context, PhotoUploadController controller, TmpContainerSession upload) {
		super(context);
		
		mImageView = new MultiTouchImageView(context);
		addView(mImageView, FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.MATCH_PARENT);

		mTagLayout = new AbsoluteLayout(context);
		addView(mTagLayout, FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.MATCH_PARENT);

//		if (null != upload) {
//			upload.setTagChangedListener(this);
//			mButton.setChecked(mController.isSelected(upload));
//		}
		mUpload = upload;
	}

	public MultiTouchImageView getImageView() {
		return mImageView;
	}

	public TmpContainerSession getPhotoSelection() {
		return mUpload;
	}

	public int getPosition() {
		return mPosition;
	}

	public void setPosition(int position) {
		mPosition = position;
	}
}
