package com.cloudjay.cjay.service;

import org.androidannotations.annotations.EIntentService;

import android.app.IntentService;
import android.content.Intent;

import com.aerilys.helpers.android.NetworkHelper;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.Utils;

@EIntentService
public class QueueIntentService extends IntentService {

	public QueueIntentService() {
		super("QueueIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		if (!Utils.isRunning(this, ContainerUploadIntentService_.class.getName())
				&& NetworkHelper.isConnected(getApplicationContext())) {

			Intent uploadIntent = new Intent(this, ContainerUploadIntentService_.class);
			startService(uploadIntent);

		} else {
			Logger.w("ContainerUploadIntentService is already running");
		}

		if (!Utils.isRunning(this, PhotoUploadService_.class.getName())) {
			startService(Utils.getUploadPhotoIntent(this));
		} else {

			if (PhotoUploadService_.isCurrentlyUploading() == false) {

				Logger.w("Force stop PhotoUploadService");
				stopService(Utils.getUploadPhotoIntent(this));
				startService(Utils.getUploadPhotoIntent(this));

			}
		}
	}
}
