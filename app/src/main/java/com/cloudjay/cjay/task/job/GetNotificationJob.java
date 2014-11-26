package com.cloudjay.cjay.task.job;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.CJayConstant;
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
        //Add LOG
        Context context = App.getInstance().getApplicationContext();
        DataCenter_.getInstance_(context).addLog(context, objectType, objectId + " |R " + messageId, CJayConstant.PREFIX_NOTIFI_LOG);

    }

    @Override
    public void onRun() throws Throwable {

        Logger.Log("Receive notification from server");
        Context context = App.getInstance().getApplicationContext();

        // Get data from notification
        if (objectType.equals("Container")) {
            DataCenter_.getInstance_(context).getSessionAsyncById(context, objectId, 0);

        } else if (objectType.equals("AuditItem")) {
            DataCenter_.getInstance_(context).getAuditItemAsyncById(context, objectId);

        } else if (objectType.equals("Damage")) {
            DataCenter_.getInstance_(context).getDamageCodeAsyncById(context, objectId);

        } else if (objectType.equals("Repair")) {
            DataCenter_.getInstance_(context).getRepairCodeAsyncById(context, objectId);

        } else if (objectType.equals("Component")) {
            DataCenter_.getInstance_(context).getComponentCodeAsyncById(context, objectId);

        } else if (objectType.equals("Operator")) {
            DataCenter_.getInstance_(context).getOperatorAsyncById(context, objectId);

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
        DataCenter_.getInstance_(context).addLog(context, objectType, objectId + " |C " + messageId, CJayConstant.PREFIX_NOTIFI_LOG);

    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }
}
