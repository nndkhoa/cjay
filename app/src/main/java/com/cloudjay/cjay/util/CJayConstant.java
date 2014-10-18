package com.cloudjay.cjay.util;

import android.os.Environment;

import java.io.File;

public class CJayConstant {

	// File path
	public static final String APP_DIRECTORY = "CJay";
	public static final String BACK_UP_DIRECTORY = ".backup";
	public static final String LOG_DIRECTORY = "log";
	public static final String DAY_FORMAT = "yyyy-MM-dd";
	public static final String DAY_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

	public static final int TYPE_IMPORT = 0;
	public static final int TYPE_EXPORT = 1;
	public static final int TYPE_AUDIT = 2;
	public static final int TYPE_REPAIRED = 3;

	//For save and query in db
	public static final String OPERATOR_KEY = "OP";
	public static final String WORKING_DB = "WORKING";
	public static final String UPLOADING_DB = "UPLOADING";
	public static final String USER_KEY = "USER";
	public static final String DAMAGE_CODE_KEY = "DAMAGE";
	public static final String REPAIR_CODE_KEY = "REPAIR";
	public static final String COMPONENT_CODE_KEY = "COMPONENT";

	// `/sdcard/DCMI/CJay/`
	public static final File APP_DIRECTORY_FILE = new File(
			Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
			CJayConstant.APP_DIRECTORY);


}
