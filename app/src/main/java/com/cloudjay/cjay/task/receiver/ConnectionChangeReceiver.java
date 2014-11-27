package com.cloudjay.cjay.task.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cloudjay.cjay.event.pubnub.PubnubSubscriptionChangedEvent;
import com.cloudjay.cjay.util.PreferencesUtil;
import com.cloudjay.cjay.util.Utils;

import org.androidannotations.annotations.EReceiver;

import de.greenrobot.event.EventBus;

@EReceiver
public class ConnectionChangeReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

        if (!Utils.canReachInternet()) {
            PreferencesUtil.storePrefsValue(context, PreferencesUtil.PREF_SUBSCRIBE_PUBNUB, false);
            EventBus.getDefault().post(new PubnubSubscriptionChangedEvent(false));
        } else {
            if (!Utils.isAlarmUp(context))
                Utils.startAlarm(context);
        }

		Utils.keepNotificationAlive(context);
	}
}