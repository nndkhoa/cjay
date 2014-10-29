package com.cloudjay.cjay.util;

import android.os.Environment;

import java.io.File;

public class CJayConstant {

	// Database
	public static final String DB_NAME = "cjay";

	// File path
	public static final String APP_DIRECTORY = "CJay";
	public static final String BACK_UP_DIRECTORY = ".backup";
	public static final String LOG_DIRECTORY = "log";
	public static final String DAY_FORMAT = "yyyy-MM-dd";
	public static final String DAY_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

	// Prefix for save and query in db
	public static final String PREFIX_OPERATOR = "OP";
	public static final String PREFIX_WORKING = "WORKING";
	public static final String PREFIX_UPLOADING = "UPLOADING";
	public static final String PREFIX_USER = "USER";
	public static final String PREFIX_DAMAGE_CODE = "DAMAGE";
	public static final String PREFIX_REPAIR_CODE = "REPAIR";
	public static final String PREFIX_COMPONENT_CODE = "COMPONENT";
    public static final String PREFIX_LOG = "LOG";

	// `/sdcard/DCMI/CJay/`
	public static final File APP_DIRECTORY_FILE = new File(
			Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
			CJayConstant.APP_DIRECTORY);

	public static final File BACK_UP_DIRECTORY_FILE = new File(Environment.getExternalStorageDirectory(),
			BACK_UP_DIRECTORY);

	public static final File LOG_DIRECTORY_FILE = new File(Environment.getExternalStorageDirectory(), LOG_DIRECTORY);
	public static final String CJAY_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZZ";
	public static final String CJAY_DATETIME_FORMAT_NO_TIMEZONE = "yyyy-MM-dd'T'HH:mm:ss";
	public static final String LOG_TO_FILE_FORMAT = "[%s]	-	%s\n";


    public static final int ALARM_ID = 49482;
	public static final int ALARM_INTERVAL = 86400;

}
