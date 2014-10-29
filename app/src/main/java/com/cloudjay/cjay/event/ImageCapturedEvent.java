package com.cloudjay.cjay.event;

import com.cloudjay.cjay.model.AuditItem;

/**
 * User for update list of image capture and session in working fragment
 */
public class ImageCapturedEvent {
	private String containerId;
    private int imageType;
    private AuditItem auditItem;

	public ImageCapturedEvent(String containerId, int imageType, AuditItem auditItem) {
		this.containerId = containerId;
        this.imageType = imageType;
        this.auditItem = auditItem;
	}

	public String getContainerId() {
		return containerId;
	}

    public int getImageType() {
        return this.imageType;
    }

    public AuditItem getAuditItem() {
        return this.getAuditItem();
    }
}