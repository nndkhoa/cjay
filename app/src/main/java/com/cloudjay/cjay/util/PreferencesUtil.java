package com.cloudjay.cjay.util;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesUtil {

	public static final String PREFS = "com.cloudjay.cjay.pref";
	public static final String PREF_CURRENT_USER = "com.cloudjay.cjay.pref_current_user";
	public static final String PREF_TOKEN = "com.cloudjay.cjay.pref_token";
	public static final String PREF_USER_NAME = "com.cloudjay.cjay.pref_name";
	public static final String PREF_USER_ROLE_NAME = "com.cloudjay.cjay.pref_role_name";
	public static final String PREF_USER_ROLE = "com.cloudjay.cjay.pref_role";
	public static final String PREF_USER_DEPOT = "com.cloudjay.cjay.pref_depot";
    public static final String PREF_MODIFIED_DATE = "com.cloudjay.cjay.pref_modified_date";
	public static final String PREF_MODIFIED_PAGE = "com.cloudjay.cjay.pref_modified_page";
	public static final String PREF_FIRST_PAGE_MODIFIED_DATE = "com.cloudjay.cjay.pref_first_page_modified_date";
    public static final String PREF_CAMERA_MODE_CONTINUOUS = "pref_camera_mode_continuous";
	public static final String PREF_UPLOAD_QUEUE_INDEX = "com.cloudjay.cjay.pref_upload_queue_index";


    public static final String PREF_SUBSCRIBE_PUBNUB = "com.cloudjay.cjay.pref_subscribe_pubnub";

	public static void clearPrefs(Context context) {
		SharedPreferences settings = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
		settings.edit().clear().commit();
	}

	public static void putObject(Context context, String key, Object object) {
		ComplexPreferences preferences = ComplexPreferences.getComplexPreferences(context, PreferencesUtil.PREFS, 0);
		preferences.putObject(key, object);
		preferences.commit();
	}

	public static <T> T getObject(Context context, String key, Class<T> a) {
		ComplexPreferences complexPreferences = ComplexPreferences.getComplexPreferences(context, PreferencesUtil.PREFS, 0);
		return complexPreferences.getObject(key, a);
	}

	public static int getPrefsValue(Context context, String key, int defVal) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS, 0);
		int value = prefs.getInt(key, defVal);
		return value;
	}

	public static String getPrefsValue(Context context, String key) {

		SharedPreferences prefs = context.getSharedPreferences(PREFS, 0);
		String value = prefs.getString(key, "");
		return value;

	}

	public static boolean getPrefsValue(Context context, String key, boolean defVal) {

		SharedPreferences prefs = context.getSharedPreferences(PREFS, 0);
		boolean value = prefs.getBoolean(key, defVal);
		return value;

	}

	public static void storePrefsValue(final Context context, String key, final boolean value) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

	public static void storePrefsValue(Context context, String key, String content) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS, 0);
		SharedPreferences.Editor editor = prefs.edit();

		// Clear Prefs User Data
		editor.putString(key, content);

		// Commit Changed Data
		editor.commit();
	}

	public static void storePrefsValue(Context context, String key, int val) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS, 0);
		SharedPreferences.Editor editor = prefs.edit();

		// Clear Prefs User Data
		editor.putInt(key, val);

		// Commit Changed Data
		editor.commit();
	}

	public static void removePrefsValue(Context context, String key) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS, 0);
		prefs.edit().remove(key).commit();
	}
}