package com.cloudjay.cjay.task.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.text.TextUtils;

import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.api.NetworkClient_;
import com.cloudjay.cjay.task.command.session.FetchSessionsCommand;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.PreferencesUtil;
import com.cloudjay.cjay.util.StringUtils;
import com.cloudjay.cjay.util.Utils;

import org.androidannotations.annotations.EIntentService;

import java.io.File;

import retrofit.RetrofitError;

@EIntentService
public class SyncIntentService extends IntentService {
	public SyncIntentService() {
		super("SyncIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		try {
			String token = PreferencesUtil.getPrefsValue(getApplicationContext(), PreferencesUtil.PREF_TOKEN);
			if (!TextUtils.isEmpty(token) && Utils.canReachInternet()) {
				Logger.w(" > Sync session between local and server");

				String lastModifiedTime = PreferencesUtil.getPrefsValue(getApplicationContext(), PreferencesUtil.PREF_MODIFIED_DATE);
				DataCenter_.getInstance_(this).add(new FetchSessionsCommand(getApplicationContext(), lastModifiedTime, true));

			} else {
				Logger.w("There was problems. Please check credential or connectivity.");
			}

		} catch (RetrofitError e) {
			e.printStackTrace();
			Utils.cancelAlarm(getApplicationContext());
		}
	}
}
