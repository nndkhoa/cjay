package com.cloudjay.cjay.task.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cloudjay.cjay.task.service.PubnubService_;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.StringUtils;
import com.cloudjay.cjay.util.Utils;

import org.androidannotations.annotations.EReceiver;

/**
 * AutoStart Receiver được gọi khi máy Boot thành công. AutoStart Receiver sẽ kiểm tra
 * và gọi alarm manager để trigger JobManager & PubNub Service.
 */
@EReceiver
public class AutoStartReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		Logger.Log("**********started************");

		// Start alarm manager
		if (!Utils.isAlarmUp(context)) {
			Logger.Log("Alarm Manager is not running. Starting alarm ...");
			Utils.startAlarm(context);
		} else {
			Logger.Log("Alarm is already running "
					+ StringUtils.getCurrentTimestamp(CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE));
		}
	}
}
