package com.cloudjay.cjay.service;

import org.androidannotations.annotations.EIntentService;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.IntentService;
import android.content.Intent;

import com.aerilys.helpers.android.NetworkHelper;
import com.cloudjay.cjay.events.CJayImageAddedEvent;
import com.cloudjay.cjay.events.NewCJayImageEvent;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.CountingInputStreamEntity;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.Utils;

import de.greenrobot.event.EventBus;

@EIntentService
public class QueueIntentService extends IntentService implements CountingInputStreamEntity.UploadListener {

	public QueueIntentService() {
		super("QueueIntentService");
	}

	@Override
	public void onChange(int percent) {
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		if (!Utils.isRunning(this, UploadIntentService_.class.getName())
				&& NetworkHelper.isConnected(getApplicationContext())) {

			Intent uploadIntent = new Intent(this, UploadIntentService_.class);
			startService(uploadIntent);
		}

		if (!Utils.isRunning(this, PhotoUploadService_.class.getName())) {

			// Logger.w("PhotoUploadService is not running");
			startService(Utils.getUploadAllIntent(this));

		} else {
			// Logger.w("PhotoUploadService is already running");
		}
	}
}
