package com.cloudjay.cjay.service;

import org.androidannotations.annotations.EService;

import android.app.ActivityManager;
import android.app.IntentService;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Intent;
import com.aerilys.helpers.android.NetworkHelper;
import com.cloudjay.cjay.util.CountingInputStreamEntity;

@EService
public class QueueIntentService extends IntentService implements
		CountingInputStreamEntity.UploadListener {
	private boolean isUploadIntentServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if ("com.cloudjay.cjay.network.UploadIntentService"
					.equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	public QueueIntentService() {
		super("QueueIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (isUploadIntentServiceRunning() == false
				&& NetworkHelper.isConnected(getApplicationContext())) {
			Intent uploadIntent = new Intent(this, UploadIntentService_.class);
			startService(uploadIntent);
		}
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
	public void onChange(int percent) {
	}
}
