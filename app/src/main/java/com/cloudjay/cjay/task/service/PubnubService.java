package com.cloudjay.cjay.task.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.event.NotificationItemReceivedEvent;
import com.cloudjay.cjay.event.pubnub.PubnubSubscriptionChangedEvent;
import com.cloudjay.cjay.model.NotificationItem;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.task.job.GetNotificationJob;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.PreferencesUtil;
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
import org.androidannotations.annotations.Trace;

import de.greenrobot.event.EventBus;

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
	private static final int DELAY_TIME = 200;

	String depotChannel;
	String uuidChannel;
	Pubnub pubnub;

//	/**
//	 * Display notification in Notification Center
//	 *
//	 * @param channel
//	 * @param objectType
//	 * @param objectId
//	 * @param messageId
//	 */
//	public void pushNotification(String channel, String objectType, long objectId, String messageId) {
//		JobManager jobManager = App.getJobManager();
//		jobManager.addJobInBackground(new GetNotificationJob(channel, messageId, objectType, objectId));
//	}

    /**
     * Display notification in Notification Center
     *
     * @param channel
     * @param objectType
     * @param objectId
     * @param messageId
     */
    public void pushNotification(String channel, String objectType, long objectId, String messageId,String container_id) {
        JobManager jobManager = App.getJobManager();
        jobManager.addJobInBackground(new GetNotificationJob(channel, messageId, objectType, objectId,container_id));
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

            Logger.e(b.toString());

			final String channel = b.getString("channel");
			final String objectType = b.getString("object_type");
			final long objectId = b.getLong("object_id");
			final String messageId = b.getString("message_id");
            final String containerId = b.getString("container_id");

			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {

				@Override
				public void run() {
					pushNotification(channel, objectType, objectId, messageId, containerId);
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
	public void notifyUser(String channel, Object message) {

		Message msg = handler.obtainMessage();
		try {
//			Logger.Log("Message: " + message.toString());

			JsonParser jsonParser = new JsonParser();
			JsonObject jo = (JsonObject) jsonParser.parse(message.toString());

			Gson gson = new Gson();
			NotificationItem item = gson.fromJson(jo, NotificationItem.class);

			Bundle b = new Bundle();
			b.putString("channel", channel);
			b.putString("object_type", item.getObjectType());
			b.putLong("object_id", item.getObjectId());
			b.putString("message_id", item.getMessageId());
            b.putString("container_id", item.getContainerId());
			msg.setData(b);
			handler.sendMessage(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void pushNotification(Session session, int type) {
		Context context = App.getInstance().getApplicationContext();

		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification;
		if (type == 1) {
			notification = new Notification.Builder(context).setContentTitle("Đã cập nhật thông tin")
					.setContentText(" → Container " + session.getContainerId())
					.setSmallIcon(R.drawable.ic_app_small)
					.setAutoCancel(true)
					.build();
		} else {

			notification = new Notification.Builder(context).setContentTitle("Đã cập nhật thông tin")
					.setContentText(" → Container " + session.getContainerId())
					.setSmallIcon(R.drawable.ic_app_small)
					.setAutoCancel(true)
					.setDefaults(Notification.DEFAULT_SOUND).build();
		}
		notificationManager.notify(CJayConstant.NOTIFICATION_ID, notification);
	}

	public void onEventMainThread(NotificationItemReceivedEvent event) {
		pushNotification(event.getSession(), event.getType());
	}

	@Override
	public void onDestroy() {
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		EventBus.getDefault().register(this);

		String token = PreferencesUtil.getPrefsValue(getApplicationContext(), PreferencesUtil.PREF_TOKEN);
		if (!TextUtils.isEmpty(token)) {

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
					e.printStackTrace();

				} catch (NullCredentialException e) {

					// Log out instantly and stop Pubnub Service also
					Utils.logOut(getApplicationContext());

//					// Open Login Activity w/ message
//					Intent loginIntent = new Intent(getApplicationContext(), LoginActivity_.class);
//					loginIntent.putExtra("EXIT", true);
//					startActivity(loginIntent);
				}
			}

			if (!TextUtils.isEmpty(depotChannel) && !TextUtils.isEmpty(uuidChannel) && Utils.canReachInternet()) {

				try {
					Logger.w(" > Prepare to subscribe to Pubnub channels");
					String[] channels = new String[]{depotChannel, uuidChannel};
					pubnub.setUUID(uuidChannel);
					pubnub.subscribe(channels, new Callback() {

						@Override
						public void connectCallback(String channel, Object message) {
							// store pref subscribe mode in shared preferences
							PreferencesUtil.storePrefsValue(getApplicationContext(),
									PreferencesUtil.PREF_SUBSCRIBE_PUBNUB, true);

							EventBus.getDefault().post(new PubnubSubscriptionChangedEvent(true));

							System.out.println("SUBSCRIBE : CONNECT on channel:" + channel
									+ " : " + message.getClass() + " : "
									+ message.toString());

						}

						@Override
						public void disconnectCallback(String channel, Object message) {
							// store pref subscribe mode in shared preferences
							PreferencesUtil.storePrefsValue(getApplicationContext(),
									PreferencesUtil.PREF_SUBSCRIBE_PUBNUB, false);

							EventBus.getDefault().post(new PubnubSubscriptionChangedEvent(false));

							System.out.println("SUBSCRIBE : DISCONNECT on channel:" + channel
									+ " : " + message.getClass() + " : "
									+ message.toString());

						}

						@Override
						public void reconnectCallback(String channel, Object message) {

							// store pref subscribe mode in shared preferences
							PreferencesUtil.storePrefsValue(getApplicationContext(),
									PreferencesUtil.PREF_SUBSCRIBE_PUBNUB, true);
							System.out.println("SUBSCRIBE : RECONNECT on channel:" + channel
									+ " : " + message.getClass() + " : "
									+ message.toString());
							EventBus.getDefault().post(new PubnubSubscriptionChangedEvent(true));
						}

						@Override
						public void successCallback(String channel, Object message) {
							notifyUser(uuidChannel, message);
						}

						@Override
						public void errorCallback(String channel, PubnubError pubnubError) {
							Logger.e("Error: " + pubnubError.toString());
						}
					});

				} catch (PubnubException e) {

					Logger.e(e.getMessage());
					dataCenter.addLog(getApplicationContext(), "PubNub", "Cannot subscribe channels", CJayConstant.PREFIX_NOTIFICATION_LOG);
				}
			} else {
				Logger.w("Auto stop Pubnub service");
				stopSelf();
			}

		} else {
			Logger.wtf("There was problems. Please check credential.");
			stopSelf();
		}
	}
}
