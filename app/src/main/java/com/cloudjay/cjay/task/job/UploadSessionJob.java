package com.cloudjay.cjay.task.job;

import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.event.upload.UploadStoppedEvent;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.model.UploadObject;
import com.cloudjay.cjay.task.command.log.AddLogCommand;
import com.cloudjay.cjay.task.command.session.remove.RemoveSessionCommand;
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
	UploadObject object;

	@Override
	public int getRetryLimit() {
		return 1;
	}

	public UploadSessionJob(Session session, UploadObject object) {
		super(new Params(Priority.MID).requireNetwork().persist().groupBy(session.getContainerId()).setPersistent(true));
		this.mSession = session;
		this.object = object;
	}

	@Override
	public void onAdded() {
	}

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
		// Network operation. It may throw an network exception that will be handle by `shouldReRunOnThrowable`
		Step uploadStep = Step.values()[x];
		Session response = dataCenter.uploadSession(context, mSession, uploadStep);
		dataCenter.add(new AddLogCommand(context, response.getContainerId(), "Upload container thành công", CJayConstant.PREFIX_LOG));

		// Save session and also notify success to Upload Fragment by posting UploadSucceededEvent in SaveSessionCommand
		dataCenter.add(new SaveSessionCommand(context, response, UploadType.SESSION));



		// UploadService will receive event and take care of the rest.

	}

	@Override
	protected boolean shouldReRunOnThrowable(Throwable throwable) {

		Logger.w("XXX");
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

	@Override
	protected void onCancel() {
		Context context = App.getInstance().getApplicationContext();
		DataCenter_.getInstance_(context).add(new AddLogCommand(context, mSession.getContainerId(), "Upload thất bại", CJayConstant.PREFIX_LOG));
		EventBus.getDefault().post(new UploadStoppedEvent(mSession));
	}
}
