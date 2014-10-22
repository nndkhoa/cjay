package com.cloudjay.cjay.task.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.util.Logger;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

/**
 * PubNub notification service
 */
public class PubnubService extends Service {

	/**
	 * Notification manager is used for display message in Notification Center
	 */
	private NotificationManager notificationManager;

	// Define PubNub Key
	private static final String PUBLISH_KEY = "pub-c-d4a2608d-f440-4ebf-a09a-dd8a570428cd";
	private static final String SUBSCRIBE_KEY = "sub-c-fe158864-9fcf-11e3-a937-02ee2ddab7fe";

	// Define PubNub Channel
	private static final String DEPOT_CHANNEL = "depot_channel";
    private static final String UUID_CHANNEL = "uuid_channel";

	private String[] channels = new String[]{
            DEPOT_CHANNEL,
            UUID_CHANNEL,
	};

	private static final int DELAY_TIME = 200;

	// Initial PubNub
	Pubnub pubnub = new Pubnub(PUBLISH_KEY, SUBSCRIBE_KEY);

	/**
	 * Display notification in Notification Center
	 *
	 * @param channel
	 * @param message
	 */
	private void pushNotification(String channel, String message) {

		//Todo: Push notification
		Notification notification = new Notification.Builder(this).setContentTitle(channel)
				.setContentText(message)
				.setSmallIcon(R.drawable.ic_launcher)
				.setAutoCancel(true)
				.setDefaults(Notification.DEFAULT_SOUND).build();

		notificationManager.notify(1, notification);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * Handler is used for post new notification
	 */
	private final Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			Bundle b = msg.getData();
			final String channel = b.getString("channel");
			final String message = b.getString("message");

			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					pushNotification(channel, message);
				}
			}, DELAY_TIME);
		}
	};

	private void notifyUser(String channel, Object message) {

		Message msg = handler.obtainMessage();
		try {
			Logger.Log("Received msg : " + message.toString());
			String obj = (String) message;
			Bundle b = new Bundle();
			b.putString("channel", channel);
			b.putString("message", obj);
			msg.setData(b);
			handler.sendMessage(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onCreate() {

		super.onCreate();
		// TODO: Add PowerManager

		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		try {
			pubnub.subscribe(channels, new Callback() {
				@Override
				public void successCallback(String channel, Object message) {
					Logger.Log("Success: " + message.toString());
					notifyUser(channel, message);
				}

				@Override
				public void errorCallback(String channel, PubnubError pubnubError) {
					Logger.e("Error: " + pubnubError.toString());
					notifyUser(channel, pubnubError.toString());
				}
			});
		} catch (PubnubException e) {
			e.printStackTrace();
		}
	}
}
