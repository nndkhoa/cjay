package com.cloudjay.cjay.util;

/**
 * @author tieubao
 */

import android.util.Log;

public final class Logger {

	/**
	 * Adjust this field for properly logs
	 * 
	 * PRODUCTION => PRODUCTION_MODE = true
	 * 
	 * DEVELOPMENT => PRODUCTION_MODE = false
	 * 
	 */

	public static boolean PRODUCTION_MODE = true;
	public static final String CJAY_TAG = "CJAY";

	public static void Log(String tag, String content) {
		if (null != content) {
			if (PRODUCTION_MODE == false) {
				Log.w(tag, content);
			}
		} else {
			Log.e(tag, "Null string");
		}
	}

	public static void Log(String content) {
		String tag = "CJAY_INFO";
		if (null != content) {
			if (PRODUCTION_MODE == false) {
				Log.w(tag, content);
			}
		} else {
			Log.e(tag, "Null string");
		}
	}

	public static void Log(String tag, String content, int mode) {
		if (null != content) {
			if (PRODUCTION_MODE == false) {
				switch (mode) {
				case Log.DEBUG:
					Log.d(tag, content);
					break;

				case Log.ERROR:
					Log.e(tag, content);
					break;

				case Log.WARN:
					Log.w(tag, content);
					break;

				default:
					Log.i(tag, content);
					break;
				}
			}
		} else {
			Log.e(tag, "Null string");
		}
	}
}