package com.cloudjay.cjay;

import android.app.Application;
import android.content.Context;

import dagger.ObjectGraph;
import hugo.weaving.DebugLog;

public class CJayApplication extends Application {

	private ObjectGraph objectGraph;

	@Override
	public void onCreate() {
		super.onCreate();
//		Crashlytics.start(this);
		buildObjectGraphAndInject();
	}

	@DebugLog
	public void buildObjectGraphAndInject() {
		objectGraph = ObjectGraph.create();
		objectGraph.inject(this);
	}

	public void inject(Object o) {
		objectGraph.inject(o);
	}
	public static CJayApplication get(Context context) {
		return (CJayApplication) context.getApplicationContext();
	}
}
