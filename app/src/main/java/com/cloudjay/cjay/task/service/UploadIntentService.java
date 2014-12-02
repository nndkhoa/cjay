package com.cloudjay.cjay.task.service;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.task.command.UploadQueue;
import com.cloudjay.cjay.task.command.cjayobject.StartJobQueueCommand;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.PreferencesUtil;
import com.cloudjay.cjay.util.Utils;
import com.path.android.jobqueue.JobManager;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EIntentService;

import retrofit.RetrofitError;

/**
 * This intent service should be called in 2 cases:
 * - First time start app / reboot device, alarm manager will trigger automatically
 * - When user want to upload new item
 */
@EIntentService
public class UploadIntentService extends IntentService {

	@Bean
	DataCenter dataCenter;

	@Bean
	UploadQueue queue;

	public UploadIntentService() {
		super("UploadIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		executeNext();
	}

	/**
	 * Biến processing sẽ có giá trị false khi không còn task nào để thực hiện
	 */
	public static boolean processing;

	private void executeNext() {

		if (processing) return; // Only one task at a time.
		try {

			// Check if user is logged in or not
			String token = PreferencesUtil.getPrefsValue(getApplicationContext(), PreferencesUtil.PREF_TOKEN);
			if (!TextUtils.isEmpty(token) && Utils.canReachInternet()) {

				JobManager manager = App.getJobManager();
				if (manager.count() != 0) {
					Logger.Log("There is already job in the queue");
				} else {
					dataCenter.add(new StartJobQueueCommand(getApplicationContext()));
				}

			} else {

				Logger.w("There was problems. Please check credential or connectivity.");
				Logger.w("Upload service will be stopped.");
				processing = false;
				stopSelf();
			}

		} catch (RetrofitError e) {
			e.printStackTrace();
//			Utils.cancelAlarm(getApplicationContext());
		}
	}
}
