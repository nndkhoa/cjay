package com.cloudjay.cjay.task.jobqueue;

import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.event.upload.UploadStartedEvent;
import com.cloudjay.cjay.event.upload.UploadingEvent;
import com.cloudjay.cjay.model.Session;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import de.greenrobot.event.EventBus;

public class UploadSessionHandCleaningJob extends Job {
	Session session;

	public UploadSessionHandCleaningJob(Session session) {
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
		DataCenter_.getInstance_(context).addLog(context, session.getContainerId(), "Bắt đầu thiết lập là container vệ sinh quét");
		EventBus.getDefault().post(new UploadingEvent());
		DataCenter_.getInstance_(context).setHandCleaningSession(context, session.getContainerId());

		//Add Log
		DataCenter_.getInstance_(context).addLog(context, session.getContainerId(), "Hoàn tất thiết lập là container vệ sinh quét");

	}

	@Override
	protected void onCancel() {
		Context context = App.getInstance().getApplicationContext();
		//Add Log
		DataCenter_.getInstance_(context).addLog(context, session.getContainerId(), "Không thể thiết lập là container vệ sinh quét");

	}

	@Override
	protected boolean shouldReRunOnThrowable(Throwable throwable) {
		Context context = App.getInstance().getApplicationContext();
		//Add Log
		DataCenter_.getInstance_(context).addLog(context, session.getContainerId(), "Thiết lập là container vệ sinh quét bị gián đoạn");
		return false;
	}
}
