package com.cloudjay.cjay.accountmanager;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Thai on 9/14/2014.
 */
public class CJayAuthenticatationService extends Service {
	@Override
	public IBinder onBind(Intent intent) {
		return new AccountAuthenticatior(this).getIBinder();
	}
}
