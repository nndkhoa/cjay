package com.cloudjay.cjay.task.jobqueue;

import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.event.upload.UploadStartedEvent;
import com.cloudjay.cjay.event.upload.UploadStoppedEvent;
import com.cloudjay.cjay.event.upload.UploadingEvent;
import com.cloudjay.cjay.model.Session;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import de.greenrobot.event.EventBus;

/**
 * Created by thai on 29/10/2014.
 */
public class UploadCompleteAuditJob extends Job {
    Session session;
    String containerId;

    @Override
    protected int getRetryLimit() {
        return 2;
    }

    public UploadCompleteAuditJob(String containerId) {
        super(new Params(1).requireNetwork().persist().groupBy(containerId));
        this.containerId = containerId;
    }

    @Override
    public void onAdded() {

        Context context = App.getInstance().getApplicationContext();
        DataCenter_.getInstance_(context).addUploadSession(containerId);
        EventBus.getDefault().post(new UploadStartedEvent(containerId));

    }

    @Override
    public void onRun() throws Throwable {
        Context context = App.getInstance().getApplicationContext();

        //Add Log
        DataCenter_.getInstance_(context).addLog(context,containerId, "Bắt đầu tải lên giám định");

        EventBus.getDefault().post(new UploadingEvent());

        DataCenter_.getInstance_(context).uploadCompleteAuditSession(context, containerId);

        //Add Log
        DataCenter_.getInstance_(context).addLog(context,session.getContainerId(), "Tải lên giám định hoàn tất");


    }


    @Override
    protected void onCancel() {
        Context context = App.getInstance().getApplicationContext();
        DataCenter_.getInstance_(context).addLog(context,session.getContainerId(), "Không thể tải lên giám định");
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        Context context = App.getInstance().getApplicationContext();
        //Add Log
        DataCenter_.getInstance_(context).addLog(context,session.getContainerId(), "Tải lên giám định bị gián đoạn");

        EventBus.getDefault().post(new UploadStoppedEvent());
        return true;
    }
}
