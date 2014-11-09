package com.cloudjay.cjay.task.job;

import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.event.upload.UploadStartedEvent;
import com.cloudjay.cjay.event.upload.UploadStoppedEvent;
import com.cloudjay.cjay.event.upload.UploadSucceededEvent;
import com.cloudjay.cjay.event.upload.UploadingEvent;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.Utils;
import com.cloudjay.cjay.util.enums.ImageType;
import com.cloudjay.cjay.util.enums.UploadStatus;
import com.cloudjay.cjay.util.enums.UploadType;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;

public class UploadImageJob extends Job {
	String containerId;
	String uri;
	String imageName;
	ImageType imageType;

	@Override
	public int getRetryLimit() {
		return CJayConstant.RETRY_THRESHOLD;
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

		// Image is uploaded in background, but we still need to notify Upload Fragment
		// in case container session upload status is > UPLOADING.
		// It will notify fragment upload to update UI
		Context context = App.getInstance().getApplicationContext();
		DataCenter_.getInstance_(context).changeImageUploadStatus(context, containerId, imageName, imageType, UploadStatus.UPLOADING);
		EventBus.getDefault().post(new UploadStartedEvent(containerId, UploadType.IMAGE));
	}

	@Override
	public void onRun() throws Throwable {

		// Notify to fragment upload that image is being uploaded.
		EventBus.getDefault().post(new UploadingEvent(containerId, UploadType.IMAGE));
		Logger.Log("Upload img: " + Utils.subString(imageName));

		// Call data center to upload image
		Context context = App.getInstance().getApplicationContext();
		DataCenter_.getInstance_(context).uploadImage(uri, imageName);

		// Change image status to COMPLETE
		DataCenter_.getInstance_(context).changeImageUploadStatus(context, containerId, imageName, imageType, UploadStatus.COMPLETE);
		EventBus.getDefault().post(new UploadSucceededEvent(containerId, UploadType.IMAGE));
	}

	@Override
	protected void onCancel() {

		// Job has exceeded retry attempts or shouldReRunOnThrowable() has returned false.
		// Set image upload Status is ERROR and notify to Upload Fragment
		Context context = App.getInstance().getApplicationContext();
		DataCenter_.getInstance_(context).changeImageUploadStatus(context, containerId, imageName, imageType, UploadStatus.ERROR);
		EventBus.getDefault().post(new UploadStoppedEvent(containerId));
		DataCenter_.getInstance_(context).addLog(context, containerId, "Không thể tải lên hình: " + imageName);

	}

	@Override
	protected boolean shouldReRunOnThrowable(Throwable throwable) {

		//if it is a 4xx error, stop
		if (throwable instanceof RetrofitError) {
			RetrofitError retrofitError = (RetrofitError) throwable;
			Logger.Log("Retrofit response: " + retrofitError.getSuccessType().toString());

			return retrofitError.getResponse().getStatus() < 400 || retrofitError.getResponse().getStatus() > 499;
		}

		return true;
	}
}
