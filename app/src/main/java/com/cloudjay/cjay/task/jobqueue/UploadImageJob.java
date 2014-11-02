package com.cloudjay.cjay.task.jobqueue;

import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.api.NetworkClient_;
import com.cloudjay.cjay.event.upload.ItemEnqueueEvent;
import com.cloudjay.cjay.event.upload.UploadStoppedEvent;
import com.cloudjay.cjay.event.upload.UploadedEvent;
import com.cloudjay.cjay.event.upload.UploadingEvent;
import com.cloudjay.cjay.model.AuditImage;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.model.GateImage;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.enums.ImageType;
import com.cloudjay.cjay.util.enums.UploadStatus;
import com.cloudjay.cjay.util.enums.UploadType;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.snappydb.DB;
import com.snappydb.SnappydbException;

import de.greenrobot.event.EventBus;

public class UploadImageJob extends Job {
	String containerId;
	String uri;
	String imageName;
	ImageType imageType;

	@Override
	protected int getRetryLimit() {
		return 1;
	}

	public UploadImageJob(String uri, String imageName, String containerId, ImageType imageType) {
		super(new Params(1).requireNetwork().persist().groupBy(containerId).setPersistent(true));
		this.containerId = containerId;
		this.uri = uri;
		this.imageName = imageName;
		this.imageType = imageType;
	}

	@Override
	public void onAdded() {

		// Image is uploaded in background, but we still need to notify Upload Fragment
		// in case container session upload status is > UPLOADING.
		// It will notify fragment upload to update UI
		EventBus.getDefault().post(new ItemEnqueueEvent(containerId, UploadType.IMAGE));
	}

	@Override
	public void onRun() throws Throwable {

		// Notify to fragment upload that image is being uploaded.
		EventBus.getDefault().post(new UploadingEvent(containerId, UploadType.IMAGE));

		// Call data center to upload image
		Context context = App.getInstance().getApplicationContext();
		NetworkClient_.getInstance_(context).uploadImage(uri, imageName);

		// Change image status to COMPLETE
		setUploadStatus(context, containerId, imageName, imageType, UploadStatus.COMPLETE);

		EventBus.getDefault().post(new UploadedEvent(containerId));
	}

	private void setUploadStatus(Context context, String containerId, String imageName, ImageType type, UploadStatus status) throws SnappydbException {
		DB db = App.getDB(context);

		// Change status image in db
		String key = containerId;
		Session session = db.getObject(key, Session.class);

		if (session != null) {
			switch (type) {

				case AUDIT:
				case REPAIRED:
					for (AuditItem auditItem : session.getAuditItems()) {
						for (AuditImage auditImage : auditItem.getAuditImages()) {
							if (auditImage.getName().equals(imageName) && auditImage.getType() == type.value) {
								auditImage.setUploadStatus(status);
							}
						}
					}
					break;

				case IMPORT:
				case EXPORT:
				default:
					for (GateImage gateImage : session.getGateImages()) {
						if (gateImage.getName().equals(imageName) && gateImage.getType() == type.value) {
							Logger.Log(imageName + " " + gateImage.getType());
							gateImage.setUploadStatus(status);
							break;
						}
					}
					break;
			}

			db.put(key, session);
		}
	}

	@Override
	protected boolean shouldReRunOnThrowable(Throwable throwable) {
		return true;
	}

	/**
	 * // Job has exceeded retry attempts or shouldReRunOnThrowable() has returned false.
	 */
	@Override
	protected void onCancel() {

		// TODO: Set image upload Status to ERROR and notify to Upload Fragment

		Context context = App.getInstance().getApplicationContext();
		EventBus.getDefault().post(new UploadStoppedEvent(containerId));
		DataCenter_.getInstance_(context).addLog(context, containerId, "Không thể tải lên hình: " + imageName);
	}
}
