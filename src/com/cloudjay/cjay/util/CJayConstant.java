package com.cloudjay.cjay.util;

import java.io.File;

import android.os.Environment;

public class CJayConstant {

	// API
	public static final String CLOUDJAY_API = "https://cloudjay-web.appspot.com";
	public static final String ACRA = "https://cloudjay-web.appspot.com/acra/";
	public static final String BETA_CLOUDJAY_API = "https://beta-dot-cloudjay-web.appspot.com";
	public static final String TOKEN = CLOUDJAY_API + "/api-token-auth/";

	public static String API_ROOT = "";
	public static String API_ADD_GCM_DEVICE = "";
	public static String CURRENT_USER = "";
	public static String LIST_OPERATORS = "";
	public static String LIST_DAMAGE_CODES = "";
	public static String LIST_REPAIR_CODES = "";
	public static String LIST_COMPONENT_CODES = "";
	public static String CONTAINER_SESSIONS = "";
	public static String CONTAINER_SESSION_ITEM = "";

	public static final String CJAY_TMP_STORAGE = "https://www.googleapis.com/upload/storage/v1beta2/b/cjaytmp/o?uploadType=media&name=%s";

	// public static final String CJAY_TMP_STORAGE = "https://www.googleapis.com/upload/storage/v1beta2/b/cjaytmp/o";

	public static void initApi(boolean isBeta) {

		if (isBeta) {
			API_ROOT = BETA_CLOUDJAY_API + "/api/";
		} else {
			API_ROOT = CLOUDJAY_API + "/api/";
		}

		API_ADD_GCM_DEVICE = API_ROOT + "mobile/gcm-devices.json";
		CURRENT_USER = API_ROOT + "cjay/current-user.json";
		LIST_OPERATORS = API_ROOT + "cjay/container-operators.json";
		LIST_DAMAGE_CODES = API_ROOT + "cjay/damage-codes.json";
		LIST_REPAIR_CODES = API_ROOT + "cjay/repair-codes.json";
		LIST_COMPONENT_CODES = API_ROOT + "cjay/component-codes.json";
		CONTAINER_SESSIONS = API_ROOT + "cjay/container-sessions.json";
		CONTAINER_SESSION_ITEM = API_ROOT + "cjay/container-sessions/%s.json";

	}

	// File path
	public static final String APP_DIRECTORY = "CJay";
	public static final String HIDDEN_APP_DIRECTORY = ".CJay";
	public static final String BACK_UP_DIRECTORY = ".backup";

	// `/sdcard/DCMI/CJay/`
	public static final File APP_DIRECTORY_FILE = new File(
															Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
															CJayConstant.APP_DIRECTORY);

	// `/sdcard/Pictures/.CJay/`
	public static final File HIDDEN_APP_DIRECTORY_FILE = new File(
																	Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
																	CJayConstant.HIDDEN_APP_DIRECTORY);

	public static final File BACK_UP_DIRECTORY_FILE = new File(Environment.getExternalStorageDirectory(),
																BACK_UP_DIRECTORY);

	public static final int SPLASH_TIME_OUT = 2000;
	public static final String CJAY_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZZ";
	public static final String CJAY_DATETIME_FORMAT_NO_TIMEZONE = "yyyy-MM-dd'T'HH:mm:ss";

	// GCM configuration
	public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	public static final String SENDER_ID = "189386999383";

	// Loader ID
	public static final int CURSOR_LOADER_ID_GATE_EXPORT = 0;
	public static final int CURSOR_LOADER_ID_GATE_IMPORT = 1;
	public static final int CURSOR_LOADER_ID_AUDITOR_REPORTING = 2;
	public static final int CURSOR_LOADER_ID_AUDITOR_NOT_REPORTED = 3;
	public static final int CURSOR_LOADER_ID_REPAIR_PENDING = 4;
	public static final int CURSOR_LOADER_ID_REPAIR_FIXED = 5;
	public static final int CURSOR_LOADER_ID_PHOTO_GD_1 = 6;
	public static final int CURSOR_LOADER_ID_PHOTO_GD_2 = 7;
	public static final int CURSOR_LOADER_ID_USER_LOG = 8;
	public static final int CURSOR_LOADER_ID_DAMAGE_CODE = 9;
	public static final int CURSOR_LOADER_ID_COMPONENT_CODE = 10;
	public static final int CURSOR_LOADER_ID_REPAIR_CODE = 11;
	public static final int CURSOR_LOADER_ID_ISSUE_ITEM = 12;
	public static final int CURSOR_LOADER_ID_UPLOAD = 13;

	// ALARM ID
	public static final int ALARM_ID = 49482;
	public static final int ALARM_INTERVAL = 10;
	public static final String CUSTOM_INTENT = "com.cloudjay.cjay.CUSTOM_INTENT";

	public static final int HIDDEN_LOG_THRESHOLD = 4;

	public static final int RETRY_THRESHOLD = 4;

	public static final String INTENT_SERVICE_UPLOAD_ALL = "cjay.intent.action.UPLOAD_ALL";
	public static final String INTENT_PHOTO_TAKEN = "cjay.intent.action.PHOTO_TAKEN";
	public static final String INTENT_LOGOUT = "cjay.intent.action.LOGOUT";
	public static final String INTENT_OPEN_TAB_UPLOAD = "cjay.intent.action.OPEN_TAB_UPLOAD";
	public static final float IMAGE_CACHE_HEAP_PERCENTAGE = 1f / 6f;

}