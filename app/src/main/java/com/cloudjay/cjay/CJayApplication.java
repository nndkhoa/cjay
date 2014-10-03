package com.cloudjay.cjay;

import android.app.Application;
import android.content.Context;

import java.util.Arrays;
import java.util.List;

import dagger.ObjectGraph;
import hugo.weaving.DebugLog;

public class CJayApplication extends Application {

	private ObjectGraph objectGraph;

	@Override
	public void onCreate() {
		super.onCreate();

		// Build Dagger graph
		buildObjectGraphAndInject();

		// Crashlytics.start(this);
	}

	@DebugLog
	public void buildObjectGraphAndInject() {
		objectGraph = ObjectGraph.create(getModules().toArray());
		objectGraph.inject(this);
	}

	private List<Object> getModules() {
		return Arrays.<Object>asList(new CJayModule(this));
	}

	public void inject(Object o) {
		objectGraph.inject(o);
	}
	public static CJayApplication get(Context context) {
		return (CJayApplication) context.getApplicationContext();
	}
}
