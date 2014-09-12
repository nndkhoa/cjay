package com.cloudjay.cjay.network;

import com.cloudjay.cjay.event.LoginSuccessEvent;
import com.cloudjay.cjay.util.ApiEndpoint;
import com.cloudjay.cjay.util.Logger;
import com.google.gson.JsonObject;
import com.squareup.okhttp.OkHttpClient;

import de.greenrobot.event.EventBus;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;

public class NetworkClient {

	private RestAdapter restAdapter;
	private static NetworkClient INSTANCE;

	public static NetworkClient getInstance() {
		if (INSTANCE == null)
			INSTANCE = new NetworkClient();
		return INSTANCE;
	}

	public NetworkClient() {
		super();
		OkHttpClient okHttpClient = new OkHttpClient();
		restAdapter = new RestAdapter.Builder().setEndpoint(ApiEndpoint.ROOT_API).setClient(new
				OkClient(okHttpClient)).build();
	}

	public void getToken(String username, String password) {

		NetworkService cJayService = restAdapter.create(NetworkService.class);
		cJayService.getToken(username, password, new Callback<JsonObject>() {
			@Override
			public void success(JsonObject jsonObject, Response response) {
				EventBus.getDefault().post(new LoginSuccessEvent());
			}

			@Override
			public void failure(RetrofitError error) {
				Logger.e(error.getMessage());
			}
		});
	}


}
