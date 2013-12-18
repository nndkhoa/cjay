package com.cloudjay.cjay;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;
import android.content.Context;

@ReportsCrashes(formKey = "YOUR_FORM_KEY")
public class CJayApplication extends Application {
	
	public static CJayApplication getApplication(Context context) {
		return (CJayApplication) context.getApplicationContext();
	}
	
	@Override
	public void onCreate() {
		ACRA.init(this);
		super.onCreate();
	}
}
