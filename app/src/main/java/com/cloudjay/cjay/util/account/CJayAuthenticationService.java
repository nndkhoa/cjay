package com.cloudjay.cjay.util.account;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * TODO: write description
 */
public class CJayAuthenticationService extends Service {
	@Override
	public IBinder onBind(Intent intent) {
		return new AccountAuthenticator(this).getIBinder();
	}
}
