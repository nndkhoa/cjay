package com.cloudjay.cjay.events;

import com.cloudjay.cjay.model.CJayImage;

public class CJayImageAddedEvent {

	private final CJayImage mCJayImage;

	public CJayImageAddedEvent(CJayImage cJayImage) {
		this.mCJayImage = cJayImage;
	}

	public CJayImage getCJayImage() {
		return this.mCJayImage;
	}

}