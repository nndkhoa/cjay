package com.cloudjay.cjay.task.job;

import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.event.upload.UploadStartedEvent;
import com.cloudjay.cjay.event.upload.UploadStoppedEvent;
import com.cloudjay.cjay.event.upload.UploadSucceededEvent;
import com.cloudjay.cjay.event.upload.UploadingEvent;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.model.UploadObject;
import com.cloudjay.cjay.task.command.cjayobject.RemoveUploadObjectCommand;
import com.cloudjay.cjay.task.command.issue.UpdateAuditItemCommand;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Priority;
import com.cloudjay.cjay.util.enums.UploadType;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;

public class UploadAuditItemJob extends Job {

	long sessionId;
	AuditItem auditItem;
    String containerId;
    boolean addMoreImages;
	UploadObject object;

	@Override
	public int getRetryLimit() {
		return CJayConstant.RETRY_THRESHOLD;
	}

	public UploadAuditItemJob(long sessionId, AuditItem auditItem, String containerId,UploadObject object, boolean addMoreImages) {
		super(new Params(Priority.MID).requireNetwork().persist().groupBy(containerId).setPersistent(true));
		this.sessionId = sessionId;
		this.auditItem = auditItem;
        this.containerId = containerId;
        this.addMoreImages = addMoreImages;
		this.object = object;
	}

	@Override
	public void onAdded() {
		EventBus.getDefault().post(new UploadStartedEvent(containerId, UploadType.AUDIT_ITEM));
	}

	@Override
	public void onRun() throws Throwable {

		Context context = App.getInstance().getApplicationContext();
		DataCenter dataCenter = DataCenter_.getInstance_(context);

		dataCenter.addLog(context, containerId, "Bắt đầu upload audit item: " + auditItem.getUuid(), CJayConstant.PREFIX_LOG);
		EventBus.getDefault().post(new UploadingEvent(containerId, UploadType.AUDIT_ITEM));

		AuditItem item;
		if (!this.addMoreImages) {
			item = dataCenter.uploadAuditItem(context, sessionId, auditItem);
		} else {
			item = dataCenter.uploadAddedAuditImage(context, auditItem);
		}

		dataCenter.add(new UpdateAuditItemCommand(context, containerId, item));
		EventBus.getDefault().post(new UploadSucceededEvent(containerId, UploadType.AUDIT_ITEM));
	}

	@Override
	protected void onCancel() {

		Context context = App.getInstance().getApplicationContext();
		DataCenter_.getInstance_(context).addLog(context, containerId, "Upload lỗi thất bại", CJayConstant.PREFIX_LOG);
		EventBus.getDefault().post(new UploadStoppedEvent(containerId));

	}

	@Override
	protected boolean shouldReRunOnThrowable(Throwable throwable) {

		if (throwable instanceof RetrofitError) {
			Context context = App.getInstance().getApplicationContext();
			DataCenter_.getInstance_(context).addLog(context, containerId, "Upload bị gián đoạn", CJayConstant.PREFIX_LOG);

			//if it is a 4xx error, stop
			RetrofitError retrofitError = (RetrofitError) throwable;
			return retrofitError.getResponse().getStatus() < 400 || retrofitError.getResponse().getStatus() > 499;
		}

		// Notify upload process is retrying
		return true;
	}
}
