package com.cloudjay.cjay.events;

import com.cloudjay.cjay.model.CJayImage;

public class NewCJayImageEvent {

	private final CJayImage mCJayImage;

	public NewCJayImageEvent(CJayImage cJayImage) {
		mCJayImage = cJayImage;
	}

	public CJayImage getCJayImage() {
		return mCJayImage;
	}

}