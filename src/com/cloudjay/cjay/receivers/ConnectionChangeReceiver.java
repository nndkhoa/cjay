package com.cloudjay.cjay.receivers;

import com.aerilys.helpers.android.NetworkHelper;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.PreferencesUtil;
import com.cloudjay.cjay.util.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class ConnectionChangeReceiver extends BroadcastReceiver {

	public static final String LOG_TAG = "ConnectionChangeReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {

		if (NetworkHelper.isConnected(context)) {
			Logger.Log(LOG_TAG, "Connected to Internet");

			PreferencesUtil.storePrefsValue(context,
					PreferencesUtil.PREF_NO_CONNECTION, false);

			if (!Utils.isAlarmUp(context)) {
				Logger.Log(LOG_TAG, "Alarm Manager is not running.");
				Utils.startAlarm(context);
			}

			Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show();

		} else {
			Logger.Log(LOG_TAG, "Not connect to Internet");

			// Mark that device has no data connection
			PreferencesUtil.storePrefsValue(context,
					PreferencesUtil.PREF_NO_CONNECTION, true);

			if (Utils.isAlarmUp(context)) {
				Logger.Log(LOG_TAG, "Alarm Manager is running.");
				Utils.cancelAlarm(context);
			}
			
			Toast.makeText(context, "Not connect to Internet",
					Toast.LENGTH_SHORT).show();
		}
	}
}
