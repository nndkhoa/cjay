package com.cloudjay.cjay.listener;

public interface AuditorIssueReportListener {
	public static final int TAB_ISSUE_LOCATION = 0;
	public static final int TAB_ISSUE_DAMAGE = 1;
	public static final int TAB_ISSUE_REPAIR = 2;
	public static final int TAB_ISSUE_COMPONENT = 3;
	public static final int TAB_ISSUE_DIMENSION = 4;
	public static final int TAB_ISSUE_QUANTITY = 5;
	
	public static final int TYPE_LOCATION_CODE = 0;
	public static final int TYPE_LENGTH = 1;
	public static final int TYPE_HEIGHT = 2;
	public static final int TYPE_QUANTITY = 3;
	public static final int TYPE_DAMAGE_CODE = 4;
	public static final int TYPE_REPAIR_CODE = 5;
	public static final int TYPE_COMPONENT_CODE = 6;
	
	public void onReportPageCompleted(int page);
	public void onReportValueChanged(int type, String val);
}
