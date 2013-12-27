package com.cloudjay.cjay;

import java.util.ArrayList;
import java.util.List;

import com.cloudjay.cjay.model.GateReportImage;
import android.content.Context;

public class PhotoUploadController {

	private final ArrayList<GateReportImage> mUploadingList;

	PhotoUploadController(Context context) {
		mUploadingList = new ArrayList<GateReportImage>();

		// populateFromDatabase();
	}

	public static PhotoUploadController getFromContext(Context context) {
		return CJayApplication.getApplication(context)
				.getPhotoUploadController();
	}

	public synchronized boolean hasWaitingUploads() {
		for (GateReportImage upload : mUploadingList) {
			// if (upload.getUploadState() ==
			// ContainerSession.STATE_UPLOAD_WAITING) {
			// return true;
			// }
		}
		return false;
	}

	public void removeUpload(final GateReportImage selection) {
		// TODO: FIX ME
	}

	public synchronized List<GateReportImage> getUploadingUploads() {
		return new ArrayList<GateReportImage>(mUploadingList);
	}
}
