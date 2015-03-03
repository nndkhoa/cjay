package com.cloudjay.cjay.util.enums;

public enum ObjectType {

    SESSION(1),
    AUDIT_ITEM(2),
    AUDIT_IMAGE(3),
    GATE_IMAGE(4);

    public final int value;

    ObjectType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
