package com.cloudjay.cjay.util.enums;

public enum Step {

	AUDIT(0),
	REPAIR(1),
	AVAILABLE(2),
	EXPORTED(3),
	IMPORT(4),
	CLEAR(5);

	public final int value;

	Step(int value) {
		this.value = value;
	}

	@Override
	public String toString() {
		if (this.value == AUDIT.value) {
			return "Giám định";
		} else if (value == REPAIR.value) {
			return "Sửa chữa";
		} else if (value == AVAILABLE.value) {
			return "Xuất";
		} else if (value == EXPORTED.value) {
			return "Đã xuất";
		} else if (value == IMPORT.value) {
			return "Nhập";
		} else if (value == CLEAR.value) {
			return "Vệ sinh - Quét";
		}

		return super.toString();
	}
}
