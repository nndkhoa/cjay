package com.cloudjay.cjay.events;

import com.cloudjay.cjay.model.CJayImage;

public class CJayImageAddedEvent {

	private final CJayImage mCJayImage;
	private final String mTag;

	public CJayImageAddedEvent(CJayImage cJayImage, String tag) {
		this.mCJayImage = cJayImage;
		this.mTag = tag;
	}

	public CJayImage getCJayImage() {
		return this.mCJayImage;
	}
	
	public String getTag() {
		return this.mTag;
	}

}