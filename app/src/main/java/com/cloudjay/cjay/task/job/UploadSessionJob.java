package com.cloudjay.cjay.task.job;

import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.event.upload.UploadStartedEvent;
import com.cloudjay.cjay.event.upload.UploadStoppedEvent;
import com.cloudjay.cjay.event.upload.UploadedEvent;
import com.cloudjay.cjay.event.upload.UploadingEvent;
import com.cloudjay.cjay.util.enums.Step;
import com.cloudjay.cjay.util.enums.UploadStatus;
import com.cloudjay.cjay.util.enums.UploadType;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.snappydb.SnappydbException;

import de.greenrobot.event.EventBus;

public class UploadSessionJob extends Job {

	String containerId;
	int currentStep;

	/**
	 * Dùng để phân biệt xem có cần clear Working hay không?
	 */
	boolean needToClearFromWorking;

	@Override
	protected int getRetryLimit() {
		return 1;
	}

	public UploadSessionJob(String containerId, int step, boolean clearFromWorking) {
		super(new Params(1).requireNetwork().persist().groupBy(containerId));
		this.containerId = containerId;
		this.currentStep = step;
		this.needToClearFromWorking = clearFromWorking;
	}

	/**
	 * 1. Thêm container session vào list upload
	 * 2. Notify cho Upload Fragment để cập nhật UI
	 * 3. Tự động thay đổi local currentStep đến level kế tiếp
	 * 4. Remove from Working tab if needed
	 */
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
			dataCenter.changeSessionLocalStep(context, containerId, Step.AUDIT);

			if (needToClearFromWorking) {
				DataCenter_.getInstance_(context).removeWorkingSession(context, containerId);
			}

			EventBus.getDefault().post(new UploadStartedEvent(containerId, UploadType.SESSION));
		} catch (SnappydbException e) {
			e.printStackTrace();
		}
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
		DataCenter dataCenter = DataCenter_.getInstance_(context);

		// Bắt đầu quá trình upload
		EventBus.getDefault().post(new UploadingEvent(containerId, UploadType.SESSION));

		Step step = Step.values()[currentStep];
		switch (step) {
			case EXPORT:

				dataCenter.addLog(context, containerId, "EXPORT | Bắt đầu quá trình upload");
				dataCenter.uploadExportSession(context, containerId);
				break;

			case AUDIT:
				dataCenter.addLog(context, containerId, "AUDIT | Bắt đầu quá trình upload");
				dataCenter.uploadAuditedSession(context, containerId);
				break;

			case REPAIR:
				dataCenter.addLog(context, containerId, "REPAIR | Bắt đầu quá trình upload");
				dataCenter.uploadRepairedSession(context, containerId);
				break;

			case IMPORT:
				dataCenter.addLog(context, containerId, "IMPORT | Bắt đầu quá trình upload");
				dataCenter.uploadImportSession(context, containerId);
				break;

			default:
				dataCenter.addLog(context, containerId, "HAND CLEANING | Bắt đầu quá trình upload");
				dataCenter.setHandCleaningSession(context, containerId);
				break;
		}

		// Change upload status to COMPLETE
		DataCenter_.getInstance_(context).changeUploadState(context, containerId, UploadStatus.COMPLETE);

		// Upload thành công
		DataCenter_.getInstance_(context).addLog(context, containerId, "Upload container session thành công");
		EventBus.getDefault().post(new UploadedEvent(containerId));
	}


	/**
	 * Quá trình upload thất bại. Gán status ERROR container
	 */
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


	/**
	 * Retry to upload
	 *
	 * @param throwable
	 * @return
	 */
	@Override
	protected boolean shouldReRunOnThrowable(Throwable throwable) {
		Context context = App.getInstance().getApplicationContext();
		DataCenter_.getInstance_(context).addLog(context, containerId, "Quá trình upload bị gián đoạn");

		// Notify upload process is retrying

		return true;
	}
}
