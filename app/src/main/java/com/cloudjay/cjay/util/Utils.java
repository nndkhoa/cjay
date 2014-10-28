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
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.AuditImage;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.model.GateImage;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.service.QueueIntentService_;
import com.snappydb.DB;
import com.snappydb.SnappydbException;

import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class Utils {

	public static boolean isRunning(Context ctx, String serviceName) {

		ActivityManager manager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (service.service.getClassName().equals(serviceName)) {
				return true;
			}
		}

		return false;
	}

	public static void startAlarm(Context context) {

		Logger.Log("start Alarm Manager");
		AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		// Making Alarm for Queue Worker
		Intent intent = new Intent(context, QueueIntentService_.class);
		PendingIntent pIntent = PendingIntent.getService(context, CJayConstant.ALARM_ID, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		Calendar cal = Calendar.getInstance();

		// start 30 seconds after boot completed
		cal.add(Calendar.SECOND, 30);

		// Start every 24 hours
		alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 86400 * 1000,
				pIntent);
	}

	public static String getAppVersionName(Context ctx) {

		PackageInfo pInfo = null;
		try {
			pInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return pInfo.versionName;
	}

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

	public static int getRole(Context context) {
		return Integer.valueOf(PreferencesUtil.getPrefsValue(context, PreferencesUtil.PREF_USER_ROLE));
	}

	//Check containerID is valid or not
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

	public static boolean simpleValid(String containerID) {
		Pattern pattern = Pattern.compile("^([A-Z]+){4,4}+(\\d{7,7}+)$");
		Matcher matcher = pattern.matcher(containerID);
		if (!matcher.matches()) return false;
		return true;
	}

	public static String replaceNullBySpace(String in) {
		return in == null || in.equals("") ? " " : in;
	}

	/**
	 * Convert container session json to Session Object.
	 * Need to check if container is existed or not. (should use insert or update concept)
	 *
	 * @param context
	 * @param session
	 * @return
	 */
	public static Session parseSession(Context context, Session session) throws SnappydbException {

		//Check available session
		DB snappyDb = App.getDB(context);
		Session found = snappyDb.getObject(session.getContainerId(), Session.class);

		// If hasn't -> create
		if (found == null) {
			snappyDb.put(session.getContainerId(), session);
			return session;
		}

		// else -> update
		else {
			snappyDb.del(session.getContainerId());
			snappyDb.put(session.getContainerId(), session);
			return session;
		}


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
					if (auditImage.isUploadStatus()) {
						uploadedImage = uploadedImage + 1;
					}
				}

			}
		}
		List<GateImage> gateImages = session.getGateImages();
		for (GateImage gateImage : gateImages) {
			if (gateImage.isUploadStatus()) {
				uploadedImage = uploadedImage + 1;
			}
		}
		return uploadedImage;
	}

	public static String getImageTypeDescription(Context ctx, int imageType) {

		switch (imageType) {
			case CJayConstant.TYPE_IMPORT:
				return ctx.getResources().getString(R.string.image_type_description_import);

			case CJayConstant.TYPE_EXPORT:
				return ctx.getResources().getString(R.string.image_type_description_export);

			case CJayConstant.TYPE_AUDIT:
				return ctx.getResources().getString(R.string.image_type_description_report);

			case CJayConstant.TYPE_REPAIRED:
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

	public static boolean isAlarmUp(Context context) {
		Intent intent = new Intent(context, QueueIntentService_.class);
		return PendingIntent.getService(context, CJayConstant.ALARM_ID, intent, PendingIntent.FLAG_NO_CREATE) != null;

	}

}
