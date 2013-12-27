package com.cloudjay.cjay.listener;

public interface OnReportPageCompleteListener {
	public static final int TAB_DAMAGE_LOCATION = 0;
	public static final int TAB_DAMAGE_DAMAGE = 1;
	public static final int TAB_DAMAGE_REPAIR = 2;
	public static final int TAB_DAMAGE_DIMENSION = 3;
	public static final int TAB_DAMAGE_QUANTITY = 4;
	
	public void onReportPageCompleted(int page, String[] vals);
}
