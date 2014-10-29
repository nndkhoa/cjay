package com.cloudjay.cjay.task.jobqueue;

import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.event.upload.UploadStartedEvent;
import com.cloudjay.cjay.event.upload.UploadStoppedEvent;
import com.cloudjay.cjay.event.upload.UploadedEvent;
import com.cloudjay.cjay.event.upload.UploadingEvent;
import com.cloudjay.cjay.model.Session;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import de.greenrobot.event.EventBus;

public class UploadSessionJob extends Job {
	Session session;

	@Override
	protected int getRetryLimit() {
		return 2;
	}

	public UploadSessionJob(Session session) {
		super(new Params(1).requireNetwork().persist().groupBy(session.getContainerId()));
		this.session = session;
	}

	@Override
	public void onAdded() {

        Context context = App.getInstance().getApplicationContext();
        DataCenter_.getInstance_(context).addUploadSession(session.getContainerId());
        EventBus.getDefault().post(new UploadStartedEvent(session.getContainerId()));

	}

	@Override
	public void onRun() throws Throwable {
        Context context = App.getInstance().getApplicationContext();

        //Add Log
        DataCenter_.getInstance_(context).addLog(context,session.getContainerId(), "Bắt đầu khởi tạo");

		EventBus.getDefault().post(new UploadingEvent());

		DataCenter_.getInstance_(context).uploadSession(context, session);

        //Add Log
        DataCenter_.getInstance_(context).addLog(context,session.getContainerId(), "Khởi tạo hoàn tất");


	}


	@Override
	protected void onCancel() {
        Context context = App.getInstance().getApplicationContext();
        DataCenter_.getInstance_(context).addLog(context,session.getContainerId(), "Không thể khởi tạo");
	}

	@Override
	protected boolean shouldReRunOnThrowable(Throwable throwable) {
        Context context = App.getInstance().getApplicationContext();
        //Add Log
        DataCenter_.getInstance_(context).addLog(context,session.getContainerId(), "Khởi tạo bị gián đoạn");

		EventBus.getDefault().post(new UploadStoppedEvent());
		return true;
	}
}
