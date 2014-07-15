package com.cloudjay.cjay.receivers;

import org.androidannotations.annotations.EReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.StringHelper;
import com.cloudjay.cjay.util.Utils;

@EReceiver
public class AutoStartReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		Logger.Log("**********started************");
		DataCenter.getDatabaseHelper(context).addUsageLog(context, "#autostart Application");

		// TODO: refactor if needed
		// Making Alarm for Queue Worker
		if (!Utils.isAlarmUp(context)) {

			Logger.Log("Alarm Manager is not running.");
			Utils.startAlarm(context);

		} else {
			Logger.Log("Alarm is already running "
					+ StringHelper.getCurrentTimestamp(CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE));
		}

		// Utils.cancelThenStartAlarm(context);
	}
}
