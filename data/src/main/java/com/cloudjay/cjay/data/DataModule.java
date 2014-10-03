package com.cloudjay.cjay.data;

import android.app.Application;

import com.cloudjay.cjay.data.api.ApiModule;
import com.squareup.okhttp.OkHttpClient;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
		includes = ApiModule.class,
		complete = false,
		library = true
)
public class DataModule {

	static OkHttpClient createOkHttpClient(Application app) {
		OkHttpClient client = new OkHttpClient();
		return client;
	}

	@Provides
	@Singleton
	OkHttpClient provideOkHttpClient(Application app) {
		return createOkHttpClient(app);
	}

}
