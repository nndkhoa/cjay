package com.cloudjay.cjay.util;

/**
 * Created by Tieu Bao on 9/30/13.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class PreferencesUtil {

	public static String PREFS = "prefs";
	public static String RESOURCE_DAMAGE_LAST_UPDATE = "resource_damage_last_update";
	public static String RESOURCE_REPAIR_LAST_UPDATE = "resource_repair_last_update";
	public static String RESOURCE_OPERATOR_LAST_UPDATE = "resource_operator_last_update";

	public static void storePrefsValue(Context context, String key,
			String content) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS, 0);
		Editor editor = prefs.edit();

		// Clear Prefs User Data
		editor.putString(key, content);

		// Commit Changed Data
		editor.commit();
	}

	public static String getPrefsValue(Context context, String key) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS, 0);
		String value = prefs.getString(key, "");
		return value;
	}
}
