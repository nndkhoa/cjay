package com.cloudjay.cjay;

import java.util.ArrayList;
import java.util.List;

import com.cloudjay.cjay.model.ContainerSession;
import android.content.Context;

public class PhotoUploadController {

	private final ArrayList<ContainerSession> mUploadingList;

	PhotoUploadController(Context context) {
		mUploadingList = new ArrayList<ContainerSession>();

		// populateFromDatabase();
	}

	public static PhotoUploadController getFromContext(Context context) {
		return CJayApplication.getApplication(context)
				.getPhotoUploadController();
	}

	public synchronized boolean hasWaitingUploads() {
		for (ContainerSession upload : mUploadingList) {
			// if (upload.getUploadState() ==
			// ContainerSession.STATE_UPLOAD_WAITING) {
			// return true;
			// }
		}
		return false;
	}

	public void removeUpload(final ContainerSession selection) {
		// TODO: FIX ME
	}

	public synchronized List<ContainerSession> getUploadingUploads() {
		return new ArrayList<ContainerSession>(mUploadingList);
	}
}
