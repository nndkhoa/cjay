package com.cloudjay.cjay.task.jobqueue;

import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.event.upload.UploadStartedEvent;
import com.cloudjay.cjay.event.upload.UploadStoppedEvent;
import com.cloudjay.cjay.event.upload.UploadedEvent;
import com.cloudjay.cjay.util.enums.Step;
import com.cloudjay.cjay.util.enums.UploadStatus;
import com.cloudjay.cjay.util.enums.UploadType;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.snappydb.SnappydbException;

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

	/**
	 * 1. Thêm container session vào list upload
	 * 2. Notify cho Upload Fragment để cập nhật UI
	 */
	@Override
	public void onAdded() {


		// Add container to collection UPLOAD
		Context context = App.getInstance().getApplicationContext();
		DataCenter_.getInstance_(context).addUploadSession(containerId);

		//Change status uploadding, step audit, remove from WORKING
		try {
			DataCenter_.getInstance_(context).changeUploadState(context,containerId, UploadStatus.UPLOADING);
			DataCenter_.getInstance_(context).changeStepSession(context,containerId, Step.AUDIT);
			DataCenter_.getInstance_(context).removeWorkingSession(context,containerId );
		} catch (SnappydbException e) {
			e.printStackTrace();
		}

		EventBus.getDefault().post(new UploadStartedEvent(containerId, UploadType.SESSION));
	}

	/**
	 * 1. Notify qua BUS quá trình upload đang diễn ra
	 * 2. Gọi hàm upload container json lên server
	 * 3. Nếu upload thành công (không bị throw exception) thì gán
	 *
	 * @throws Throwable
	 */
	@Override
	public void onRun() throws Throwable {
		Context context = App.getInstance().getApplicationContext();
		String containerId = session.getContainerId();

		//Add Log
		DataCenter_.getInstance_(context).addLog(context, containerId, "Bắt đầu khởi tạo");

//		EventBus.getDefault().post(new UploadingEvent());
		DataCenter_.getInstance_(context).uploadImportSession(context, containerId);

		//Add Log
		DataCenter_.getInstance_(context).addLog(context, containerId, "Khởi tạo hoàn tất");

		//Change status uploadded
		DataCenter_.getInstance_(context).changeUploadState(context,containerId, UploadStatus.COMPLETE);

		EventBus.getDefault().post(new UploadedEvent(containerId));
	}


	@Override
	protected void onCancel() {
		Context context = App.getInstance().getApplicationContext();
		DataCenter_.getInstance_(context).addLog(context, containerId, "Không thể khởi tạo");

		//Change status error
		try {
			DataCenter_.getInstance_(context).changeUploadState(context,containerId, UploadStatus.ERROR);
		} catch (SnappydbException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Retry to upload
	 *
	 * @param throwable
	 * @return
	 */
	@Override
	protected boolean shouldReRunOnThrowable(Throwable throwable) {

		Context context = App.getInstance().getApplicationContext();
		String containerId = session.getContainerId();

		//Add Log
		DataCenter_.getInstance_(context).addLog(context, containerId, "Khởi tạo bị gián đoạn");
		EventBus.getDefault().post(new UploadStoppedEvent(containerId));

		// Notify upload process is retrying
		return true;
	}

	/**
	 * Quá trình upload thất bại. Gán status ERROR container
	 */
	@Override
	protected void onCancel() {

		Context context = App.getInstance().getApplicationContext();
		String containerId = session.getContainerId();

		// Set status ERROR vào container id
		DataCenter_.getInstance_(context).addLog(context, containerId, "Upload thất bại");
		DataCenter_.getInstance_(context).setSessionStatus(context, containerId, UploadStatus.ERROR);
		EventBus.getDefault().post(new UploadFailedEvent(containerId));
	}

}
