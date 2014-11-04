package com.cloudjay.cjay.task.job;

import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
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
			DataCenter_.getInstance_(context).getSessionById(context, objectId);

		} else if (objectType.equals("AuditItem")) {
			DataCenter_.getInstance_(context).getAuditItemById(context, objectId);

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

	@Override
	protected void onCancel() {
		//Add LOG
		Context context = App.getInstance().getApplicationContext();
		DataCenter_.getInstance_(context).addLog(context,objectType, objectId+" | "+messageId);
	}

	@Override
	protected boolean shouldReRunOnThrowable(Throwable throwable) {
		return false;
	}
}
