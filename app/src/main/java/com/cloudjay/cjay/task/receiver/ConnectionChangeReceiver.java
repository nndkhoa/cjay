package com.cloudjay.cjay.task.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cloudjay.cjay.util.Utils;

import org.androidannotations.annotations.EReceiver;

@EReceiver
public class ConnectionChangeReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Utils.keepNotificationAlive(context);

		if (!Utils.isAlarmUp(context))
			Utils.startAlarm(context);
	}
}