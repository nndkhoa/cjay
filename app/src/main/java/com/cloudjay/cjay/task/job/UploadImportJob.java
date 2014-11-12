package com.cloudjay.cjay.task.job;

import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.event.upload.PreUploadStartedEvent;
import com.cloudjay.cjay.event.upload.UploadStartedEvent;
import com.cloudjay.cjay.event.upload.UploadStoppedEvent;
import com.cloudjay.cjay.event.upload.UploadSucceededEvent;
import com.cloudjay.cjay.event.upload.UploadingEvent;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.Priority;
import com.cloudjay.cjay.util.enums.Step;
import com.cloudjay.cjay.util.enums.UploadStatus;
import com.cloudjay.cjay.util.enums.UploadType;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.snappydb.SnappydbException;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;

public class UploadImportJob extends Job {

	Session mSession;

	/**
	 * Dùng để phân biệt xem có cần clear Working hay không?
	 */
	@Override
	public int getRetryLimit() {
		return 1;
	}

	public UploadImportJob(Session session) {

		super(new Params(Priority.MID).requireNetwork().persist().groupBy(session.getContainerId()).setPersistent(true));

		// step is local step
		this.mSession = session;

	}

	/**
	 * 1. Thêm container session vào list upload
	 * 2. Notify cho Upload Fragment để cập nhật UI
	 * 3. Tự động thay đổi local currentStep đến level kế tiếp
	 * 4. Remove from Working tab if needed
	 */
	@Override
	public void onAdded() {

		// TODO: Change status to Uploading --> outside

		Context context = App.getInstance().getApplicationContext();
		Step step = Step.values()[mSession.getLocalStep()];
		DataCenter_.getInstance_(context).addLog(context, mSession.getContainerId(), step.name() + " | Add container vào Queue");

		EventBus.getDefault().post(new PreUploadStartedEvent(mSession, UploadType.SESSION));
		EventBus.getDefault().post(new UploadStartedEvent(mSession, UploadType.SESSION));
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

		EventBus.getDefault().post(new UploadingEvent(mSession.getContainerId(), UploadType.SESSION));

		Context context = App.getInstance().getApplicationContext();
		DataCenter dataCenter = DataCenter_.getInstance_(context);

		Step step = Step.values()[mSession.getLocalStep()];
		dataCenter.addLog(context, mSession.getContainerId(), step.name() + " | Bắt đầu quá trình upload");

		// Bắt đầu quá trình upload

		switch (step) {
			case AVAILABLE:
				dataCenter.uploadExportSession(context, mSession);
				break;

			case AUDIT:
				dataCenter.uploadAuditSession(context, mSession);
				break;

			case REPAIR:
				dataCenter.uploadRepairSession(context, mSession);
				break;

			case IMPORT:
				dataCenter.uploadImportSession(context, mSession);
				break;

			default:
				dataCenter.setHandCleaningSession(context, mSession);
				break;
		}

		dataCenter.addLog(context, mSession.getContainerId(), "Upload container thành công");
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
			DataCenter_.getInstance_(context).addLog(context, mSession.getContainerId(), "Upload bị gián đoạn");

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

		Context context = App.getInstance().getApplicationContext();
		DataCenter_.getInstance_(context).addLog(context, mSession.getContainerId(), "Upload thất bại");

		EventBus.getDefault().post(new UploadStoppedEvent(mSession));
	}
}
