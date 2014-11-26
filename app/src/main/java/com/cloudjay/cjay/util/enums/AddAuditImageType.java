package com.cloudjay.cjay.util.enums;

public enum AddAuditImageType {

    ADD_AUDIT_IMAGE_TO_NEW_ISSUE(0),
    ADD_AUDIT_IMAGE_TO_EXISTED_ISSUE(1);

    public final int value;

    AddAuditImageType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}
