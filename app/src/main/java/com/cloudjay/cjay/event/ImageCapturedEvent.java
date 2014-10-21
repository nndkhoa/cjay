package com.cloudjay.cjay.event;

/**
 * User for update list of image capture and session in working fragment
 */
public class ImageCapturedEvent {
	private String containerId;

	public ImageCapturedEvent(String containerId) {
		this.containerId = containerId;
	}

	public String getContainerId() {
		return containerId;
	}
}