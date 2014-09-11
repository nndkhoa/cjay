package com.cloudjay.cjay.network;

import android.util.Log;

import com.cloudjay.cjay.event.LoginSuccessEvent;
import com.cloudjay.cjay.util.ApiConfiguration;
import com.google.gson.JsonObject;
import com.squareup.okhttp.OkHttpClient;

import de.greenrobot.event.EventBus;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Created by Thai on 9/11/2014.
 */
public class NetworkClient {

	private static NetworkClient INSTANCE;

	public static NetworkClient getInstance() {
		if (INSTANCE == null)
			INSTANCE = new NetworkClient();

		return INSTANCE;
	}

	interface CJayService {

		@FormUrlEncoded
		@POST(ApiConfiguration.TOKEN)
		public void getToken(@Field("username") String username, @Field("password") String password, Callback<JsonObject> callback);


	}

	public String getToken(String username, String password) {
		Log.i("Running;","aasdasdsadsadsadsadsadsa");
		OkHttpClient okHttpClient = new OkHttpClient();
		RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(ApiConfiguration.ROOT).setClient(new
				OkClient(okHttpClient)).build();
		CJayService cJayService = restAdapter.create(CJayService.class);

		cJayService.getToken(username, password, new Callback<JsonObject>() {

			@Override
			public void success(JsonObject jsonObject, Response response) {
				EventBus.getDefault().post(new LoginSuccessEvent());
			}

			@Override
			public void failure(RetrofitError error) {
				Log.e("TAG", error.getMessage());
			}
		});

		return null;
	}


}
