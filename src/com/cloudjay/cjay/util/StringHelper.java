package com.cloudjay.cjay.util;

import android.annotation.SuppressLint;
import android.text.format.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
	public static String getCurrentTimestamp(String format) {
		String timeStamp = "";

		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		Date date = new Date();
		timeStamp = dateFormat.format(date);

		return timeStamp;
	}

	@SuppressLint("SimpleDateFormat")
	public static String getRelativeDate(String date) {

		if (date == "") {
			return "";
		}

		Date now = new Date();

		// 2013-11-10T21:05:24+08:00
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ssZZ");

		Date convertedDate = new Date();
		try {
			convertedDate = dateFormat.parse(date);
		} catch (ParseException e) {
			// e.printStackTrace();
			return "";
		}

		String timeString = DateUtils.getRelativeTimeSpanString(
				convertedDate.getTime(), now.getTime(),
				DateUtils.SECOND_IN_MILLIS).toString();

		return timeString;

	}
	//
	// public static String getLocation(Context context) {
	// String cityName = "";
	// LocationInfo latestInfo = new LocationInfo(context);
	//
	// Logger.Log("Long: " + Float.toString(latestInfo.lastLat) + "/ Lat: "
	// + Float.toString(latestInfo.lastLong));
	//
	// Geocoder gcd = new Geocoder(context, Locale.getDefault());
	// List<Address> addresses;
	// try {
	// addresses = gcd.getFromLocation(latestInfo.lastLat,
	// latestInfo.lastLong, 1);
	// if (addresses.size() > 0) {
	//
	// Logger.Log(addresses.get(0).getFeatureName() + ", "
	// + addresses.get(0).getLocality() + ", "
	// + addresses.get(0).getAdminArea() + ", "
	// + addresses.get(0).getCountryName());
	//
	// cityName = addresses.get(0).getAdminArea() + ", "
	// + addresses.get(0).getCountryName();
	// } else {
	// Logger.Log("Cannot detect current location.");
	// }
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// return cityName;
	// }
}
