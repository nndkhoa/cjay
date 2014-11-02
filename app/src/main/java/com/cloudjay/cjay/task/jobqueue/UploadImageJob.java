package com.cloudjay.cjay.task.jobqueue;

import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.event.upload.UploadStartedEvent;
import com.cloudjay.cjay.event.upload.UploadStoppedEvent;
import com.cloudjay.cjay.event.upload.UploadedEvent;
import com.cloudjay.cjay.event.upload.UploadingEvent;
import com.cloudjay.cjay.util.enums.ImageType;
import com.cloudjay.cjay.util.enums.UploadType;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.snappydb.SnappydbException;

import de.greenrobot.event.EventBus;

public class UploadImageJob extends Job {
	String containerId;
	String uri;
	String imageName;
	ImageType imageType;

	@Override
	protected int getRetryLimit() {
		return 2;
	}

	public UploadImageJob(String uri, String imageName, String containerId, ImageType imageType) {
		super(new Params(1).requireNetwork().persist().groupBy(containerId).setPersistent(true));
		this.containerId = containerId;
		this.uri = uri;
		this.imageName = imageName;
		this.imageType = imageType;
	}

	@Override
	public void onAdded() {
		try {

			// Set ít status to UPLOADING
			Context context = App.getInstance().getApplicationContext();
			DataCenter_.getInstance_(context).setUploadStatus(context, containerId, imageName, imageType, UploadStatus.UPLOADING);

			// Image is uploaded in background, but we still need to notify Upload Fragment
			// in case container session upload status is > UPLOADING.
			// It will notify fragment upload to update UI
			EventBus.getDefault().post(new ItemEnqueueEvent(containerId, UploadType.IMAGE));
		} catch (SnappydbException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onRun() throws Throwable {

		// Notify to fragment upload that image is being uploaded.
		EventBus.getDefault().post(new UploadingEvent(containerId, UploadType.IMAGE));

		// Call data center to upload image then set upload status to COMPLETE
		Context context = App.getInstance().getApplicationContext();
		DataCenter_.getInstance_(context).uploadImage(context, uri, imageName, containerId, imageType);
		EventBus.getDefault().post(new UploadedEvent(containerId));
	}

	@Override
	protected boolean shouldReRunOnThrowable(Throwable throwable) {
		return true;
	}

	/**
	 * // Job has exceeded retry attempts or shouldReRunOnThrowable() has returned false.
	 */
	@Override
	protected void onCancel() {

		return true;
	}
}
