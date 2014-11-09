package com.cloudjay.cjay.task.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.text.TextUtils;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.model.NotificationItem;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.task.job.GetNotificationJob;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.Utils;
import com.cloudjay.cjay.util.exception.NullCredentialException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.path.android.jobqueue.JobManager;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;
import com.snappydb.SnappydbException;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;

/**
 * Pubnub service is used to handle notification from Server by using Pubnub API
 */
@EService
public class PubnubService extends Service {

	@Bean
	DataCenter dataCenter;

	/**
	 * Notification manager is used for display message in Notification Center
	 */
	NotificationManager notificationManager;
	PowerManager.WakeLock wl = null;

	private static final int DELAY_TIME = 200;

	String depotChannel;
	String uuidChannel;
	Pubnub pubnub;

	/**
	 * Display notification in Notification Center
	 *
	 * @param channel
	 * @param objectType
	 * @param objectId
	 * @param messageId
	 */
	public void pushNotification(String channel, String objectType, long objectId, String messageId) {
		JobManager jobManager = App.getJobManager();
		jobManager.addJobInBackground(new GetNotificationJob(channel, messageId, objectType, objectId));
	}


	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * Handler is used for post new notification. --> Handle notification here.
	 */
	private final Handler handler = new Handler() {

		public void handleMessage(Message msg) {

			Bundle b = msg.getData();
			final String channel = b.getString("channel");
			final String objectType = b.getString("object_type");
			final long objectId = b.getLong("object_id");
			final String messageId = b.getString("message_id");

			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {

				@Override
				public void run() {
					pushNotification(channel, objectType, objectId, messageId);
				}

			}, DELAY_TIME);
		}
	};

	/**
	 * Dispatch notification message to Handler
	 *
	 * @param channel
	 * @param message
	 */
	private void notifyUser(String channel, Object message) {

		Message msg = handler.obtainMessage();
		try {
			Logger.Log("Message: " + message.toString());

			JsonParser jsonParser = new JsonParser();
			JsonObject jo = (JsonObject) jsonParser.parse(message.toString());

			Gson gson = new Gson();
			NotificationItem item = gson.fromJson(jo, NotificationItem.class);

			Bundle b = new Bundle();
			b.putString("channel", channel);
			b.putString("object_type", item.getObjectType());
			b.putLong("object_id", item.getObjectId());
			b.putString("message_id", item.getMessageId());
			msg.setData(b);
			handler.sendMessage(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onCreate() {
		super.onCreate();

//		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//		wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SubscribeAtBoot");
//		if (wl != null) {
//			wl.acquire();
//		}

		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		pubnub = new Pubnub(CJayConstant.PUBLISH_KEY, CJayConstant.SUBSCRIBE_KEY);
		if (TextUtils.isEmpty(depotChannel) || TextUtils.isEmpty(uuidChannel)) {

			// Get info from Database
			try {

				User user = dataCenter.getUser(getApplicationContext());
				depotChannel = user.getChannelDepot();
				uuidChannel = user.getChannelUuid();

				Logger.Log("Depot channel: " + depotChannel);
				Logger.Log("UUID channel: " + uuidChannel);

			} catch (SnappydbException e) {
				Logger.w(e.getMessage());

			} catch (NullCredentialException e) {

				// Log out instantly
				Utils.logOut(getApplicationContext());
			}
		}

		if (!TextUtils.isEmpty(depotChannel) && !TextUtils.isEmpty(uuidChannel) && Utils.canReachInternet()) {

			try {
				Logger.Log(" > Prepare to subscribe to Pubnub channels");
				String[] channels = new String[]{depotChannel, uuidChannel};
				pubnub.setUUID(uuidChannel);
				pubnub.subscribe(channels, new Callback() {

					@Override
					public void connectCallback(String channel, Object message) {
						System.out.println("SUBSCRIBE : CONNECT on channel:" + channel
								+ " : " + message.getClass() + " : "
								+ message.toString());
					}

					@Override
					public void disconnectCallback(String channel, Object message) {
						System.out.println("SUBSCRIBE : DISCONNECT on channel:" + channel
								+ " : " + message.getClass() + " : "
								+ message.toString());
					}

					public void reconnectCallback(String channel, Object message) {
						System.out.println("SUBSCRIBE : RECONNECT on channel:" + channel
								+ " : " + message.getClass() + " : "
								+ message.toString());
					}

					@Override
					public void successCallback(String channel, Object message) {
//					Logger.Log("Success: " + message.toString());
						notifyUser(uuidChannel, message);
					}

					@Override
					public void errorCallback(String channel, PubnubError pubnubError) {
						Logger.e("Error: " + pubnubError.toString());
					}
				});

			} catch (PubnubException e) {

				Logger.e(e.getMessage());
				dataCenter.addLog(getApplicationContext(), "PubNub", "Cannot subscribe channels");

			}
		} else {

			Logger.w("Auto stop Pubnub service");
			stopSelf();

		}
	}
}
