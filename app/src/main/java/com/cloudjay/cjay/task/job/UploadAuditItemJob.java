package com.cloudjay.cjay.task.job;

import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.event.upload.UploadStartedEvent;
import com.cloudjay.cjay.event.upload.UploadStoppedEvent;
import com.cloudjay.cjay.event.upload.UploadSucceededEvent;
import com.cloudjay.cjay.event.upload.UploadingEvent;
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

	String containerId;
	String auditItemUuid;

	@Override
	public int getRetryLimit() {
		return CJayConstant.RETRY_THRESHOLD;
	}

	public UploadAuditItemJob(String containerId, String auditItemUUID) {
		super(new Params(1).requireNetwork().persist().groupBy(containerId).setPersistent(true));
		this.containerId = containerId;
		this.auditItemUuid = auditItemUUID;
	}

	@Override
	public void onAdded() {

		try {
			Context context = App.getInstance().getApplicationContext();
			DataCenter_.getInstance_(context).changeUploadStatus(context, containerId, auditItemUuid, UploadStatus.UPLOADING);
			EventBus.getDefault().post(new UploadStartedEvent(containerId, UploadType.AUDIT_ITEM));
		} catch (SnappydbException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onRun() throws Throwable {

		Context context = App.getInstance().getApplicationContext();
		DataCenter dataCenter = DataCenter_.getInstance_(context);

		dataCenter.addLog(context, containerId, "Bắt đầu upload audit item: " + auditItemUuid);
		EventBus.getDefault().post(new UploadingEvent(containerId, UploadType.AUDIT_ITEM));

		dataCenter.uploadAuditItem(context, containerId, auditItemUuid);

		dataCenter.changeUploadStatus(context, containerId, auditItemUuid, UploadStatus.COMPLETE);
		EventBus.getDefault().post(new UploadSucceededEvent(containerId, UploadType.AUDIT_ITEM));
	}

	@Override
	protected void onCancel() {

		try {

			Context context = App.getInstance().getApplicationContext();
			DataCenter_.getInstance_(context).addLog(context, containerId, "Upload lỗi thất bại");
			DataCenter_.getInstance_(context).changeUploadStatus(context, containerId, auditItemUuid, UploadStatus.ERROR);
			EventBus.getDefault().post(new UploadStoppedEvent(containerId));

		} catch (SnappydbException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected boolean shouldReRunOnThrowable(Throwable throwable) {

		if (throwable instanceof RetrofitError) {
			Context context = App.getInstance().getApplicationContext();
			DataCenter_.getInstance_(context).addLog(context, containerId, "Upload lỗibị gián đoạn");

			//if it is a 4xx error, stop
			RetrofitError retrofitError = (RetrofitError) throwable;
			Logger.Log("Retrofit response: " + retrofitError.getSuccessType().toString());

			return retrofitError.getResponse().getStatus() < 400 || retrofitError.getResponse().getStatus() > 499;
		}

		// Notify upload process is retrying
		return true;
	}
}
