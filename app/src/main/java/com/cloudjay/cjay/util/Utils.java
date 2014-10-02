package com.cloudjay.cjay.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
	public static String getAppVersionName(Context ctx) {

		PackageInfo pInfo = null;
		try {
			pInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return pInfo.versionName;
	}

	//Check containerID ís Vaild or not
	public static boolean isContainerIdValid(String containerId) {

		if (!Logger.isDebuggable()) {

			int crc = ContCheckDigit.getCRC(containerId);
			if (crc == 10) {
				crc = 0;
			}

			char lastChar = containerId.charAt(containerId.length() - 1);
			if (Character.getNumericValue(lastChar) == crc) {
				return true;
			} else {
				return false;
			}
		}

		return true;
	}

	public static boolean simpleValid(String containeriD) {
		Pattern pattern = Pattern.compile("^([A-Z]+){4,4}+(\\d{7,7}+)$");
		Matcher matcher = pattern.matcher(containeriD);

		if (!matcher.matches()) return false;

		return true;
	}
}
