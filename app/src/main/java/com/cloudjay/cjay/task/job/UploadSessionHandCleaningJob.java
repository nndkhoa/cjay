package com.cloudjay.cjay.task.job;

import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.event.upload.UploadStartedEvent;
import com.cloudjay.cjay.util.enums.Step;
import com.cloudjay.cjay.util.enums.UploadStatus;
import com.cloudjay.cjay.util.enums.UploadType;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.snappydb.SnappydbException;

import de.greenrobot.event.EventBus;

public class UploadSessionHandCleaningJob extends Job {
	String containerId;

	public UploadSessionHandCleaningJob(String containerId) {
		super(new Params(1).requireNetwork().persist().groupBy(containerId));
		this.containerId = containerId;
	}

	@Override
	public void onAdded() {

		// Add container to collection UPLOAD
		Context context = App.getInstance().getApplicationContext();
		DataCenter_.getInstance_(context).addUploadSession(containerId);

		//Change status uploadding, step audit, remove from WORKING
		try {
			DataCenter_.getInstance_(context).changeUploadState(context,containerId, UploadStatus.UPLOADING);
			DataCenter_.getInstance_(context).changeStepSession(context,containerId, Step.EXPORT);
			DataCenter_.getInstance_(context).removeWorkingSession(context,containerId );
		} catch (SnappydbException e) {
			e.printStackTrace();
		}

		EventBus.getDefault().post(new UploadStartedEvent(containerId, UploadType.SESSION));

	}

	@Override
	public void onRun() throws Throwable {
		Context context = App.getInstance().getApplicationContext();

		//Add Log
		DataCenter_.getInstance_(context).addLog(context, containerId, "Bắt đầu thiết lập là container vệ sinh quét");
//		EventBus.getDefault().post(new UploadingEvent());
		DataCenter_.getInstance_(context).setHandCleaningSession(context, containerId);

		//Add Log
		DataCenter_.getInstance_(context).addLog(context, containerId, "Hoàn tất thiết lập là container vệ sinh quét");

	}

	@Override
	protected void onCancel() {
		Context context = App.getInstance().getApplicationContext();
		//Add Log
		DataCenter_.getInstance_(context).addLog(context, containerId, "Không thể thiết lập là container vệ sinh quét");

	}

	@Override
	protected boolean shouldReRunOnThrowable(Throwable throwable) {
		Context context = App.getInstance().getApplicationContext();
		//Add Log
		DataCenter_.getInstance_(context).addLog(context, containerId, "Thiết lập là container vệ sinh quét bị gián đoạn");
		return false;
	}
}
