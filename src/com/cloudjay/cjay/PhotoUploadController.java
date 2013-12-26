package com.cloudjay.cjay;

import java.util.ArrayList;
import java.util.List;

import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.PhotoUpload;

import android.content.Context;

public class PhotoUploadController {

	private final Context mContext;
	private final ArrayList<PhotoUpload> mSelectedPhotoList;
	private final ArrayList<PhotoUpload> mUploadingList;

	PhotoUploadController(Context context) {
		mContext = context;

		mSelectedPhotoList = new ArrayList<PhotoUpload>();
		mUploadingList = new ArrayList<PhotoUpload>();

		// populateFromDatabase();
	}

	public static PhotoUploadController getFromContext(Context context) {
		return CJayApplication.getApplication(context)
				.getPhotoUploadController();
	}

	public synchronized boolean hasWaitingUploads() {
		for (PhotoUpload upload : mUploadingList) {
			if (upload.getUploadState() == ContainerSession.STATE_UPLOAD_WAITING) {
				return true;
			}
		}
		return false;
	}
	
	public void removeUpload(final PhotoUpload selection) {
		//TODO: FIX ME
	}
	
	public synchronized List<PhotoUpload> getUploadingUploads() {
		return new ArrayList<PhotoUpload>(mUploadingList);
	}
}
