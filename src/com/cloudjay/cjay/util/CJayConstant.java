package com.cloudjay.cjay.util;

import java.io.File;

import android.os.Environment;

/**
 * 
 * Return JayPix meta string
 * 
 * @author Tieu Bao
 * 
 */
public final class CJayConstant {

	/**
	 * API
	 */
	public static final String HOMEPAGE = "http://www.cjaynet.com";

	public static final String CLOUDJAY_API = "https://cloudjay-web.appspot.com";
	public static final String TOKEN = CLOUDJAY_API + "/api-token-auth/";
	public static final String API_ROOT = CLOUDJAY_API + "/api/";

	public static final int PICTURE_SIZE_MAX_WIDTH = 1280;
	public static final int PREVIEW_SIZE_MAX_WIDTH = 640;

	public static final String API_ADD_GCM_DEVICE = API_ROOT
			+ "mobile/gcm-devices.json";
	// public static final String API_GOOGLE_CLOUD_STORAGE_TOKEN = API_ROOT
	// + "auth/get-google-cloud-storage-token/";

	public static final String CURRENT_USER = API_ROOT + "cjay/current-user";
	public static final String ALL_USERS = API_ROOT + "auth/jayusers";
	public static final String LIST_OPERATORS = API_ROOT
			+ "cjay/container-operators";
	public static final String LIST_DAMAGE_CODES = API_ROOT
			+ "cjay/damage-codes";
	public static final String LIST_REPAIR_CODES = API_ROOT
			+ "cjay/repair-codes";
	public static final String LIST_COMPONENT_CODES = API_ROOT
			+ "cjay/component-codes";
	public static final String CJAY_RESOURCE_STATUS = API_ROOT
			+ "cjay/cjay-resource-status";
	public static final String LIST_CONTAINER_SESSIONS = API_ROOT
			+ "cjay/container-sessions";

	public static final String CJAY_ITEMS = API_ROOT
			+ "cjay/container-sessions.json";

	public static final String LIST_CONTAINER_SESSIONS_WITH_DATETIME = API_ROOT
			+ "cjay/container-sessions.json?created_after=%s";

	public static final String LIST_CONTAINER_SESSIONS_REPORT_LIST = API_ROOT
			+ "cjay/container-sessions-report-list.json";

	public static final String CJAY_TMP_STORAGE = "https://www.googleapis.com/upload/storage/v1beta2/b/cjaytmp/o?uploadType=media&name=%s";

	/**
	 * File path
	 */
	public static final String APP_DIRECTORY = "DCIM/CJay";
	public static final String HIDDEN_APP_DIRECTORY = "Pictures/.CJay"; // for
																		// temporary

	// `/sdcard/DCMI/CJay/`
	public static final File APP_DIRECTORY_FILE = new File(
			Environment.getExternalStorageDirectory(),
			CJayConstant.APP_DIRECTORY);

	// `/sdcard/Pictures/.CJay/`
	public static final File HIDDEN_APP_DIRECTORY_FILE = new File(
			Environment.getExternalStorageDirectory(),
			CJayConstant.HIDDEN_APP_DIRECTORY);

	/**
	 * Threshold
	 */
	public static final float ACTION_MOVE_DELTA_THRESHOLD = 3.0f;
	public static final long LONG_CLICK_THRESHOLD = 1000;

	/**
	 * Splash Screen
	 */
	public static final int SPLASH_TIME_OUT = 2000;

	/**
	 * CJay Code
	 */
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int CAPTURE_REQUEST = 100;
	public static final int SELECT_PHOTO = 101;
	public static final float IMAGE_CACHE_HEAP_PERCENTAGE = 1f / 6f;

	public static final String CJAY_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZZ";
	public static final String CJAY_SERVER_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
	public static final String CJAY_UPLOAD_DATETIME_FORMAT = "yyyy-MM-dd";

	public static final long SCALE_ANIMATION_DURATION_FULL_DISTANCE = 800;
	public static final String PREF_UPLOADS_PAUSED = "pref_uploads_paused";
	public static final String PREF_INSTANT_UPLOAD_ENABLED = "pref_instant_upload_enabled";
	public static final String INTENT_SERVICE_UPLOAD_ALL = "cjay.intent.action.UPLOAD_ALL";
	public static final String PREF_INSTANT_UPLOAD_IF_ROAMING = "pref_instant_upload_roaming_enabled";
	public static final String PREF_INSTANT_UPLOAD_WIFI_ONLY = "pref_instant_upload_wifi_only";
}