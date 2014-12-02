package com.cloudjay.cjay.task.job;

import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.event.upload.UploadStoppedEvent;
import com.cloudjay.cjay.model.CJayObject;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.command.cjayobject.GetNextJobCommand;
import com.cloudjay.cjay.task.command.cjayobject.RemoveCJayObjectCommand;
import com.cloudjay.cjay.task.command.log.AddLogCommand;
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
	CJayObject object;

	/**
	 * Dùng để phân biệt xem có cần clear Working hay không?
	 */
	@Override
	public int getRetryLimit() {
		return 1;
	}

	public UploadSessionJob(Session session, CJayObject object) {
		super(new Params(Priority.MID).requireNetwork().persist().groupBy(session.getContainerId()).setPersistent(true));
		this.mSession = session;
		this.object = object;
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

		// Tính toán lại giá trị của biến x, là giá trị trước khi đưa vào Queue.
		// Cần refactor lại code sau khi release.
		int x = mSession.getLocalStep();
		if (mSession.getLocalStep() <= 3 && mSession.getLocalStep() >= 1) {
			x = mSession.getLocalStep() - 1;
		} else if (mSession.getLocalStep() == 0) {
			x = 4;
		}

		mSession.setId(object.getSessionId());

		// Bắt đầu quá trình upload
		Step uploadStep = Step.values()[x];
		Session response = dataCenter.uploadSession(context, mSession, uploadStep);

		dataCenter.addLog(context, mSession.getContainerId(), "Upload container thành công", CJayConstant.PREFIX_LOG);

		// Save session and also notify success to Upload Fragment
		dataCenter.add(new AddLogCommand(context, response.getContainerId(), "Upload container thành công", CJayConstant.PREFIX_LOG));
		dataCenter.add(new SaveSessionCommand(context, response, UploadType.SESSION));
		dataCenter.add(new RemoveCJayObjectCommand(context, object));

		// TODO: #tieubao, I think we should post an event to Upload Service
		// Get next item
		dataCenter.add(new GetNextJobCommand(context, object));
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
