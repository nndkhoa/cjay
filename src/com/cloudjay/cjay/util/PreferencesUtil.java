package com.cloudjay.cjay.util;

/**
 * Created by Tieu Bao on 9/30/13.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class PreferencesUtil {

	public static final String PREFS = "prefs";
	public static final String PREF_RESOURCE_DAMAGE_LAST_UPDATE = "resource_damage_last_update";
	public static final String PREF_RESOURCE_REPAIR_LAST_UPDATE = "resource_repair_last_update";
	public static final String PREF_RESOURCE_OPERATOR_LAST_UPDATE = "resource_operator_last_update";
	public static final String PREF_RESOURCE_COMPONENT_LAST_UPDATE = "resource_component_last_update";
	public static final String PREF_CONTAINER_SESSION_LAST_UPDATE = "container_session_last_update";
	public static final String PREF_CAMERA_MODE_CONTINUOUS = "pref_camera_mode_continuous";
	public static final String PREF_NO_CONNECTION = "pref_no_connection";
	public static final String PREF_IS_FETCHING_DATA = "pref_is_fetching_data";
	public static final String PREF_IS_UPDATING_DATA = "pref_is_updating_data";
	public static final String PREF_USERNAME = "pref_username";
	public static final String PREF_APP_VERSION = "pref_app_version";

	public static void storePrefsValue(Context context, String key,
			String content) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS, 0);
		Editor editor = prefs.edit();

		// Clear Prefs User Data
		editor.putString(key, content);

		// Commit Changed Data
		editor.commit();
	}

	public static void storePrefsValue(final Context context, String key,
			final boolean value) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS, 0);
		Editor editor = prefs.edit();
		editor.putBoolean(key, value);
		editor.commit();
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
}
