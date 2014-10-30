package com.cloudjay.cjay.task.jobqueue;

import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.event.upload.UploadStartedEvent;
import com.cloudjay.cjay.event.upload.UploadStoppedEvent;
import com.cloudjay.cjay.util.enums.UploadType;
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

		// Image is uploaded in background, but we still need to notify Upload Fragment
		// in case container session upload status is > UPLOADING.
		// It will notify fragment upload to update UI
		EventBus.getDefault().post(new UploadStartedEvent(containerId, UploadType.IMAGE));
	}

	@Override
	public void onRun() throws Throwable {

//		// Notify to fragment upload that image is being uploaded.
//		EventBus.getDefault().post(new UploadingEvent(containerId, UploadType.IMAGE));

		// Call data center to upload image
		Context context = App.getInstance().getApplicationContext();
		DataCenter_.getInstance_(context).uploadImage(context, uri, imageName, containerId);
	}

	@Override
	protected void onCancel() {

		// Job has exceeded retry attempts or shouldReRunOnThrowable() has returned false.
		// TODO: Set image upload Status to ERROR and notify to Upload Fragment

		Context context = App.getInstance().getApplicationContext();
		EventBus.getDefault().post(new UploadStoppedEvent(containerId));
		DataCenter_.getInstance_(context).addLog(context, containerId, "Không thể tải lên hình: " + imageName);
	}

	@Override
	protected boolean shouldReRunOnThrowable(Throwable throwable) {

		if (getCurrentRunCount() >= getRetryLimit()) {
			return false;
		}

		return true;
	}
}
