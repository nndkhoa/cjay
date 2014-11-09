package com.cloudjay.cjay.event.image;

import com.cloudjay.cjay.model.AuditImage;

import java.util.List;

/**
 * Created by nambv on 21/10/2014.
 */
public class AuditImagesGotEvent {
    private List<AuditImage> auditImages;

    public AuditImagesGotEvent(List<AuditImage> auditImages) {
        this.auditImages = auditImages;
    }

    public List<AuditImage> getAuditImages() {
        return this.auditImages;
    }
}
