package com.cloudjay.cjay;

import android.app.Application;

import com.cloudjay.cjay.data.DataModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

// Define this will be a dagger module
@Module(
		includes = {
				DataModule.class
		},
		injects = {
				CJayApplication.class
		}
)
public class CJayModule {

	private final CJayApplication app;

	public CJayModule(CJayApplication app) {
		this.app = app;
	}

	@Provides
	@Singleton
	public Application provideApplication() {
		return app;
	}

}
