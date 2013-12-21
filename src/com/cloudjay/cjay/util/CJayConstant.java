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
	public static final String API_GOOGLE_CLOUD_STORAGE_TOKEN = API_ROOT
			+ "auth/get-google-cloud-storage-token/";

	public static final String CURRENT_USER = API_ROOT + "cjay/current-user";
	public static final String ALL_USERS = API_ROOT + "auth/jayusers";
	
	/**
	 * File path
	 */
	public static final String APP_DIRECTORY = "DCIM/CJay";
	public static final String HIDDEN_APP_DIRECTORY = "Pictures/.CJay";

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
	public static final int SPLASH_TIME_OUT = 3000;
	
	/**
	 * CJay Code
	 */
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int CAPTURE_REQUEST = 100;
	public static final int SELECT_PHOTO = 101;
}