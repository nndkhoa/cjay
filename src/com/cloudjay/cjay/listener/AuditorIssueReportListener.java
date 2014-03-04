package com.cloudjay.cjay.listener;



public interface AuditorIssueReportListener {
	
	public static final int TAB_ISSUE_PHOTO = 0;
	public static final int TAB_ISSUE_COMPONENT = 1;
	public static final int TAB_ISSUE_LOCATION = 2;
	public static final int TAB_ISSUE_DAMAGE = 3;
	public static final int TAB_ISSUE_REPAIR = 4;
	public static final int TAB_ISSUE_DIMENSION = 5;
	public static final int TAB_ISSUE_QUANTITY = 6;

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
