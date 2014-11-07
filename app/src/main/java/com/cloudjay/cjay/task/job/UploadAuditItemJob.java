package com.cloudjay.cjay.task.job;

import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.event.upload.UploadStartedEvent;
import com.cloudjay.cjay.event.upload.UploadStoppedEvent;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.enums.UploadType;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import de.greenrobot.event.EventBus;

/**
 * Created by thai on 29/10/2014.
 */
public class UploadAuditItemJob extends Job {
	String containerId;
	String auditItemUUID;

    @Override
    public int getRetryLimit() {
        return 2;
    }


	public UploadAuditItemJob(String containerId, String auditItemUUID) {
		super(new Params(1).requireNetwork().persist().groupBy(containerId));
		this.containerId = containerId;
		this.auditItemUUID = auditItemUUID;
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
		DataCenter_.getInstance_(context).addLog(context, containerId, "Bắt đầu thêm lỗi");

//        EventBus.getDefault().post(new UploadingEvent());

		DataCenter_.getInstance_(context).uploadAuditItem(context, containerId, auditItemUUID);
		//Add Log
		DataCenter_.getInstance_(context).addLog(context, containerId, "Khởi tạo thêm lỗi");

		Session session = DataCenter_.getInstance_(context).getSession(context, containerId);


	}


	@Override
	protected void onCancel() {
		Context context = App.getInstance().getApplicationContext();
		DataCenter_.getInstance_(context).addLog(context, containerId, "Không thể thêm lỗi");
	}

	@Override
	protected boolean shouldReRunOnThrowable(Throwable throwable) {
		Context context = App.getInstance().getApplicationContext();
		//Add Log
		DataCenter_.getInstance_(context).addLog(context, containerId, "Thêm lỗi bị gián đoạn");

		EventBus.getDefault().post(new UploadStoppedEvent(containerId));
		return true;
	}
}
