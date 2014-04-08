package com.cloudjay.cjay.service;

import org.androidannotations.annotations.EIntentService;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.IntentService;
import android.content.Intent;

import com.aerilys.helpers.android.NetworkHelper;
import com.cloudjay.cjay.util.CountingInputStreamEntity;

@EIntentService
public class QueueIntentService extends IntentService implements CountingInputStreamEntity.UploadListener {
	public QueueIntentService() {
		super("QueueIntentService");
	}

	private boolean isUploadIntentServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if ("com.cloudjay.cjay.network.UploadIntentService".equals(service.service.getClassName())) return true;
		}
		return false;
	}

	@Override
	public void onChange(int percent) {
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
		if (isUploadIntentServiceRunning() == false && NetworkHelper.isConnected(getApplicationContext())) {
			Intent uploadIntent = new Intent(this, UploadIntentService_.class);
			startService(uploadIntent);
		}
	}
}
