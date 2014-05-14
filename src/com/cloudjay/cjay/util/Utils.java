package com.cloudjay.cjay.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.cloudjay.cjay.AuditorHomeActivity_;
import com.cloudjay.cjay.CJayActivity;
import com.cloudjay.cjay.GateHomeActivity_;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.RepairHomeActivity_;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.service.PhotoUploadService_;
import com.cloudjay.cjay.service.QueueIntentService_;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class Utils {

	private static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_CURRENT_USER_ID = "current_user_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";

	public static boolean enableAutoCheckForUpdate(Context context) {

		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		return pref.getBoolean(context.getString(R.string.pref_key_auto_check_update_checkbox), false);

	}

	public static void backupDatabase(String username) {

		Logger.Log("Backing up database ...");

		FileChannel src = null;
		FileChannel dst = null;
		try {
			File sd = CJayConstant.BACK_UP_DIRECTORY_FILE;
			File data = Environment.getDataDirectory();

			if (sd.canWrite()) {

				String currentDBPath = "//data//com.cloudjay.cjay//databases//cjay.db";

				String backupDBPath = "cjay-"
						+ StringHelper.getCurrentTimestamp(CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE) + "-"
						+ username + ".db";

				File currentDB = new File(data, currentDBPath);
				File backupDB = new File(sd, backupDBPath);

				if (currentDB.exists()) {
					src = new FileInputStream(currentDB).getChannel();
					dst = new FileOutputStream(backupDB).getChannel();
					dst.transferFrom(src, 0, src.size());
				} else {
					Logger.e("Current database do not exist");
				}

			}
		} catch (Exception e) {

			e.printStackTrace();

		} finally {
			try {
				src.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				dst.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void cancelAlarm(Context context) {

		Logger.Log("stop Alarm Manager");

		Intent intent = new Intent(context, QueueIntentService_.class);
		PendingIntent sender = PendingIntent.getService(context, CJayConstant.ALARM_ID, intent,
														PendingIntent.FLAG_UPDATE_CURRENT);

		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		alarmManager.cancel(sender);
		sender.cancel();

		// --------
		// Intent stopServiceIntent = new Intent(context,
		// QueueIntentService_.class);
		// context.stopService(stopServiceIntent);
	}

	public static void cancelThenStartAlarm(Context context) {

		Intent intent = new Intent(context, QueueIntentService_.class);
		PendingIntent sender = PendingIntent.getService(context, CJayConstant.ALARM_ID, intent,
														PendingIntent.FLAG_UPDATE_CURRENT);

		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		alarmManager.cancel(sender);

		Calendar cal = Calendar.getInstance();

		// Start every 10 seconds
		// InexactRepeating allows Android to optimize the energy consumption
		alarmManager.setInexactRepeating(	AlarmManager.ELAPSED_REALTIME_WAKEUP, cal.getTimeInMillis(),
											CJayConstant.ALARM_INTERVAL * 1000, sender);
	}

	public static boolean checkPlayServices(Context context) {
		Logger.Log("checkPlayServices()");

		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {

				GooglePlayServicesUtil.getErrorDialog(resultCode, (Activity) context,
														CJayConstant.PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Log.e("DEVICE_UNSUPPORTED", "This device is not supported.");
				((Activity) context).finish();
			}
			return false;
		}
		return true;
	}

	public static File getAppDirectoryFile() {
		return new File(Environment.getExternalStorageDirectory(), CJayConstant.APP_DIRECTORY);
	}

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	public static int getAppVersionCode(Context context) {

		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;

		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	public static String getAppVersionName(Context ctx) {

		PackageInfo pInfo = null;
		try {
			pInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return pInfo.versionName;
	}

	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private static SharedPreferences getGCMPreferences(Context context) {
		// how you store the regID in your app is up to you.
		return context.getSharedPreferences(CJayActivity.class.getSimpleName(), Context.MODE_PRIVATE);
	}

	public static File getHiddentAppDirectoryFile() {
		return new File(Environment.getExternalStorageDirectory(), CJayConstant.HIDDEN_APP_DIRECTORY);
	}

	public static String getImageTypeDescription(Context ctx, int imageType) {

		switch (imageType) {
			case CJayImage.TYPE_IMPORT:
				return ctx.getResources().getString(R.string.image_type_description_import);

			case CJayImage.TYPE_EXPORT:
				return ctx.getResources().getString(R.string.image_type_description_export);

			case CJayImage.TYPE_AUDIT:
				return ctx.getResources().getString(R.string.image_type_description_report);

			case CJayImage.TYPE_REPAIRED:
			default:
				return ctx.getResources().getString(R.string.image_type_description_repaired);
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
			Logger.w("Registration not found.");
			return "";
		}

		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);

		int registeredCurrentUserId = prefs.getInt(PROPERTY_CURRENT_USER_ID, Integer.MIN_VALUE);

		int currentVersion = getAppVersionCode(context);

		try {
			if (registeredVersion != currentVersion
					|| registeredCurrentUserId != CJaySession.restore(context).getCurrentUser().getID()) {
				Logger.i("App version changed.");
				return "";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}

		return registrationId;
	}

	public static boolean hasNoConnection(final Context context) {

		return context.getSharedPreferences(PreferencesUtil.PREFS, 0).getBoolean(PreferencesUtil.PREF_NO_CONNECTION,
																					false) == true;

	}

	public static boolean isAlarmUp(Context context) {
		Intent intent = new Intent(context, QueueIntentService_.class);
		return PendingIntent.getService(context, CJayConstant.ALARM_ID, intent, PendingIntent.FLAG_NO_CREATE) != null;

	}

	public static boolean isContainerIdValid(String containerId) {

		Pattern pattern = Pattern.compile("^([A-Z]+){4,4}+(\\d{7,7}+)$");
		Matcher matcher = pattern.matcher(containerId);

		if (!matcher.matches()) return false;

		return true;
	}

	/**
	 * 
	 * @param ctx
	 * @param packageName
	 */
	public static void isStillRunning(Context ctx, String packageName) {

		ActivityManager activityManager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
		for (int i = 0; i < procInfos.size(); i++) {
			if (procInfos.get(i).processName.equals(packageName)) {
				Toast.makeText(ctx, packageName + "is running", Toast.LENGTH_LONG).show();
			}
		}
	}

	public static boolean isRunning(Context ctx, String serviceName) {

		ActivityManager manager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (service.service.getClassName().equals(serviceName)) { return true; }
		}

		return false;
	}

	public static void startAlarm(Context context) {

		Logger.Log("start Alarm Manager");
		AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		// Making Alarm for Queue Worker
		Intent intent = new Intent(context, QueueIntentService_.class);
		PendingIntent pintent = PendingIntent.getService(	context, CJayConstant.ALARM_ID, intent,
															PendingIntent.FLAG_UPDATE_CURRENT);

		Calendar cal = Calendar.getInstance();

		// start 30 seconds after boot completed
		cal.add(Calendar.SECOND, 10);

		// Start every 10 seconds
		// InexactRepeating allows Android to optimize the energy consumption
		// TODO: replace setRepeating
		alarm.setInexactRepeating(	AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), CJayConstant.ALARM_INTERVAL * 1000,
									pintent);
	}

	/**
	 * Stores the registration ID and app versionCode in the application's {@code SharedPreferences}.
	 * 
	 * @param context
	 *            application's context.
	 * @param regId
	 *            registration ID
	 */
	public static void storeRegistrationId(Context context, String regId) {

		Logger.Log("regId: " + regId);
		final SharedPreferences prefs = getGCMPreferences(context);
		int appVersion = getAppVersionCode(context);

		Logger.Log("Saving regId on app version " + appVersion);
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

	public static String replaceNull(String in, String replace) {
		return TextUtils.isEmpty(in) ? replace : in;
	}

	public static String replaceNullBySpace(String in) {
		return in == null || in.equals("") ? " " : in;
	}

	public static String stripNull(String in) {
		return in == null ? "" : in;
	}

	public static int toInt(boolean val) {
		return val ? 1 : 0;
	}

	public static String sqlString(String input) {
		return "'" + input.replace("'", "''") + "'";
	}

	public static Class<?> getHomeActivity(Context context) throws NullSessionException {

		CJaySession session = CJaySession.restore(context);
		if (session == null) { throw new NullSessionException(); }

		int userRole = 6;

		try {
			userRole = session.getCurrentUser().getRole();
		} catch (Exception e) {
			e.printStackTrace();
		}

		UserRole role = UserRole.values()[userRole];

		switch (role) {
			case GATE_KEEPER:
				return GateHomeActivity_.class;

			case AUDITOR:
				return AuditorHomeActivity_.class;

			case REPAIR_STAFF:
				return RepairHomeActivity_.class;

			default:
				break;
		}
		DataCenter.getDatabaseHelper(context).addUsageLog("Error | Cannot start Home Activity");
		return null;
	}

	public static Intent getUploadAllIntent(Context context) {
		Intent intent = new Intent(context, PhotoUploadService_.class);
		intent.setAction(CJayConstant.INTENT_SERVICE_UPLOAD_ALL);
		return intent;
	}

	public static boolean canStopAlarm(Context context) {

		if (PreferencesUtil.getPrefsValue(context, PreferencesUtil.PREF_EMPTY_CONTAINER_QUEUE, false)
				&& PreferencesUtil.getPrefsValue(context, PreferencesUtil.PREF_EMPTY_PHOTO_QUEUE, false)) { return true; }

		return false;
	}
}
