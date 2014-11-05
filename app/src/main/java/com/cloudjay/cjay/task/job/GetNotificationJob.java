package com.cloudjay.cjay.task.job;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.Logger;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

public class GetNotificationJob extends Job {
	String channel;
	String messageId;
	String objectType;
	long objectId;


	public GetNotificationJob(String channel, String messageId, String objectType, long objectId) {
		super(new Params(1).persist().requireNetwork());
		this.channel = channel;
		this.messageId = messageId;
		this.objectType = objectType;
		this.objectId = objectId;
	}

	@Override
	public void onAdded() {

	}

	@Override
	public void onRun() throws Throwable {

		Context context = App.getInstance().getApplicationContext();

		// Get data from notification
		if (objectType.equals("Container")) {
			Session session = DataCenter_.getInstance_(context).getSessionById(context, objectId);
			pushNotification(session);

		} else if (objectType.equals("AuditItem")) {
			Session session = DataCenter_.getInstance_(context).getAuditItemById(context, objectId);
			pushNotification(session);

		} else if (objectType.equals("Damage")) {
			DataCenter_.getInstance_(context).getDamageCodeById(context, objectId);

		} else if (objectType.equals("Repair")) {
			DataCenter_.getInstance_(context).getRepairCodeById(context, objectId);

		} else if (objectType.equals("Component")) {
			DataCenter_.getInstance_(context).getComponentCodeById(context, objectId);

		} else if (objectType.equals("Operator")) {
			DataCenter_.getInstance_(context).getOperatorById(context, objectId);

		} else {
			Logger.e("Cannot parse notification");
		}

		// Notify to server that message was received.
		DataCenter_.getInstance_(context).gotMessage(context, channel, messageId);
	}

	public void pushNotification(Session session) {
		Context context = App.getInstance().getApplicationContext();

		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification.Builder(context).setContentTitle("Đã cập nhật thông tin")
				.setContentText(session.getContainerId())
				.setSmallIcon(R.drawable.ic_app_small)
				.setAutoCancel(true)
				.setDefaults(Notification.DEFAULT_SOUND).build();

		notificationManager.notify(1, notification);
	}

	@Override
	protected void onCancel() {
		//Add LOG
		Context context = App.getInstance().getApplicationContext();
		DataCenter_.getInstance_(context).addLog(context, objectType, objectId + " | " + messageId);
	}

	@Override
	protected boolean shouldReRunOnThrowable(Throwable throwable) {
		return false;
	}
}
