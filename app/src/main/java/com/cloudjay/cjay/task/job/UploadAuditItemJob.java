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
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.enums.UploadStatus;
import com.cloudjay.cjay.util.enums.UploadType;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.snappydb.SnappydbException;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;

public class UploadAuditItemJob extends Job {

	Session session;
	String auditItemUuid;

	@Override
	public int getRetryLimit() {
		return CJayConstant.RETRY_THRESHOLD;
	}

	public UploadAuditItemJob(Session session, String auditItemUUID) {
		super(new Params(1).requireNetwork().persist().groupBy(session.getContainerId()).setPersistent(true));
		this.session = session;
		this.auditItemUuid = auditItemUUID;
	}

	@Override
	public void onAdded() {

			EventBus.getDefault().post(new UploadStartedEvent(session, UploadType.AUDIT_ITEM));

	}

	@Override
	public void onRun() throws Throwable {

		Context context = App.getInstance().getApplicationContext();
		DataCenter dataCenter = DataCenter_.getInstance_(context);

		dataCenter.addLog(context, session.getContainerId(), "Bắt đầu upload audit item: " + auditItemUuid);
		EventBus.getDefault().post(new UploadingEvent(session, UploadType.AUDIT_ITEM));

		dataCenter.uploadAuditItem(context, session, auditItemUuid);

		EventBus.getDefault().post(new UploadSucceededEvent(session, UploadType.AUDIT_ITEM));
	}

	@Override
	protected void onCancel() {

			Context context = App.getInstance().getApplicationContext();
			DataCenter_.getInstance_(context).addLog(context, session.getContainerId(), "Upload lỗi thất bại");

			EventBus.getDefault().post(new UploadStoppedEvent(session));

	}

	@Override
	protected boolean shouldReRunOnThrowable(Throwable throwable) {

		if (throwable instanceof RetrofitError) {
			Context context = App.getInstance().getApplicationContext();
			DataCenter_.getInstance_(context).addLog(context, session.getContainerId(), "Upload lỗibị gián đoạn");

			//if it is a 4xx error, stop
			RetrofitError retrofitError = (RetrofitError) throwable;
			Logger.Log("Retrofit response: " + retrofitError.getSuccessType().toString());

			return retrofitError.getResponse().getStatus() < 400 || retrofitError.getResponse().getStatus() > 499;
		}

		// Notify upload process is retrying
		return true;
	}
}
