package com.cloudjay.cjay;

import android.app.Application;
import android.content.Context;

public class CJayApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

//		Crashlytics.start(this);
	}

	public static CJayApplication get(Context context) {
		return (CJayApplication) context.getApplicationContext();
	}
}
