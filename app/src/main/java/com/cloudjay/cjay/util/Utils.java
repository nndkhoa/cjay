package com.cloudjay.cjay.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Created by Thai on 9/13/2014.
 */
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
}
