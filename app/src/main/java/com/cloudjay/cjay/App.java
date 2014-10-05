package com.cloudjay.cjay;

import android.app.Application;
import android.content.Context;

public class App extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

//		Crashlytics.start(this);
	}

	public static App get(Context context) {
		return (App) context.getApplicationContext();
	}
}
