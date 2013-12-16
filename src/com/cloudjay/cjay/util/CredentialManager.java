package com.cloudjay.cjay.util;

/**
 * Created by Tieu Bao on 9/30/13.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class CredentialManager {

	public static String PREFS = "prefs";
	public static String USER = "user";
	public static String USERNAME = "username";
	public static String USER_ID = "user_id";
	public static String USER_TOKEN = "user_token";

	public static String AVATAR_URL = "avatar_url";
	public static String FULL_NAME = "full_name";
	public static String TEAM_NAME = "team_name";
	public static String TEAM_NICK = "team_nick";
	public static String TEAM_ARRAY = "team_array";

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

	public static boolean hasLoggedInBefore(Context context) {
		return getPrefsValue(context, USERNAME).equals("") == false;
	}

	public static boolean clearCredential(Context context) {
		try {
			SharedPreferences prefs = context.getSharedPreferences(PREFS, 0);
			Editor editor = prefs.edit();

			// Clear Prefs User Data
			editor.putString(USER, "");
			editor.putString(USER_ID, "");
			editor.putString(USER_TOKEN, "");
			editor.putString(AVATAR_URL, "");
			editor.putString(FULL_NAME, "");
			editor.putString(TEAM_NAME, "");
			editor.putString(TEAM_NICK, "");
			editor.putString(TEAM_ARRAY, "");

			// Commit Changed Data
			editor.commit();

			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	public static boolean isAuthenticated(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS, 0);
		String userId = prefs.getString(USER_ID, "");
		return !userId.equals("");
	}

}
