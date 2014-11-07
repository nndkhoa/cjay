package com.cloudjay.cjay.util;

import android.os.Looper;

import com.cloudjay.cjay.BuildConfig;

public class ThreadPreconditions {
	public static void checkOnMainThread() {
		if (BuildConfig.DEBUG) {
			if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
				throw new IllegalStateException("This method should be called from the Main Thread");
			}
		}
	}
}
