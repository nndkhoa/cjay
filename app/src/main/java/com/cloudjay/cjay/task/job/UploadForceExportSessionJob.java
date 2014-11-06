package com.cloudjay.cjay.task.job;

import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.event.upload.UploadStoppedEvent;
import com.cloudjay.cjay.event.upload.UploadedEvent;
import com.cloudjay.cjay.event.upload.UploadingEvent;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.enums.Step;
import com.cloudjay.cjay.util.enums.UploadStatus;
import com.cloudjay.cjay.util.enums.UploadType;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.snappydb.SnappydbException;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;

/**
 * Created by thai on 06/11/2014.
 */
public class UploadForceExportSessionJob extends Job {

	String containerId;

	public UploadForceExportSessionJob(String containerId, boolean b) {
		super(new Params(1).requireNetwork().persist().groupBy(containerId));
		this.containerId = containerId;
	}

	@Override
	public void onAdded() {
		// Add container to collection UPLOAD
		Context context = App.getInstance().getApplicationContext();
		DataCenter dataCenter = DataCenter_.getInstance_(context);

		// Set session upload status to UPLOADING
		// Add session to Collection Upload
		// Change status uploading, currentStep audit, remove from WORKING
		try {

			dataCenter.addLog(context, containerId, "Container được add vào hàng đợi");
			dataCenter.addUploadSession(containerId);

			dataCenter.changeUploadState(context, containerId, UploadStatus.UPLOADING);

			dataCenter.changeSessionLocalStep(context, containerId, Step.EXPORTED);
		} catch (SnappydbException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onRun() throws Throwable {
		Context context = App.getInstance().getApplicationContext();
		DataCenter dataCenter = DataCenter_.getInstance_(context);

		// Bắt đầu quá trình upload
		EventBus.getDefault().post(new UploadingEvent(containerId, UploadType.SESSION));

		dataCenter.addLog(context,containerId,"EXPORT IMMEDIATELY | Bắt đầu quá trình upload");
		dataCenter.forceExportSession(context, containerId);

		// Change upload status to COMPLETE
		DataCenter_.getInstance_(context).changeUploadState(context, containerId, UploadStatus.COMPLETE);

		// Upload thành công
		DataCenter_.getInstance_(context).addLog(context, containerId, "Upload container session thành công");
		EventBus.getDefault().post(new UploadedEvent(containerId));

	}

	@Override
	protected void onCancel() {
		//Change status error
		try {

			Context context = App.getInstance().getApplicationContext();
			DataCenter_.getInstance_(context).addLog(context, containerId, "Upload thất bại");
			DataCenter_.getInstance_(context).changeUploadState(context, containerId, UploadStatus.ERROR);
			EventBus.getDefault().post(new UploadStoppedEvent(containerId));

		} catch (SnappydbException e) {
			e.printStackTrace();
		}

	}

	@Override
	protected boolean shouldReRunOnThrowable(Throwable throwable) {
		if (throwable instanceof RetrofitError) {
			Context context = App.getInstance().getApplicationContext();
			DataCenter_.getInstance_(context).addLog(context, containerId, "Quá trình upload bị gián đoạn");

			//if it is a 4xx error, stop
			RetrofitError retrofitError = (RetrofitError) throwable;
			Logger.Log("Retrofit response: " + retrofitError.getSuccessType().toString());

			return retrofitError.getResponse().getStatus() < 400 || retrofitError.getResponse().getStatus() > 499;
		}

		// Notify upload process is retrying

		return true;
	}
}
