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

public class UploadSessionJob extends Job {

	String containerId;

	/**
	 * Dùng để phân biệt xem có cần clear Working hay không?
	 */
	int type;

	@Override
	protected int getRetryLimit() {
		return 2;
	}

	public UploadSessionJob(String containerId) {
		super(new Params(1).requireNetwork().persist().groupBy(containerId));
		this.containerId = containerId;
	}

	@Override
	public void onAdded() {

		// Add container to collection UPLOAD
		Context context = App.getInstance().getApplicationContext();
		DataCenter_.getInstance_(context).addUploadSession(containerId);
		EventBus.getDefault().post(new UploadStartedEvent(containerId, UploadType.SESSION));
	}

	@Override
	public void onRun() throws Throwable {
		Context context = App.getInstance().getApplicationContext();

		//Add Log
		DataCenter_.getInstance_(context).addLog(context, containerId, "Bắt đầu khởi tạo");

//		EventBus.getDefault().post(new UploadingEvent());
		DataCenter_.getInstance_(context).uploadImportSession(context, containerId);

		//Add Log
		DataCenter_.getInstance_(context).addLog(context, containerId, "Khởi tạo hoàn tất");
	}


	@Override
	protected void onCancel() {
		Context context = App.getInstance().getApplicationContext();
		DataCenter_.getInstance_(context).addLog(context, containerId, "Không thể khởi tạo");
	}

	@Override
	protected boolean shouldReRunOnThrowable(Throwable throwable) {
		Context context = App.getInstance().getApplicationContext();

		//Add Log
		DataCenter_.getInstance_(context).addLog(context, containerId, "Khởi tạo bị gián đoạn");
		EventBus.getDefault().post(new UploadStoppedEvent(containerId));

		return true;
	}
}
