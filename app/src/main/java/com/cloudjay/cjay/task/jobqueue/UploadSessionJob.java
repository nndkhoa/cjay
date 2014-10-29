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

	public UploadSessionJob(Session session) {
		super(new Params(1).requireNetwork().persist().groupBy(session.getContainerId()));
		this.session = session;
	}

	@Override
	public void onAdded() {

        Context context = App.getInstance().getApplicationContext();
        DataCenter_.getInstance_(context).addUploadingSession(session.getContainerId());
        EventBus.getDefault().post(new UploadStartedEvent(session.getContainerId()));

	}

	@Override
	public void onRun() throws Throwable {

		EventBus.getDefault().post(new UploadingEvent());
		Context context = App.getInstance().getApplicationContext();
		DataCenter_.getInstance_(context).uploadContainerSession(context, session);


	}


	@Override
	protected void onCancel() {

	}

	@Override
	protected boolean shouldReRunOnThrowable(Throwable throwable) {

		// TODO: @thai: Assign upload status of container to ERROR


		EventBus.getDefault().post(new UploadStoppedEvent());
		return true;
	}
}
