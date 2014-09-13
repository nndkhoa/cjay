package com.cloudjay.cjay.network;

import android.content.Context;

import com.cloudjay.cjay.util.ApiEndpoint;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.squareup.okhttp.OkHttpClient;

import retrofit.RestAdapter;
import retrofit.client.OkClient;

public class NetworkClient {
	private static NetworkClient INSTANCE;

	public static NetworkClient getInstance() {
		if (INSTANCE == null)
			INSTANCE = new NetworkClient();
		return INSTANCE;
	}

	public NetworkClient() {
		super();

	}

	private void createClient() {

	}


	public String getToken(Context context, String username, String password) {
		OkHttpClient okHttpClient = new OkHttpClient();
		RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(ApiEndpoint.ROOT_API).setClient(new
				OkClient(okHttpClient)).build();
		NetworkService cJayService = restAdapter.create(NetworkService.class);
		JsonObject tokenJson = cJayService.getToken(username, password);
		String token = tokenJson.get("token").getAsString();
		return token;
	}


	public String getCurrentUser(Context context, String token) {
		OkHttpClient okHttpClient = new OkHttpClient();
		RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(ApiEndpoint.ROOT_API).setClient(new
				OkClient(okHttpClient)).build();
		NetworkService cJayService = restAdapter.create(NetworkService.class);
		String cJayVersion = Utils.getAppVersionName(context);
		JsonObject userJson = cJayService.getCurrentUser(token, cJayVersion);
		return userJson.toString();

	}

	public String getRepairCodes(Context context, String token, String lastModifiedDate) {
		OkHttpClient okHttpClient = new OkHttpClient();
		RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(ApiEndpoint.ROOT_API).setClient(new
				OkClient(okHttpClient)).build();
		NetworkService cJayService = restAdapter.create(NetworkService.class);
		String cJayVersion = Utils.getAppVersionName(context);
		JsonArray repairCodes = cJayService.getRepairCodes(token, cJayVersion, lastModifiedDate);
		return repairCodes.toString();
	}

	public String getDamageCodes(Context context, String token, String lastModifiedDate) {
		OkHttpClient okHttpClient = new OkHttpClient();
		RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(ApiEndpoint.ROOT_API).setClient(new
				OkClient(okHttpClient)).build();
		NetworkService cJayService = restAdapter.create(NetworkService.class);
		String cJayVersion = Utils.getAppVersionName(context);
		JsonArray repairCodes = cJayService.getDamageCodes(token, cJayVersion, lastModifiedDate);
		return repairCodes.toString();
	}

	public String getComponentCodes(Context context, String token, String lastModifiedDate) {
		OkHttpClient okHttpClient = new OkHttpClient();
		RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(ApiEndpoint.ROOT_API).setClient(new
				OkClient(okHttpClient)).build();
		NetworkService cJayService = restAdapter.create(NetworkService.class);
		String cJayVersion = Utils.getAppVersionName(context);
		JsonArray repairCodes = cJayService.getComponentCodes(token, cJayVersion, lastModifiedDate);
		return repairCodes.toString();
	}

	public String getOperators(Context context, String token, String lastModifiedDate) {
		OkHttpClient okHttpClient = new OkHttpClient();
		RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(ApiEndpoint.ROOT_API).setClient(new
				OkClient(okHttpClient)).build();
		NetworkService cJayService = restAdapter.create(NetworkService.class);
		String cJayVersion = Utils.getAppVersionName(context);
		JsonArray repairCodes = cJayService.getOperators(token, cJayVersion, lastModifiedDate);
		return repairCodes.toString();
	}

	public String getContainerSessionsByPage(Context context, String token,int page, String lastModifiedDate) {
		OkHttpClient okHttpClient = new OkHttpClient();
		RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(ApiEndpoint.ROOT_API).setClient(new
				OkClient(okHttpClient)).build();
		NetworkService cJayService = restAdapter.create(NetworkService.class);
		String cJayVersion = Utils.getAppVersionName(context);
		JsonArray repairCodes = cJayService.getContainerSessionsByPage(token, cJayVersion,page, lastModifiedDate);
		return repairCodes.toString();
	}
}
