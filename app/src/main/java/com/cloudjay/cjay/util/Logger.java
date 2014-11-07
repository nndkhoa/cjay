package com.cloudjay.cjay.util;

import android.text.TextUtils;
import android.util.Log;

import com.cloudjay.cjay.BuildConfig;
import com.google.gson.Gson;

/**
 *
 */
public final class Logger {

	/**
	 * Adjust this field for properly logs
	 * <p/>
	 * PRODUCTION => PRODUCTION_MODE = true
	 * <p/>
	 * DEVELOPMENT => PRODUCTION_MODE = false
	 */
	private static boolean isDebuggable = BuildConfig.DEBUG;

	private volatile static Logger instance;
	static String className;
	static String methodName;

	private static String createLog(String log) {

		StringBuffer buffer = new StringBuffer();
		buffer.append("[");
		buffer.append(methodName);
		buffer.append("] ");

		if (TextUtils.isEmpty(log)) {
			buffer.append("message is empty");
		} else {
			buffer.append(log);
		}

		return buffer.toString();
	}

	public static void d(String message) {
		if (!isDebuggable()) return;

		getMethodNames(new Throwable().getStackTrace());
		Log.d(className, createLog(message));
	}

	public static void e(String message) {
		if (!isDebuggable()) return;

		getMethodNames(new Throwable().getStackTrace());
		Log.e(className, createLog(message));
	}

	public static Logger getInstance() {

		if (instance == null) {
			synchronized (Logger.class) {
				if (instance == null) {
					instance = new Logger();
				}
			}
		}

		return instance;
	}

	private static void getMethodNames(StackTraceElement[] sElements) {
		className = sElements[1].getFileName();
		methodName = sElements[1].getMethodName();
	}

	public static void i(String message) {
		if (!isDebuggable()) return;

		getMethodNames(new Throwable().getStackTrace());
		Log.i(className, createLog(message));
	}

	public static boolean isDebuggable() {
		return isDebuggable;
	}

	public static void Log(String message) {
		if (!isDebuggable()) return;

		getMethodNames(new Throwable().getStackTrace());
		Log.i(className, createLog(message));
	}

	public static void Log(String tag, String message) {
		if (!isDebuggable()) return;

		if (null != message) {
			Log.w(tag, message);
		} else {
			Log.e(tag, "Null string");
		}
	}

	public static void Log(String tag, String message, int mode) {

		if (null != message) {
			if (isDebuggable == false) {
				switch (mode) {
					case Log.DEBUG:
						Log.d(tag, message);
						break;

					case Log.ERROR:
						Log.e(tag, message);
						break;

					case Log.WARN:
						Log.w(tag, message);
						break;

					default:
						Log.i(tag, message);
						break;
				}
			}
		} else {
			Log.e(tag, "Null string");
		}
	}

	public static void v(String message) {
		if (!isDebuggable()) return;

		getMethodNames(new Throwable().getStackTrace());
		Log.v(className, createLog(message));
	}

	public static void w(String message) {
		if (!isDebuggable()) return;

		getMethodNames(new Throwable().getStackTrace());
		Log.w(className, createLog(message));
	}

	public static void wtf(String message) {
		if (!isDebuggable()) return;

		getMethodNames(new Throwable().getStackTrace());
		Log.wtf(className, createLog(message));
	}

	public void setDebuggable(boolean enable) {
		isDebuggable = enable;
	}

	public static void logJson(Object object) {
		Gson gson = new Gson();
		String s = gson.toJson(object);
		d("Json: " + s);
	}
}
