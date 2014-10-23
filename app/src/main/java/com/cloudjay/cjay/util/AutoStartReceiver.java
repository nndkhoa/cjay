package com.cloudjay.cjay.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.androidannotations.annotations.EReceiver;

/**
 * Created by thai on 22/10/2
 */
@EReceiver
public class AutoStartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Logger.Log("**********started************");
        // Making Alarm for Queue Worker
        if (!Utils.isAlarmUp(context)) {

            Logger.Log("Alarm Manager is not running.");
            Utils.startAlarm(context);

        } else {
            Logger.Log("Alarm is already running "
                    + StringHelper.getCurrentTimestamp(CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE));
        }
    }
}
