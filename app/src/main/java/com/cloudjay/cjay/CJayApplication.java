package com.cloudjay.cjay;

import android.app.Application;

import com.crashlytics.android.Crashlytics;

public class CJayApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		Crashlytics.start(this);
	}
}
