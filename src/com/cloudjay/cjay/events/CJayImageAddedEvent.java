package com.cloudjay.cjay.events;

import com.cloudjay.cjay.model.CJayImage;

/**
 * 
 * Trigger when user captured an image (tell people that an image has been
 * created)
 * 
 * @author quocvule
 * 
 */
public class CJayImageAddedEvent {

	private final CJayImage mCJayImage;
	private final String mTag;

	public CJayImageAddedEvent(CJayImage cJayImage, String tag) {
		mCJayImage = cJayImage;
		mTag = tag;
	}

	public CJayImage getCJayImage() {
		return mCJayImage;
	}

	public String getTag() {
		return mTag;
	}

}