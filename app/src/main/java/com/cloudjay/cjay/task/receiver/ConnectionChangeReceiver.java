package com.cloudjay.cjay.task.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.activity.HomeActivity_;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Utils;

import org.androidannotations.annotations.EReceiver;
import org.androidannotations.annotations.SystemService;

@EReceiver
public class ConnectionChangeReceiver extends BroadcastReceiver {

	@SystemService
	NotificationManager notificationManager;

	String contentText;
	String tickerText;
	String contentTitle;

	@Override
	public void onReceive(Context context, Intent intent) {

		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				new Intent(context, HomeActivity_.class), PendingIntent.FLAG_UPDATE_CURRENT);

		if (Utils.canReachInternet()) {
			tickerText = "Connected to Internet";
			contentText = "Connected";
			contentTitle = "CJay Network";

		} else {
			tickerText = "Disconnected from Internet";
			contentText = "Disconnected";
			contentTitle = "CJay Network";
		}

		// Change notification message to connected
		Notification.Builder builder = new Notification.Builder(context)
				.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_app_360))
				.setSmallIcon(R.drawable.ic_app_small).setTicker(tickerText)
				.setWhen(System.currentTimeMillis())
				.setContentTitle(contentTitle)
				.setOngoing(true)
				.setContentText(contentText)
				.setContentIntent(contentIntent);

		notificationManager.notify(CJayConstant.PERMANENT_NOTIFICATION_ID, builder.build());
	}
}