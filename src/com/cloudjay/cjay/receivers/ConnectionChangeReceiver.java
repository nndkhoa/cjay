package com.cloudjay.cjay.receivers;

import com.aerilys.helpers.android.NetworkHelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class ConnectionChangeReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		if (NetworkHelper.isConnected(context)) {
			Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(context, "Not connected to Internet",
					Toast.LENGTH_SHORT).show();
		}

	}
}
