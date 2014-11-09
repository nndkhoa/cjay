package com.cloudjay.cjay.task.job;

import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.event.upload.UploadStartedEvent;
import com.cloudjay.cjay.event.upload.UploadStoppedEvent;
import com.cloudjay.cjay.event.upload.UploadSucceededEvent;
import com.cloudjay.cjay.event.upload.UploadingEvent;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.enums.Step;
import com.cloudjay.cjay.util.enums.UploadStatus;
import com.cloudjay.cjay.util.enums.UploadType;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.snappydb.SnappydbException;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;

public class UploadSessionJob extends Job {

	String containerId;
	int currentStep;
    Session mSession;

	/**
	 * Dùng để phân biệt xem có cần clear Working hay không?
	 */
	boolean needToClearFromWorking;

	@Override
	public int getRetryLimit() {
		return 1;
	}

	public UploadSessionJob(String containerId, int step, boolean clearFromWorking) {

        // step is local step
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

        mSession = DataCenter_.getInstance_(context).getSession(context, containerId);

		// Set session upload status to UPLOADING
		// Add session to Collection Upload
		// Change status uploading, currentStep audit, remove from WORKING
		try {
			dataCenter.addUploadSession(containerId);
			dataCenter.changeUploadStatus(context, containerId, UploadStatus.UPLOADING);

			Step step = Step.values()[currentStep];
			dataCenter.addLog(context, containerId, step.name() + " | Add container vào Queue");
			switch (step) {

				case IMPORT:
					if (mSession.getPreStatus() == 0) {
						dataCenter.changeSessionLocalStep(context, containerId, Step.AVAILABLE);
					} else {
						dataCenter.changeSessionLocalStep(context, containerId, Step.AUDIT);
					}
					break;

				case AUDIT:
					dataCenter.changeSessionLocalStep(context, containerId, Step.REPAIR);
					break;

				case REPAIR:
				case HAND_CLEAN:
					dataCenter.changeSessionLocalStep(context, containerId, Step.AVAILABLE);
					break;

				case EXPORTED:
				default:
					dataCenter.changeSessionLocalStep(context, containerId, Step.EXPORTED);
					break;
			}

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

		// Bắt đầu quá trình upload
		Context context = App.getInstance().getApplicationContext();
		DataCenter dataCenter = DataCenter_.getInstance_(context);
		EventBus.getDefault().post(new UploadingEvent(containerId, UploadType.SESSION));
		Step step = Step.values()[currentStep];

		Logger.Log(" >> Uploading container: " + containerId + " | " + step.name());
		dataCenter.addLog(context, containerId, step.name() + " | Bắt đầu quá trình upload");

		switch (step) {
			case AVAILABLE:
				dataCenter.uploadExportSession(context, containerId);
				break;

			case AUDIT:
				dataCenter.uploadAuditSession(context, containerId);
				break;

			case REPAIR:
				dataCenter.uploadRepairSession(context, containerId);
				break;

			case IMPORT:
				dataCenter.uploadImportSession(context, containerId);
				break;

			default:
				dataCenter.setHandCleaningSession(context, containerId);
				break;
		}

		// Change upload status to COMPLETE
		DataCenter_.getInstance_(context).changeUploadStatus(context, containerId, UploadStatus.COMPLETE);

		// Upload thành công
		DataCenter_.getInstance_(context).addLog(context, containerId, "Upload container thành công");
		EventBus.getDefault().post(new UploadSucceededEvent(containerId, UploadType.SESSION));
	}

	/**
	 * Retry to upload
	 *
	 * @param throwable
	 * @return
	 */
	@Override
	protected boolean shouldReRunOnThrowable(Throwable throwable) {

		if (throwable instanceof RetrofitError) {
			Context context = App.getInstance().getApplicationContext();
			DataCenter_.getInstance_(context).addLog(context, containerId, "Upload bị gián đoạn");

			//if it is a 4xx error, stop
			RetrofitError retrofitError = (RetrofitError) throwable;
			Logger.Log("Retrofit response: " + retrofitError.getSuccessType().toString());

			return retrofitError.getResponse().getStatus() < 400 || retrofitError.getResponse().getStatus() > 499;
		}

		// Notify upload process is retrying
		return true;
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
			DataCenter_.getInstance_(context).changeUploadStatus(context, containerId, UploadStatus.ERROR);
			EventBus.getDefault().post(new UploadStoppedEvent(containerId));

		} catch (SnappydbException e) {
			e.printStackTrace();
		}
	}
}
