package com.cloudjay.cjay.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by nambv on 17/10/2014.
 */
public class WorkingSession extends Session {

    public WorkingSession() {

    }

    @SerializedName("audit_images")
    @Expose
    private List<AuditImage> auditImages;

    public List<AuditImage> getAuditImages() {
        return auditImages;
    }

    public void setAuditImages(List<AuditImage> auditImages) {
        this.auditImages = auditImages;
    }

    public WorkingSession withAuditImages(List<AuditImage> auditImages) {
        this.auditImages = auditImages;
        return this;
    }
}
