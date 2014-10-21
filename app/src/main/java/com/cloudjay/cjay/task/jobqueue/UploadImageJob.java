package com.cloudjay.cjay.task.jobqueue;

import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.event.StartUpLoadEvent;
import com.cloudjay.cjay.event.UploadStoppedEvent;
import com.cloudjay.cjay.event.UploadingEvent;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.snappydb.SnappydbException;

import de.greenrobot.event.EventBus;

public class UploadImageJob extends Job {
	String containerId;
	String uri;
	String imageName;

	public UploadImageJob(String uri, String imageName, String containerId) {

		super(new Params(2).requireNetwork().persist().groupBy(containerId));
		this.containerId = containerId;
		this.uri = uri;
		this.imageName = imageName;
	}

	@Override
	public void onAdded() {
		try {

			Context context = App.getInstance().getApplicationContext();
			DataCenter_.getInstance_(context).addUploadingSession(containerId);
			EventBus.getDefault().post(new StartUpLoadEvent(containerId));

		} catch (SnappydbException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onRun() throws Throwable {
		EventBus.getDefault().post(new UploadingEvent());

		Context context = App.getInstance().getApplicationContext();
		DataCenter_.getInstance_(context).uploadImage(context, uri, imageName, containerId);
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
