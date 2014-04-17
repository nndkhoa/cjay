package com.cloudjay.cjay.events;

import com.cloudjay.cjay.model.CJayImage;

/**
 * 
 * Trigger when user change `Upload State` of CJayImage.
 * 
 * @author tieubao
 * 
 */

public class CJayImageUploadStateChangedEvent {

	private final CJayImage cjayImage;

	public CJayImageUploadStateChangedEvent(CJayImage upload) {
		cjayImage = upload;
	}

	public CJayImage getTarget() {
		return cjayImage;
	}

}
