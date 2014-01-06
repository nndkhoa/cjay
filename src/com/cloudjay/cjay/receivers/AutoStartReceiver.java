package com.cloudjay.cjay.receivers;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.cloudjay.cjay.service.QueueIntentService;

public class AutoStartReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {

		Log.i("AutoStart", "**********started************");

		// Making Alarm for Queue Worker
		intent = new Intent(context, QueueIntentService.class);
		PendingIntent pintent = PendingIntent.getService(context, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		Calendar current = Calendar.getInstance();
		AlarmManager alarm = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);

		// Start every 30 seconds
		alarm.setRepeating(AlarmManager.RTC_WAKEUP, current.getTimeInMillis(),
				10 * 1000, pintent);
	}
}
