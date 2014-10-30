package com.cloudjay.cjay.task.jobqueue;

import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.event.upload.UploadStartedEvent;
import com.cloudjay.cjay.event.upload.UploadStoppedEvent;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.util.enums.UploadType;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import de.greenrobot.event.EventBus;

/**
 * Created by thai on 29/10/2014.
 */
public class UploadAuditItemJob extends Job {
    String containerId;
    AuditItem auditItem;

    @Override
    protected int getRetryLimit() {
        return 2;
    }

    public UploadAuditItemJob(String containerId, AuditItem auditItem) {
        super(new Params(1).requireNetwork().persist().groupBy(containerId));
        this.containerId = containerId;
        this.auditItem = auditItem;
    }

    @Override
    public void onAdded() {

        Context context = App.getInstance().getApplicationContext();
        DataCenter_.getInstance_(context).addUploadSession(containerId);
        EventBus.getDefault().post(new UploadStartedEvent(containerId, UploadType.AUDIT_ITEM));

    }

    @Override
    public void onRun() throws Throwable {
        Context context = App.getInstance().getApplicationContext();

        //Add Log
        DataCenter_.getInstance_(context).addLog(context,containerId, "Bắt đầu thêm lỗi");

        //EventBus.getDefault().post(new UploadingEvent());

        DataCenter_.getInstance_(context).uploadAuditItem(context, containerId, auditItem);

        //Add Log
        DataCenter_.getInstance_(context).addLog(context,containerId, "Khởi tạo thêm lỗi");


    }


    @Override
    protected void onCancel() {
        Context context = App.getInstance().getApplicationContext();
        DataCenter_.getInstance_(context).addLog(context,containerId, "Không thể thêm lỗi");
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        Context context = App.getInstance().getApplicationContext();
        //Add Log
        DataCenter_.getInstance_(context).addLog(context,containerId, "Thêm lỗi bị gián đoạn");

        EventBus.getDefault().post(new UploadStoppedEvent(containerId));
        return true;
    }
}
