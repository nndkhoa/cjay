package com.cloudjay.cjay.task.jobqueue;

import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.event.upload.UploadStartedEvent;
import com.cloudjay.cjay.event.upload.UploadStoppedEvent;
import com.cloudjay.cjay.event.upload.UploadingEvent;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.enums.Step;
import com.cloudjay.cjay.util.enums.UploadStatus;
import com.cloudjay.cjay.util.enums.UploadType;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.snappydb.SnappydbException;

import de.greenrobot.event.EventBus;

public class UploadCompleteRepairJob extends Job {
	Session session;
	String containerId;

	@Override
	protected int getRetryLimit() {
		return 2;
	}

	public UploadCompleteRepairJob(String containerId) {
		super(new Params(1).requireNetwork().persist().groupBy(containerId));
		this.containerId = containerId;
	}

	@Override
	public void onAdded() {

		// Add container to collection UPLOAD
		Context context = App.getInstance().getApplicationContext();
		DataCenter_.getInstance_(context).addUploadSession(containerId);

		//Change status uploadding, step export, remove from WORKING
		try {
			DataCenter_.getInstance_(context).changeUploadState(context, containerId, UploadStatus.UPLOADING);
			DataCenter_.getInstance_(context).changeStepSession(context, containerId, Step.EXPORT);
			DataCenter_.getInstance_(context).removeWorkingSession(context, containerId);
		} catch (SnappydbException e) {
			e.printStackTrace();
		}
		EventBus.getDefault().post(new UploadStartedEvent(containerId, UploadType.AUDIT_ITEM));

	}

	@Override
	public void onRun() throws Throwable {
		Context context = App.getInstance().getApplicationContext();

		//Add Log
		DataCenter_.getInstance_(context).addLog(context, containerId, "Bắt đầu tải lên đã sữa");

//		EventBus.getDefault().post(new UploadingEvent());

		DataCenter_.getInstance_(context).uploadCompleteRepairSession(context, containerId);

		//Add Log
		DataCenter_.getInstance_(context).addLog(context, session.getContainerId(), "Tải lên đã sữa hoàn tất");
	}


	@Override
	protected void onCancel() {
		Context context = App.getInstance().getApplicationContext();
		DataCenter_.getInstance_(context).addLog(context, session.getContainerId(), "Không thể tải lên đã sữa");
	}

	@Override
	protected boolean shouldReRunOnThrowable(Throwable throwable) {
		Context context = App.getInstance().getApplicationContext();
		//Add Log
		DataCenter_.getInstance_(context).addLog(context, session.getContainerId(), "Tải lên giám định bị đã sữa");

		EventBus.getDefault().post(new UploadStoppedEvent(containerId));
		return true;
	}
}
