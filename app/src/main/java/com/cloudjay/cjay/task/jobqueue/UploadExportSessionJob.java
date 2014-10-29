package com.cloudjay.cjay.task.jobqueue;

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
public class UploadExportSessionJob extends Job {
	Session session;

	@Override
	protected int getRetryLimit() {
		return 2;
	}

	public UploadExportSessionJob(Session session) {
		super(new Params(1).requireNetwork().persist().groupBy(session.getContainerId()));
		this.session = session;
	}

	@Override
	public void onAdded() {

		Context context = App.getInstance().getApplicationContext();
		DataCenter_.getInstance_(context).addUploadSession(session.getContainerId());
		EventBus.getDefault().post(new UploadStartedEvent(session.getContainerId(), UploadType.SESSION));

	}

	@Override
	public void onRun() throws Throwable {
		Context context = App.getInstance().getApplicationContext();

		//Add Log
		DataCenter_.getInstance_(context).addLog(context, session.getContainerId(), "Bắt đầu xuất");

//        EventBus.getDefault().post(new UploadingEvent());

		DataCenter_.getInstance_(context).uploadExportSession(context, session);

		//Add Log
		DataCenter_.getInstance_(context).addLog(context, session.getContainerId(), "Xuất hoàn tất");


	}


	@Override
	protected void onCancel() {
		Context context = App.getInstance().getApplicationContext();
		DataCenter_.getInstance_(context).addLog(context, session.getContainerId(), "Không thể xuất");
	}

	@Override
	protected boolean shouldReRunOnThrowable(Throwable throwable) {
		Context context = App.getInstance().getApplicationContext();
		//Add Log
		DataCenter_.getInstance_(context).addLog(context, session.getContainerId(), "Xuất bị gián đoạn");

		EventBus.getDefault().post(new UploadStoppedEvent(session.getContainerId()));
		return true;
	}
}
