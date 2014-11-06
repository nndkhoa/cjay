package com.cloudjay.cjay.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.AuditImage;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.model.GateImage;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.service.PubnubService_;
import com.cloudjay.cjay.task.service.QueueIntentService_;
import com.cloudjay.cjay.util.enums.ImageType;
import com.cloudjay.cjay.util.enums.UploadStatus;
import com.pubnub.api.Pubnub;
import com.snappydb.DB;
import com.snappydb.SnappydbException;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class Utils {

	public static void logOut(Context context) {

        // Unsubscribe channel pubnub
        Pubnub pubnub = new Pubnub(CJayConstant.PUBLISH_KEY, CJayConstant.SUBSCRIBE_KEY);
        pubnub.unsubscribeAllChannels();

		// Clear preference and Database
		PreferencesUtil.clearPrefs(context);
		context.deleteDatabase("db_default_job_manager.db");

		try {
			DB db = App.getDB(context);
			db.destroy();
		} catch (SnappydbException e) {
			Logger.w(e.getMessage());
		}
	}

	/**
	 * Check a intent service is running or not
	 *
	 * @param ctx
	 * @param serviceName
	 * @return
	 */
	public static boolean isRunning(Context ctx, String serviceName) {

		ActivityManager manager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (service.service.getClassName().equals(serviceName)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Init alarm manager to start QueueIntentService
	 *
	 * @param context
	 */
	public static void startAlarm(Context context) {

		// start 30 seconds after boot completed
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, 30);

		AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		// Making Alarm for Queue Worker
		Intent intent = new Intent(context, QueueIntentService_.class);
		PendingIntent pQueueIntent = PendingIntent.getService(context, CJayConstant.ALARM_QUEUE_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		// Start every 24 hours
		alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
				CJayConstant.ALARM_INTERVAL * 1000, pQueueIntent);

		// --------
		// Configure Pubnub Service

		String token = PreferencesUtil.getPrefsValue(context, PreferencesUtil.PREF_TOKEN);
		if (!TextUtils.isEmpty(token)) {

			Intent pubnubIntent = new Intent(context, PubnubService_.class);
			PendingIntent pPubnubIntent = PendingIntent.getService(context, CJayConstant.ALARM_PUBNUB_ID, pubnubIntent, PendingIntent.FLAG_UPDATE_CURRENT);

			// wake up every 5 minutes to ensure service stays alive
			alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
					(5 * 60 * 1000), pPubnubIntent);
		}
	}

	/**
	 * Use to check if alarm is up or not
	 *
	 * @param context
	 * @return
	 */
	public static boolean isAlarmUp(Context context) {

		Intent queueIntent = new Intent(context, QueueIntentService_.class);
		Intent pubnubIntent = new Intent(context, PubnubService_.class);

		boolean queueUp = PendingIntent.getService(context, CJayConstant.ALARM_QUEUE_ID, queueIntent, PendingIntent.FLAG_NO_CREATE) != null;
		boolean pubnubUp = PendingIntent.getService(context, CJayConstant.ALARM_PUBNUB_ID, pubnubIntent, PendingIntent.FLAG_NO_CREATE) != null;

		if (!queueUp)
			Logger.w("Queue Service is not running");

		if (!pubnubUp)
			Logger.w("Pubnub Service is not running");

		if (queueUp && pubnubUp)
			return true;
		else
			return false;
	}

	public static void cancelAlarm(Context context) {

		Logger.Log("stop Alarm Manager");

		Intent intent = new Intent(context, QueueIntentService_.class);
		PendingIntent sender = PendingIntent.getService(context, CJayConstant.ALARM_QUEUE_ID, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		alarmManager.cancel(sender);
		sender.cancel();
	}


	/**
	 * Get app version name
	 *
	 * @param ctx
	 * @return
	 */
	public static String getAppVersionName(Context ctx) {

		PackageInfo pInfo = null;
		try {
			pInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return pInfo.versionName;
	}

	/**
	 * Display a pretty alert
	 *
	 * @param context
	 * @param textResId
	 */
	public static void showCrouton(Activity context, int textResId) {
		Crouton.cancelAllCroutons();
		final Crouton crouton = Crouton.makeText(context, textResId, Style.ALERT);
		crouton.setConfiguration(new de.keyboardsurfer.android.widget.crouton.Configuration.Builder().setDuration(de.keyboardsurfer.android.widget.crouton.Configuration.DURATION_INFINITE).build());
		crouton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Crouton.hide(crouton);
			}
		});
		crouton.show();
	}

	/**
	 * Display a pretty alert
	 *
	 * @param context
	 * @param message
	 */
	public static void showCrouton(Activity context, String message, Style style) {
		Crouton.cancelAllCroutons();
		final Crouton crouton = Crouton.makeText(context, message, style);
		crouton.setConfiguration(new Configuration.Builder()
				.setDuration(Configuration.DURATION_SHORT).build());
		crouton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Crouton.hide(crouton);
			}
		});
		crouton.show();
	}

	/**
	 * Display a pretty alert
	 *
	 * @param context
	 * @param message
	 */
	public static void showCrouton(Activity context, String message) {
		Crouton.cancelAllCroutons();
		final Crouton crouton = Crouton.makeText(context, message, Style.ALERT);
		crouton.setConfiguration(new de.keyboardsurfer.android.widget.crouton.Configuration.Builder().setDuration(de.keyboardsurfer.android.widget.crouton.Configuration.DURATION_INFINITE).build());
		crouton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Crouton.hide(crouton);
			}
		});
		crouton.show();
	}

	/**
	 * Return role of current user
	 *
	 * @param context
	 * @return
	 */
	public static int getRole(Context context) {
		return Integer.valueOf(PreferencesUtil.getPrefsValue(context, PreferencesUtil.PREF_USER_ROLE));
	}

	/**
	 * Check containerID is valid or not
	 *
	 * @param containerId
	 * @return
	 */
	public static boolean isContainerIdValid(String containerId) {

		//if (!Logger.isDebuggable()) {

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
		//}

		//return true;
	}

	/**
	 * Check if container id is valid based on ISO 6346
	 *
	 * @param containerID
	 * @return
	 */
	public static boolean simpleValid(String containerID) {
		Pattern pattern = Pattern.compile("^([A-Z]+){4,4}+(\\d{7,7}+)$");
		Matcher matcher = pattern.matcher(containerID);
		if (!matcher.matches()) return false;
		return true;
	}

	/**
	 * Replace a null string by space
	 *
	 * @param in
	 * @return
	 */
	public static String replaceNullBySpace(String in) {
		return in == null || in.equals("") ? " " : in;
	}

	/**
	 * Count total image off session
	 *
	 * @param session
	 * @return
	 */
	public static int countTotalImage(Session session) {
		int totalImage = 0;
		List<AuditItem> auditItems = session.getAuditItems();
		if (auditItems != null) {
			for (AuditItem auditItem : auditItems) {
				totalImage = totalImage + auditItem.getAuditImages().size();
			}
			totalImage = totalImage + session.getGateImages().size();
			return totalImage;
		} else {
			totalImage = session.getGateImages().size();
			return totalImage;
		}
	}

	public static int countUploadedImage(Session session) {
		int uploadedImage = 0;
		List<AuditItem> auditItems = session.getAuditItems();
		if (auditItems != null) {
			for (AuditItem auditItem : auditItems) {
				List<AuditImage> auditImages = auditItem.getAuditImages();
				for (AuditImage auditImage : auditImages) {
					if (auditImage.getUploadStatus() == UploadStatus.COMPLETE.value) {
						uploadedImage = uploadedImage + 1;
					}
				}

			}
		}

		List<GateImage> gateImages = session.getGateImages();
		for (GateImage gateImage : gateImages) {
			if (gateImage.getUploadStatus() == UploadStatus.COMPLETE.value) {
				uploadedImage = uploadedImage + 1;
			}
		}
		return uploadedImage;
	}

	public static String getImageTypeDescription(Context ctx, int type) {

		ImageType imageType = ImageType.values()[type];

		switch (imageType) {
			case IMPORT:
				return ctx.getResources().getString(R.string.image_type_description_import);

			case EXPORT:
				return ctx.getResources().getString(R.string.image_type_description_export);

			case AUDIT:
				return ctx.getResources().getString(R.string.image_type_description_report);

			case REPAIRED:
			default:
				return ctx.getResources().getString(R.string.image_type_description_repaired);
		}
	}

	/**
	 * Cấu hình cho Text box theo chuẩn iso
	 */
	public static void setupEditText(final EditText editText) {

		final Pattern pattern = Pattern.compile("^[a-zA-Z]{4}");

		// Cấu hình filter cho text box
		// Chỉ cho phép nhập chữ vào số
		InputFilter isLetterAndDigitFilter = new InputFilter() {

			@Override
			public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

				for (int i = start; i < end; i++) {
					if (!Character.isLetterOrDigit(source.charAt(i))) {
						return "";
					}
				}
				return null;
			}
		};

		// Chỉ cho phép nhập 4 kí tự và 7 số
		InputFilter validCharacterFilter = new InputFilter() {
			@Override
			public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

				// dest là kết quả sau khi được append source character vào
				// source là kí tự được user nhập vào

				if (dest.length() > 10)
					return "";


				if (dest.length() < 4) {
					Pattern temp = Pattern.compile("^[a-zA-Z]");
					Matcher matcher = temp.matcher(source);
					if (!matcher.matches()) {
						return "";
					}
					return source.toString().toUpperCase();
				} else {
					Pattern temp = Pattern.compile("[0-9]$");
					Matcher matcher = temp.matcher(source);
					if (!matcher.matches()) {
						return "";
					}
					return source.toString().toUpperCase();
				}
			}
		};

		// Set input filter for search text box
		editText.setFilters(new InputFilter[]{isLetterAndDigitFilter, validCharacterFilter});

		// Set keyboard type
		editText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
		editText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				Matcher matcher = pattern.matcher(s);
				if (s.length() < 4) {
					if (editText.getInputType() != InputType.TYPE_CLASS_TEXT) {
						editText.setInputType(InputType.TYPE_CLASS_TEXT
								| InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
					}
				} else if (matcher.matches()) {
					if (editText.getInputType() != InputType.TYPE_CLASS_NUMBER) {
						editText.setInputType(InputType.TYPE_CLASS_NUMBER);
					}
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
	}

	public static String parseUrltoUri(String url) {
		if (url.contains("file://")) {
			String uri = url.substring(7);
			return uri;
		}
		return null;
	}

	public static String getImageNameFromUrl(String url) {
		String name = url.substring(url.lastIndexOf("/") + 1,
				url.length());
		return name;
	}

	public static boolean canReachInternet() {

		//StackTraceElement[] trace = new Throwable().getStackTrace();
		//Logger.Log(trace[1].getFileName() + "#" + trace[1].getMethodName() + "() | Line: " + trace[1].getLineNumber());


		Runtime runtime = Runtime.getRuntime();

		try {
			Process mIpAddrProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
			int mExitValue = mIpAddrProcess.waitFor();
			// System.out.println(" mExitValue " + mExitValue);

			if (mExitValue == 0) {
				return true;
			} else {
				return false;
			}
		} catch (InterruptedException ignore) {
//			ignore.printStackTrace();
//			System.out.println(" Exception:" + ignore);
		} catch (IOException e) {
//			e.printStackTrace();
//			System.out.println(" Exception:" + e);
		}
		return false;
	}
}
