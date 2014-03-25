package com.cloudjay.cjay.receivers;

import org.androidannotations.annotations.EReceiver;

import com.aerilys.helpers.android.NetworkHelper;
import com.cloudjay.cjay.CJayApplication;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.PreferencesUtil;
import com.cloudjay.cjay.util.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

@EReceiver
public class ConnectionChangeReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		Logger.w("on Connection change receiver");
		if (NetworkHelper.isConnected(context)) {

			Logger.Log("Connected to Internet");
			PreferencesUtil.storePrefsValue(context,
					PreferencesUtil.PREF_NO_CONNECTION, false);

			if (!Utils.isAlarmUp(CJayApplication.getContext())) {
				Logger.Log("Alarm Manager is not running.");
				Utils.startAlarm(CJayApplication.getContext());
			}

			Toast.makeText(context, "Connected to Internet", Toast.LENGTH_SHORT)
					.show();

		} else {

			Logger.Log("Not connect to Internet");

			// Mark that device has no data connection
			PreferencesUtil.storePrefsValue(context,
					PreferencesUtil.PREF_NO_CONNECTION, true);

			// BUG: alarm is always running :|
			if (Utils.isAlarmUp(CJayApplication.getContext())) {
				Logger.Log("Alarm Manager is running.");
				Utils.cancelAlarm(CJayApplication.getContext());
			}

			Toast.makeText(context, "Not connect to Internet",
					Toast.LENGTH_SHORT).show();
		}
	}
}
