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
	public static final String HOMEPAGE = "http://www.jaypixapp.com";

	public static final String JAYPIX_API = "https://cloudjay-web.appspot.com";
	public static final String TOKEN = JAYPIX_API + "/api-token-auth/";
	public static final String API_ROOT = JAYPIX_API + "/api/";

	public static final int PICTURE_SIZE_MAX_WIDTH = 1280;
	public static final int PREVIEW_SIZE_MAX_WIDTH = 640;

	public static final String API_ADD_GCM_DEVICE = API_ROOT
			+ "mobile/gcm-devices.json";
	public static final String API_GOOGLE_CLOUD_STORAGE_TOKEN = API_ROOT
			+ "auth/get-google-cloud-storage-token/";

	public static final String CURRENT_USER = API_ROOT + "jaypix/current-user";
	public static final String LIST_USER = API_ROOT
			+ "jaypix/jaypix-users-by-team";
	public static final String LIST_ITEM = API_ROOT
			+ "jaypix/jaypix-items-by-team";

	public static final String JAYPIX_ITEMS = API_ROOT
			+ "jaypix/jaypix-items.json";
	public static final String ALL_USERS = API_ROOT + "auth/jayusers";

	/**
	 * File path
	 */
	public static final String APP_DIRECTORY = "DCIM/JayPixApp";
	public static final String HIDDEN_APP_DIRECTORY = "Pictures/.JayPixApp";

	/**
	 * /sdcard/DCMI/JayPixApp/
	 */
	public static final File APP_DIRECTORY_FILE = new File(
			Environment.getExternalStorageDirectory(),
			CJayConstant.APP_DIRECTORY);

	/**
	 * /sdcard/Pictures/.JayPixApp/
	 */
	public static final File HIDDEN_APP_DIRECTORY_FILE = new File(
			Environment.getExternalStorageDirectory(),
			CJayConstant.HIDDEN_APP_DIRECTORY);

	/**
	 * Threshold
	 */
	public static final float ACTION_MOVE_DELTA_THRESHOLD = 3.0f;
	public static final long LONG_CLICK_THRESHOLD = 1000;

	/**
	 * Color
	 */
	public static final String COLOR_RED = "#ffe51a1d";
	public static final String COLOR_BLUE = "#ff377db8";
	public static final String COLOR_GREEN = "#ff4eae4a";
	public static final String COLOR_PURPLE = "#ff984ea3";
	public static final String COLOR_ORANGE = "#ffff7f00";
	public static final String COLOR_YELLOW = "#ffffff33";
	public static final String COLOR_BROWN = "#ffa5562f";
	public static final String COLOR_PINK = "#fff781be";
	
	/**
	 * Splash Screen
	 */
	public static final int SPLASH_TIME_OUT = 3000;
}