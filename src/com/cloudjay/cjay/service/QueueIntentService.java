package com.cloudjay.cjay.service;

import org.androidannotations.annotations.EIntentService;

import android.app.IntentService;
import android.content.Intent;

import com.aerilys.helpers.android.NetworkHelper;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.StringHelper;
import com.cloudjay.cjay.util.Utils;

@EIntentService
public class QueueIntentService extends IntentService {

	public QueueIntentService() {
		super("QueueIntentService");
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		if (!Utils.isRunning(this, ContainerUploadIntentService_.class.getName())
				&& NetworkHelper.isConnected(getApplicationContext())) {

			Logger.Log("Start ContainerUploadIntentService at "
					+ StringHelper.getCurrentTimestamp(CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE));

			Intent uploadIntent = new Intent(this, ContainerUploadIntentService_.class);
			startService(uploadIntent);

		} else {
			Logger.w("ContainerUploadIntentService is already running");
		}

		if (!Utils.isRunning(this, PhotoUploadService_.class.getName())) {

			Logger.Log("Start PhotoUploadService at "
					+ StringHelper.getCurrentTimestamp(CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE));
			startService(Utils.getUploadAllIntent(this));
		} else {

			if (PhotoUploadService_.isCurrentlyUploading() == false) {
				Logger.w("Force stop PhotoUploadService");
				stopService(Utils.getUploadAllIntent(this));
				startService(Utils.getUploadAllIntent(this));
			}
		}
	}
}
