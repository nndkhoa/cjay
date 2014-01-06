package com.cloudjay.cjay.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.text.format.DateUtils;

public class StringHelper {
	public static boolean isNullOrEmpty(String input) {
		if (input == null || input.trim() == "")
			return true;
		return false;
	}

	public static String addThumbExtensionUrl(String input) {
		try {
			int lastIndex = input.lastIndexOf(".");
			String extension = input.substring(lastIndex, input.length());
			String result = input.replace(extension, "_thumb" + extension);
			return result;
		} catch (Exception ex) {
			return input;
		}

	}

	@SuppressLint("SimpleDateFormat")
	public static String getTimestamp(String format, Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		String timeStamp = dateFormat.format(date);
		return timeStamp;
	}

	@SuppressLint("SimpleDateFormat")
	public static String getTimestamp(String oldFormat, String newFormat,
			String date) {

		if (TextUtils.isEmpty(date)) {
			return "";
		}

		Logger.Log(oldFormat);
		Logger.Log(newFormat);
		Logger.Log(date);

		try {
			Date tmp = new SimpleDateFormat(oldFormat).parse(date);
			SimpleDateFormat formatter = new SimpleDateFormat(newFormat);
			String timeStamp = formatter.format(date);
			return timeStamp;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return "";
	}

	@SuppressLint("SimpleDateFormat")
	public static String getCurrentTimestamp(String format) {
		String timeStamp = "";

		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		Date date = new Date();
		timeStamp = dateFormat.format(date);

		return timeStamp;
	}

	@SuppressLint("SimpleDateFormat")
	public static String getRelativeDate(String format, String date) {

		if (TextUtils.isEmpty(date)) {
			return "";
		}

		Date now = new Date();

		// 2013-11-10T21:05:24+08:00
		SimpleDateFormat dateFormat = new SimpleDateFormat(format);

		Date convertedDate = new Date();
		try {
			convertedDate = dateFormat.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
			return "";
		}

		String timeString = DateUtils.getRelativeTimeSpanString(
				convertedDate.getTime(), now.getTime(),
				DateUtils.SECOND_IN_MILLIS).toString();

		return timeString;

	}

}
