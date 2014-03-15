package com.cloudjay.cjay.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.Toast;

import com.cloudjay.cjay.CJayActivity;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.service.QueueIntentService_;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class Utils {

	private static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_CURRENT_USER_ID = "current_user_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";

	public static String replaceNullBySpace(String in) {
		return (in == null || in.equals("") ? " " : in);
	}

	public static String stripNull(String in) {
		return (in == null ? "" : in);
	}

	public static void isStillRunning(Context ctx, String packageName) {

		ActivityManager activityManager = (ActivityManager) ctx
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> procInfos = activityManager
				.getRunningAppProcesses();
		for (int i = 0; i < procInfos.size(); i++) {
			if (procInfos.get(i).processName.equals(packageName)) {
				Toast.makeText(ctx, packageName + "is running",
						Toast.LENGTH_LONG).show();
			}
		}
	}

	/**
	 * Gets the current registration ID for application on GCM service.
	 * <p>
	 * If result is empty, the app needs to register.
	 * 
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	public static String getRegistrationId(Context context) {
		final SharedPreferences prefs = getGCMPreferences(context);
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (registrationId.isEmpty()) {
			Logger.i("Registration not found.");
			return "";
		}

		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION,
				Integer.MIN_VALUE);

		int registeredCurrentUserId = prefs.getInt(PROPERTY_CURRENT_USER_ID,
				Integer.MIN_VALUE);

		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion
				|| registeredCurrentUserId != CJaySession.restore(context)
						.getCurrentUser().getID()) {
			Logger.i("App version changed.");
			return "";
		}
		return registrationId;
	}

	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private static SharedPreferences getGCMPreferences(Context context) {
		// how you store the regID in your app is up to you.
		return context.getSharedPreferences(CJayActivity.class.getSimpleName(),
				Context.MODE_PRIVATE);
	}

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	/**
	 * Stores the registration ID and app versionCode in the application's
	 * {@code SharedPreferences}.
	 * 
	 * @param context
	 *            application's context.
	 * @param regId
	 *            registration ID
	 */
	public static void storeRegistrationId(Context context, String regId) {
		final SharedPreferences prefs = getGCMPreferences(context);
		int appVersion = getAppVersion(context);

		Logger.i("Saving regId on app version " + appVersion);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID, regId);

		// Save Current User to Match the Current Recognized User to Get
		// Notifications.
		User user = CJaySession.restore(context).getCurrentUser();
		int CURRENT_USER_ID = user.getID();
		editor.putInt(PROPERTY_CURRENT_USER_ID, CURRENT_USER_ID);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		editor.commit();
	}

	public static boolean checkPlayServices(Context context) {
		Logger.Log("checkPlayServices()");

		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(context);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {

				GooglePlayServicesUtil.getErrorDialog(resultCode,
						(Activity) context,
						CJayConstant.PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Log.e("DEVICE_UNSUPPORTED", "This device is not supported.");
				((Activity) context).finish();
			}
			return false;
		}
		return true;
	}

	@SuppressLint("SimpleDateFormat")
	public static void updatePreferenceData(Context ctx, String candidateString) {

		String lastDateString = PreferencesUtil.getPrefsValue(ctx,
				PreferencesUtil.PREF_CONTAINER_SESSION_LAST_UPDATE);

		try {
			Date lastDate = new SimpleDateFormat(
					CJayConstant.CJAY_SERVER_DATETIME_FORMAT)
					.parse(lastDateString);

			Date candidate = new SimpleDateFormat(
					CJayConstant.CJAY_SERVER_DATETIME_FORMAT)
					.parse(candidateString);

			if (candidate.after(lastDate)) {
				PreferencesUtil.storePrefsValue(ctx,
						PreferencesUtil.PREF_CONTAINER_SESSION_LAST_UPDATE,
						candidateString);
			}

		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public static boolean hasNoConnection(final Context context) {
		return context.getSharedPreferences(PreferencesUtil.PREFS, 0)
				.getBoolean(PreferencesUtil.PREF_NO_CONNECTION, false) == true;
	}

	public static void startAlarm(Context context) {

		Logger.Log("start Alarm Manager");

		// Making Alarm for Queue Worker
		Intent intent = new Intent(context, QueueIntentService_.class);
		PendingIntent pintent = PendingIntent.getService(context, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		Calendar current = Calendar.getInstance();
		AlarmManager alarm = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);

		// Start every 10 seconds
		alarm.setRepeating(AlarmManager.RTC_WAKEUP, current.getTimeInMillis(),
				10 * 1000, pintent);
	}

	public static void cancelAlarm(Context context) {

		Logger.Log("stop Alarm Manager");

		Intent intent = new Intent(context, QueueIntentService_.class);
		PendingIntent sender = PendingIntent
				.getBroadcast(context, 0, intent, 0);

		AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);

		alarmManager.cancel(sender);
	}

	public static boolean isAlarmUp(Context context) {

		return PendingIntent.getService(context, 0, new Intent(context,
				QueueIntentService_.class), PendingIntent.FLAG_NO_CREATE) != null;

	}

	public static File getAppDirectoryFile() {
		return new File(Environment.getExternalStorageDirectory(),
				CJayConstant.APP_DIRECTORY);
	}

	public static File getHiddentAppDirectoryFile() {
		return new File(Environment.getExternalStorageDirectory(),
				CJayConstant.HIDDEN_APP_DIRECTORY);
	}

}
