package com.cloudjay.cjay.task.jobqueue;

import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.event.upload.ItemEnqueueEvent;
import com.cloudjay.cjay.event.upload.UploadFailedEvent;
import com.cloudjay.cjay.event.upload.UploadSucceedEvent;
import com.cloudjay.cjay.event.upload.UploadingEvent;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.enums.UploadStatus;
import com.cloudjay.cjay.util.enums.UploadType;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import de.greenrobot.event.EventBus;

public class UploadSessionJob extends Job {

	Session session;

	/**
	 * Dùng để phân biệt xem có cần clear Working hay không?
	 */
	int type;

	@Override
	protected int getRetryLimit() {
		return 2;
	}

	public UploadSessionJob(Session session) {
		super(new Params(1).requireNetwork().persist().groupBy(session.getContainerId()));
		this.session = session;
	}

	/**
	 * 1. Thêm container session vào list upload
	 * 2. Notify cho Upload Fragment để cập nhật UI
	 */
	@Override
	public void onAdded() {

		// Add container to collection UPLOAD
		Context context = App.getInstance().getApplicationContext();
		String containerId = session.getContainerId();

		// Set session upload status to UPLOADING
		// Add session to Collection Upload
		DataCenter_.getInstance_(context).addLog(context, containerId, "Container được add vào hàng đợi");
		DataCenter_.getInstance_(context).addUploadSession(containerId);
		EventBus.getDefault().post(new ItemEnqueueEvent(containerId, UploadType.SESSION));
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

		// Bắt đầu quá trình upload
		DataCenter_.getInstance_(context).addLog(context, containerId, "Bắt đầu quá trình upload");
		EventBus.getDefault().post(new UploadingEvent(containerId, UploadType.SESSION));
		DataCenter_.getInstance_(context).uploadSession(context, session);

		// Upload thành công
		DataCenter_.getInstance_(context).addLog(context, containerId, "Upload thành công");
		EventBus.getDefault().post(new UploadSucceedEvent(containerId));
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
		DataCenter_.getInstance_(context).addLog(context, containerId, "Quá trình upload bị gián đoạn");

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
