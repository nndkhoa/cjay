package com.cloudjay.cjay.service;

import org.androidannotations.annotations.EIntentService;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.Trace;

import android.R.integer;
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
import android.support.v4.content.WakefulBroadcastReceiver;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.cloudjay.cjay.CJayApplication;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.network.CJayClient;
import com.cloudjay.cjay.util.CJaySession;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.InvokeType;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.NullSessionException;
import com.cloudjay.cjay.util.UserRole;
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
			Bundle extras = intent.getExtras();
			GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

			// The getMessageType() intent parameter must be the intent you
			// received
			// in your BroadcastReceiver.
			String messageType = gcm.getMessageType(intent);

			if (!extras.isEmpty()) { // has effect of unparcelling Bundle
				Logger.Log("Message Type: " + messageType);

				/*
				 * Filter messages based on message type. Since it is likely
				 * that GCM will be extended in the future with new message
				 * types, just ignore any message types you're not interested
				 * in, or that you don't recognize.
				 */

				if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {

					Logger.Log("Send error: " + extras.toString());
					sendNotification("Send error: " + extras.toString());

				} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
					Logger.Log("Deleted messages on server: " + extras.toString());

					sendNotification("Deleted messages on server: " + extras.toString());

					// If it's a regular GCM message, do some work.
				} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {

					Logger.Log("Received: " + extras.toString());
					sendNotification(extras);

				} else {
					//
					Logger.Log("Last case - " + extras.toString());
					Logger.Log("MessageType: " + messageType);

					String registrationId = extras.getString("registration_id");

					int id = -1;
					try {
						id = Integer.parseInt(extras.getString("id"));
					} catch (Exception e) {

					}

					String msg = extras.getString("msg");
					String type = extras.getString("type");

					Logger.Log("Notification got Type = " + type + " | MSG = " + msg + " | Id = "
							+ Integer.toString(id));

					if (!TextUtils.isEmpty(registrationId)) {
						Logger.Log("Registration Id: " + registrationId);

					} else {

						// Alert cannot register GCM device
						Logger.Log("Cannot register GCM device. Please log out and try again.");
						DataCenter.getDatabaseHelper(getApplicationContext())
									.addUsageLog("Cannot register #GCM device. Please log out and try again.");

						Toast.makeText(this, "Server đang gặp sự cố.\nHãy đăng xuất và thử lại sau 5 phút nữa.",
										Toast.LENGTH_LONG).show();

						// TODO: Log user out.
						// CJayApplication.logOutInstantly(getApplicationContext());
					}
				}

			} else {
				Logger.Log("Extra is Empty");
			}

			// Release the wake lock provided by the WakefulBroadcastReceiver.
			WakefulBroadcastReceiver.completeWakefulIntent(intent);

		} catch (Exception e) {
			e.printStackTrace();
			WakefulBroadcastReceiver.completeWakefulIntent(intent);
		}
	}

	// Put the message into a notification and post it.
	// This is just one simple example of what you might choose to do with
	// a GCM message.
	@Trace(level = Log.INFO)
	void sendNotification(Bundle extras) {

		try {
			mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

			// int userRole = 0;
			UserRole userRole = UserRole.NONE;

			try {
				userRole = UserRole.values()[CJaySession.restore(this).getUserRole()];
			} catch (Exception e) {

				e.printStackTrace();

				DataCenter.getDatabaseHelper(getApplicationContext()).addUsageLog("NullSessionException");

				Toast.makeText(getApplicationContext(), "Tài khoản có vấn đề. Xin hãy đăng nhập lại.",
								Toast.LENGTH_LONG).show();

				CJayApplication.logOutInstantly(getApplicationContext());
				onDestroy();
				return;
			}

			int id = -1;
			try {
				id = Integer.parseInt(extras.getString("id"));
			} catch (Exception e) {

			}

			String type = extras.getString("type");
			Logger.Log("Notification got Type = " + type + " | Id = " + Integer.toString(id));

			if (type.equalsIgnoreCase("NEW_CONTAINER")) {

				// Received: Container được upload lên từ CỔNG
				// Received Roles: GATE | AUDIT
				// --> Get more data from Server

				DataCenter.getInstance().updateListContainerSessions(this, CJayClient.REQUEST_TYPE_MODIFIED,
																		InvokeType.NOTIFICATION);

			} else if (type.equalsIgnoreCase("NEW_TEMP_CONTAINER")) {

				Logger.Log("Received notification: NEW_TEMP_CONTAINER");
				DataCenter.getInstance().updateListContainerSessions(this, CJayClient.REQUEST_TYPE_CREATED,
																		InvokeType.NOTIFICATION);

			} else if (type.equalsIgnoreCase("EXPORT_CONTAINER")) {
				// Received: Container xuất khỏi Depot ở CỔNG
				// Received Roles: all roles with attached `id`
				// --> Remove Container Session having this id

				DataCenter.getInstance().removeContainerSession(this, id);

			} else if (type.equalsIgnoreCase("UPDATE_CONTAINER_STATUS")) {

				int status = -1;
				status = Integer.parseInt(extras.getString("status"));

				if (id == -1 || status == -1) {
					Logger.e("Cannot not update container status");
				} else {

					Logger.Log("Id: " + id + " | Status: " + status);
					DataCenter.getInstance().updateContainerStatus(this, id, status);
				}

			} else if (type.equalsIgnoreCase("NEW_ERROR_LIST")) {
				// Received: AUDIT post new Issue List
				// Received Roles: REPAIR, (new) GATE
				// Đối với ROLE == AUDIT, kèm `id` để remove
				// use MODIFIED_AFTER

				if (userRole == UserRole.AUDITOR) {

					// If role is AUDIT -> remove Container Session having `id`
					DataCenter.getInstance().removeContainerSession(this, id);

				} else {

					Logger.Log("NEW_ERROR_LIST | Other roles");
					// Get more data from Server
					//
					DataCenter.getInstance().updateListContainerSessions(this, CJayClient.REQUEST_TYPE_MODIFIED,
																			InvokeType.NOTIFICATION);
				}

			} else if (type.equalsIgnoreCase("UPDATE_ERROR_LIST")) {

				// Received: Có thông tin thay đổi từ `văn phòng` || tổ sửa chữa
				// thêm lỗi mới
				// Received Roles: REPAIR
				// --> Get more data from Server
				// use MODIFIED_AFTER

				if (userRole == UserRole.REPAIR_STAFF && userRole == UserRole.GATE_KEEPER) {
					DataCenter.getInstance().updateListContainerSessions(this, CJayClient.REQUEST_TYPE_MODIFIED,
																			InvokeType.NOTIFICATION);
				}

			} else if (type.equalsIgnoreCase("CONTAINER_REPAIRED")) {

				// Received: Sau khi post báo cáo `Sau sửa chữa` từ REPAIR
				// Received Roles: REPAIR with attached `id`
				// --> Remove Container Session having this `id`

				if (userRole == UserRole.REPAIR_STAFF || userRole == UserRole.AUDITOR) {
					DataCenter.getInstance().removeContainerSession(this, id);
				} else {
					DataCenter.getInstance().updateListContainerSessions(this, CJayClient.REQUEST_TYPE_MODIFIED,
																			InvokeType.NOTIFICATION);
				}

			} else if (type.equalsIgnoreCase("UPDATE_DAMAGE_CODE")) {
				DataCenter.getInstance().updateListDamageCodes(this);

			} else if (type.equalsIgnoreCase("UPDATE_REPAIR_CODE")) {
				DataCenter.getInstance().updateListRepairCodes(this);

			} else if (type.equalsIgnoreCase("UPDATE_COMP_CODE")) {
				DataCenter.getInstance().updateListComponentCodes(this);

			} else if (type.equalsIgnoreCase("UPDATE_OPERATOR_LIST")) {
				DataCenter.getInstance().updateListOperators(this);

			} else if (type.equalsIgnoreCase("USER_INFO_UPDATED")) {
				User user = com.cloudjay.cjay.util.CJaySession.restore(this).getCurrentUser();
				DataCenter.getInstance().saveCredential(this, user.getAccessToken());
			}
		} catch (NullSessionException e) {

			CJayApplication.logOutInstantly(getApplicationContext());
			onDestroy();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sendNotification(String msg) {
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.ic_app)
																					.setContentTitle("CJAY NOTICE")
																					.setStyle(	new NotificationCompat.BigTextStyle().bigText(msg))
																					.setContentText(msg)
																					.setDefaults(	Notification.DEFAULT_SOUND)
																					.setDefaults(	Notification.DEFAULT_LIGHTS)
																					.setDefaults(	Notification.DEFAULT_VIBRATE);

		Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		mBuilder.setSound(alarmSound);

		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}
}