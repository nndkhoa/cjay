package com.cloudjay.cjay.task.jobqueue;

import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.event.upload.UploadStartedEvent;
import com.cloudjay.cjay.event.upload.UploadStoppedEvent;
import com.cloudjay.cjay.event.upload.UploadingEvent;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import de.greenrobot.event.EventBus;

public class UploadImageJob extends Job {
	String containerId;
	String uri;
	String imageName;

	@Override
	protected int getRetryLimit() {
		return 5;
	}

	public UploadImageJob(String uri, String imageName, String containerId) {
		super(new Params(2).requireNetwork().persist().groupBy(containerId).setPersistent(true));
		this.containerId = containerId;
		this.uri = uri;
		this.imageName = imageName;
	}

	@Override
	public void onAdded() {

	}

	@Override
	public void onRun() throws Throwable {

		EventBus.getDefault().post(new UploadingEvent());
		Context context = App.getInstance().getApplicationContext();
		DataCenter_.getInstance_(context).uploadImage(context, uri, imageName, containerId);
	}

	@Override
	protected void onCancel() {
        Context context = App.getInstance().getApplicationContext();
        DataCenter_.getInstance_(context).addLog(context,containerId, "Không thể tải lên hình: "+imageName);
	}

	@Override
	protected boolean shouldReRunOnThrowable(Throwable throwable) {
		EventBus.getDefault().post(new UploadStoppedEvent());
		return true;
	}
}
