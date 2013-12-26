package com.cloudjay.cjay;

import java.util.ArrayList;
import java.util.List;

import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.TmpContainerSession;

import android.content.Context;

public class PhotoUploadController {

	private final ArrayList<TmpContainerSession> mUploadingList;

	PhotoUploadController(Context context) {
		mUploadingList = new ArrayList<TmpContainerSession>();

		// populateFromDatabase();
	}

	public static PhotoUploadController getFromContext(Context context) {
		return CJayApplication.getApplication(context)
				.getPhotoUploadController();
	}

	public synchronized boolean hasWaitingUploads() {
		for (TmpContainerSession upload : mUploadingList) {
			if (upload.getUploadState() == ContainerSession.STATE_UPLOAD_WAITING) {
				return true;
			}
		}
		return false;
	}
	
	public void removeUpload(final TmpContainerSession selection) {
		//TODO: FIX ME
	}
	
	public synchronized List<TmpContainerSession> getUploadingUploads() {
		return new ArrayList<TmpContainerSession>(mUploadingList);
	}
}
