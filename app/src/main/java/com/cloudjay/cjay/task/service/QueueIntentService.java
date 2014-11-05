package com.cloudjay.cjay.task.service;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;

import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.util.PreferencesUtil;
import com.cloudjay.cjay.util.Utils;
import com.snappydb.SnappydbException;

import org.androidannotations.annotations.EIntentService;

import retrofit.RetrofitError;

@EIntentService
public class QueueIntentService extends IntentService {
	public QueueIntentService() {
		super("QueueIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		try {
			String token = PreferencesUtil.getPrefsValue(getApplicationContext(), PreferencesUtil.PREF_TOKEN);

			if (!TextUtils.isEmpty(token)) {
				String lastModifiedTime = PreferencesUtil.getPrefsValue(getApplicationContext(), PreferencesUtil.PREF_MODIFIED_DATE);
				DataCenter_.getInstance_(getApplicationContext()).fetchSession(getApplicationContext(), lastModifiedTime);
			}

		} catch (SnappydbException e) {
			e.printStackTrace();

		} catch (RetrofitError e) {
			e.printStackTrace();
			Utils.cancelAlarm(getApplicationContext());
		}
	}
}
