package com.cloudjay.cjay.events;

import com.cloudjay.cjay.model.CJayImage;

public class CJayImageAddedEvent {

	private final CJayImage mCJayImage;
	private final String mTag;
	private final String mUuid;

	public CJayImageAddedEvent(CJayImage cJayImage, String tag) {
		mCJayImage = cJayImage;
		mTag = tag;
		mUuid = "";
	}

	public CJayImageAddedEvent(String uuid, String tag) {
		mUuid = uuid;
		mTag = tag;
		mCJayImage = null;
	}

	public CJayImage getCJayImage() {
		return mCJayImage;
	}

	public String getTag() {
		return mTag;
	}

	public String getUuid() {
		return mUuid;
	}
}