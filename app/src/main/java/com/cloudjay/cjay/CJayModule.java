package com.cloudjay.cjay;

import android.app.Application;

import com.cloudjay.cjay.data.DataModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

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
	Application provideApplication() {
		return app;
	}

}
