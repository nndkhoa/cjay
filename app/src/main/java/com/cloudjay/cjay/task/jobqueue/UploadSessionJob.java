package com.cloudjay.cjay.task.jobqueue;

import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter_;
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

	}

	@Override
	public void onRun() throws Throwable {

		// Notify container is being uploaded
		EventBus.getDefault().post(new UploadingEvent());

		Context context = App.getInstance().getApplicationContext();
		DataCenter_.getInstance_(context).uploadSession(context, session);

		// Notify container was uploaded
		EventBus.getDefault().post(new UploadedEvent(session.getContainerId()));
	}


	@Override
	protected void onCancel() {

	}

	@Override
	protected boolean shouldReRunOnThrowable(Throwable throwable) {

		EventBus.getDefault().post(new UploadStoppedEvent());
		return true;
	}
}
