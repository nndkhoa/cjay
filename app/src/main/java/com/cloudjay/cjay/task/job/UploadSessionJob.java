package com.cloudjay.cjay.task.job;

import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.event.upload.UploadStoppedEvent;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.command.log.AddLogCommand;
import com.cloudjay.cjay.task.command.session.update.PrepareForUploadingCommand;
import com.cloudjay.cjay.task.command.session.update.SaveSessionCommand;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.Priority;
import com.cloudjay.cjay.util.enums.Step;
import com.cloudjay.cjay.util.enums.UploadType;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;

public class UploadSessionJob extends Job {

	Session mSession;

	/**
	 * Dùng để phân biệt xem có cần clear Working hay không?
	 */
	@Override
	public int getRetryLimit() {
		return 1;
	}

	public UploadSessionJob(Session session) {
		super(new Params(Priority.MID).requireNetwork().persist().groupBy(session.getContainerId()).setPersistent(true));
		this.mSession = session;
	}

	/**
	 * 1. Thêm container session vào list upload
	 * 2. Notify cho Upload Fragment để cập nhật UI
	 * 3. Tự động thay đổi local currentStep đến stage kế tiếp
	 * 4. Remove from Working tab if needed
	 */
	@Override
	public void onAdded() {
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

		// Change local step and post Upload Started Event also
		dataCenter.add(new PrepareForUploadingCommand(context, mSession));

		// Bắt đầu quá trình upload
		Step step = Step.values()[mSession.getLocalStep()];
		Session response = dataCenter.uploadSession(context, mSession, step);

		// Save session and also notify success to Upload Fragment
		dataCenter.add(new AddLogCommand(context, response.getContainerId(), "Upload container thành công", CJayConstant.PREFIX_LOG));
		dataCenter.add(new SaveSessionCommand(context, response, UploadType.SESSION));
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
			DataCenter_.getInstance_(context).add(new AddLogCommand(context, mSession.getContainerId(), "Upload bị gián đoạn", CJayConstant.PREFIX_LOG));

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
		DataCenter_.getInstance_(context).add(new AddLogCommand(context, mSession.getContainerId(), "Upload thất bại", CJayConstant.PREFIX_LOG));
		EventBus.getDefault().post(new UploadStoppedEvent(mSession));
	}
}
