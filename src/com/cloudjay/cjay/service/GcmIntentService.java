package com.cloudjay.cjay.service;

import org.androidannotations.annotations.EIntentService;
import org.androidannotations.annotations.SystemService;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.receivers.GcmBroadcastReceiver;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.Logger;
import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * 
 * Handle notification from server
 * 
 * @author anhqnguyen
 * 
 */

@EIntentService
public class GcmIntentService extends IntentService {

	public static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;
	static final String LOG_TAG = "GcmIntentService";
	NotificationCompat.Builder builder;

	@SystemService
	AudioManager audioManager;

	MediaPlayer shootMediaPlayer = null;

	public GcmIntentService() {
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		try {
			Log.i(LOG_TAG, "onHandleIntent()");

			Bundle extras = intent.getExtras();
			GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

			// The getMessageType() intent parameter must be the intent you
			// received
			// in your BroadcastReceiver.
			String messageType = gcm.getMessageType(intent);

			if (!extras.isEmpty()) { // has effect of unparcelling Bundle

				Logger.Log(LOG_TAG, "Extra is not empty");
				Logger.Log(LOG_TAG, "Message Type: " + messageType);

				/*
				 * Filter messages based on message type. Since it is likely
				 * that GCM will be extended in the future with new message
				 * types, just ignore any message types you're not interested
				 * in, or that you don't recognize.
				 */

				if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR
						.equals(messageType)) {

					Logger.Log(LOG_TAG, "Send error: " + extras.toString());
					sendNotification("Send error: " + extras.toString());

				} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
						.equals(messageType)) {

					Logger.Log(LOG_TAG,
							"Deleted messages on server: " + extras.toString());

					sendNotification("Deleted messages on server: "
							+ extras.toString());

					// If it's a regular GCM message, do some work.
				} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
						.equals(messageType)) {

					Logger.Log(LOG_TAG, "Received: " + extras.toString());
					sendNotification(extras);

				} else {
					//
					Logger.Log(LOG_TAG, "Last case - " + extras.toString());
					Logger.Log(LOG_TAG, "MessageType: " + messageType);

					int id = -1;
					try {
						id = Integer.parseInt(extras.getString("id"));
					} catch (Exception e) {

					}

					String msg = extras.getString("msg");
					String type = extras.getString("type");

					Logger.Log(LOG_TAG,
							"Notification got Type = " + type + " | MSG = "
									+ msg + " | Id = " + Integer.toString(id));

				}

				// int volume = audioManager
				// .getStreamVolume(AudioManager.STREAM_NOTIFICATION);
				//
				// if (volume != 0) {
				// if (shootMediaPlayer == null)
				// shootMediaPlayer = MediaPlayer
				// .create(getApplicationContext(),
				// Uri.parse("file:///system/media/audio/ui/camera_click.ogg"));
				//
				// if (shootMediaPlayer != null)
				// shootMediaPlayer.start();
				// }

			} else {
				Logger.Log(LOG_TAG, "Extra is Empty");
			}

			// Release the wake lock provided by the WakefulBroadcastReceiver.
			GcmBroadcastReceiver.completeWakefulIntent(intent);

		} catch (Exception e) {
			e.printStackTrace();
			GcmBroadcastReceiver.completeWakefulIntent(intent);
		}
	}

	// Put the message into a notification and post it.
	// This is just one simple example of what you might choose to do with
	// a GCM message.
	private void sendNotification(Bundle extras) {

		Logger.Log(LOG_TAG, "sendNotification");

		try {
			mNotificationManager = (NotificationManager) this
					.getSystemService(Context.NOTIFICATION_SERVICE);

			int id = -1;
			try {
				id = Integer.parseInt(extras.getString("id"));
			} catch (Exception e) {

			}

			String type = extras.getString("type");

			Logger.Log(LOG_TAG, "Notification got Type = " + type + " | Id = "
					+ Integer.toString(id));

			if (type.equalsIgnoreCase("NEW_CONTAINER")) {

				// Received: Container được upload lên từ CỔNG
				// Received Roles: GATE | AUDIT
				// --> Get more data from Server

				DataCenter.getInstance().updateListContainerSessions(this);

			} else if (type.equalsIgnoreCase("EXPORT_CONTAINER")) {
				// Received: Container xuất khỏi Depot ở CỔNG
				// Received Roles: all roles with attached `id`
				// --> Remove Container Session having this id

				DataCenter.getInstance().removeContainerSession(this, id);

			} else if (type.equalsIgnoreCase("NEW_ERROR_LIST")) {
				// Received: AUDIT post new Issue List
				// Received Roles: REPAIR
				// Đối với ROLE == AUDIT, kèm `id` để remove

				User user = com.cloudjay.cjay.util.Session.restore(this)
						.getCurrentUser();

				if (user.getRole() == User.ROLE_AUDITOR) {

					// If role is AUDIT -> remove Container Session having `id`
					DataCenter.getInstance().removeContainerSession(this, id);

				} else {

					// Get more data from Server
					DataCenter.getInstance().updateListContainerSessions(this);
				}

			} else if (type.equalsIgnoreCase("UPDATE_ERROR_LIST")) {

				// Received: Có thông tin thay đổi từ `văn phòng` || tổ sửa chữa
				// thêm lỗi mới
				// Received Roles: REPAIR
				// --> Get more data from Server
				DataCenter.getInstance().updateListContainerSessions(this);

			} else if (type.equalsIgnoreCase("CONTAINER_REPAIRED")) {

				// Received: Sau khi post báo cáo `Sau sửa chữa` từ REPAIR
				// Received Roles: REPAIR with attached `id`
				// --> Remove Container Session having this `id`
				DataCenter.getInstance().removeContainerSession(this, id);

			} else if (type.equalsIgnoreCase("UPDATE_DAMAGE_CODE")) {
				DataCenter.getInstance().updateListDamageCodes(this);

			} else if (type.equalsIgnoreCase("UPDATE_REPAIR_CODE")) {
				DataCenter.getInstance().updateListRepairCodes(this);

			} else if (type.equalsIgnoreCase("UPDATE_COMP_CODE")) {
				DataCenter.getInstance().updateListComponentCodes(this);

			} else if (type.equalsIgnoreCase("UPDATE_OPERATOR_LIST")) {
				DataCenter.getInstance().updateListOperators(this);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Intent previewIntent = new Intent(this, ItemDetailActivity_.class);
		// previewIntent.putExtra("feed_id", extras.getString("feed_id"));
		// PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
		// previewIntent, 0);
		//
		// try {
		// NotificationCompat.Builder mBuilder =
		// new NotificationCompat.Builder(this)
		// .setSmallIcon(R.drawable.logo_small)
		// .setContentTitle(extras.getString("team_name"))
		// .setStyle(new NotificationCompat.BigTextStyle()
		// .bigText(extras.getString("msg")))
		// .setContentText(extras.getString("msg"))
		// .setDefaults(Notification.DEFAULT_SOUND)
		// .setDefaults(Notification.DEFAULT_LIGHTS)
		// .setDefaults(Notification.DEFAULT_VIBRATE);
		//
		// Uri alarmSound =
		// RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		// mBuilder.setSound(alarmSound);
		//
		//
		//
		// mBuilder.setContentIntent(contentIntent);
		// mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
		// }
		// catch (NotFoundException e) {
		// Log.e("GCM", e.getMessage());
		// }
	}

	private void sendNotification(String msg) {
		mNotificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.ic_app)
				.setContentTitle("CJAY NOTICE")
				.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
				.setContentText(msg).setDefaults(Notification.DEFAULT_SOUND)
				.setDefaults(Notification.DEFAULT_LIGHTS)
				.setDefaults(Notification.DEFAULT_VIBRATE);

		Uri alarmSound = RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		mBuilder.setSound(alarmSound);

		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}
}