package com.cloudjay.cjay.network;

import android.content.Context;

import com.cloudjay.cjay.model.IsoCode;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.util.ApiEndpoint;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.squareup.okhttp.OkHttpClient;

import java.util.List;
import java.util.logging.Level;

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


	public User getCurrentUser(Context context, String token) {
		OkHttpClient okHttpClient = new OkHttpClient();
		RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(ApiEndpoint.ROOT_API).setClient(new
				OkClient(okHttpClient)).build();
		NetworkService cJayService = restAdapter.create(NetworkService.class);
		String cJayVersion = Utils.getAppVersionName(context);
		User user = cJayService.getCurrentUser(token, cJayVersion);

		return user;

	}

	public List<IsoCode> getRepairCodes(Context context, String token, String lastModifiedDate) {
		OkHttpClient okHttpClient = new OkHttpClient();
		RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(ApiEndpoint.ROOT_API).setClient(new
				OkClient(okHttpClient)).build();
		NetworkService cJayService = restAdapter.create(NetworkService.class);
		String cJayVersion = Utils.getAppVersionName(context);
        List<IsoCode>  repairCodes = cJayService.getRepairCodes(token, cJayVersion, lastModifiedDate);
        Logger.e(repairCodes.get(0).display_name);
		return repairCodes;
	}

	public List<IsoCode> getDamageCodes(Context context, String token, String lastModifiedDate) {
		OkHttpClient okHttpClient = new OkHttpClient();
		RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(ApiEndpoint.ROOT_API).setClient(new
				OkClient(okHttpClient)).build();
		NetworkService cJayService = restAdapter.create(NetworkService.class);
		String cJayVersion = Utils.getAppVersionName(context);
        List<IsoCode> damageCodes = cJayService.getDamageCodes(token, cJayVersion, lastModifiedDate);
        Logger.e(damageCodes.get(0).display_name);
		return damageCodes;
	}

	public List<IsoCode> getComponentCodes(Context context, String token, String lastModifiedDate) {
		OkHttpClient okHttpClient = new OkHttpClient();
		RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(ApiEndpoint.ROOT_API).setClient(new
				OkClient(okHttpClient)).build();
		NetworkService cJayService = restAdapter.create(NetworkService.class);
		String cJayVersion = Utils.getAppVersionName(context);
        List<IsoCode> componentCodes = cJayService.getComponentCodes(token, cJayVersion, lastModifiedDate);
        Logger.e(componentCodes.get(0).display_name);
		return componentCodes;
	}

	public List<Operator> getOperators(Context context, String token, String lastModifiedDate) {
		OkHttpClient okHttpClient = new OkHttpClient();
		RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(ApiEndpoint.ROOT_API).setClient(new
				OkClient(okHttpClient)).build();
		NetworkService cJayService = restAdapter.create(NetworkService.class);
		String cJayVersion = Utils.getAppVersionName(context);
        List<Operator> operators = cJayService.getOperators(token, cJayVersion, lastModifiedDate);
        Logger.e(operators.get(0).Name);
		return operators;
	}

	public List<Session> getContainerSessionsByPage(Context context, String token,int page, String lastModifiedDate) {
		OkHttpClient okHttpClient = new OkHttpClient();
		RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(ApiEndpoint.ROOT_API).setClient(new
				OkClient(okHttpClient)).setLogLevel(RestAdapter.LogLevel.FULL).setLog(new RestAdapter.Log() {
            @Override
            public void log(String message) {
                Logger.e("Get Container: "+message);
            }
        }).build();
		NetworkService cJayService = restAdapter.create(NetworkService.class);
		String cJayVersion = Utils.getAppVersionName(context);
        List<Session> containerSessionsByPage = cJayService.getContainerSessionsByPage(token, cJayVersion, page, lastModifiedDate);
        Logger.e(containerSessionsByPage.get(0).getContainerId());
		return containerSessionsByPage;
	}

    public Session getContainerSessionById(Context context, String token,int id) {
        OkHttpClient okHttpClient = new OkHttpClient();
        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(ApiEndpoint.ROOT_API).setClient(new
                OkClient(okHttpClient)).setLogLevel(RestAdapter.LogLevel.FULL).setLog(new RestAdapter.Log() {
            @Override
            public void log(String message) {
                Logger.e("getContainerSessionById: "+message);
            }
        }).build();
        NetworkService cJayService = restAdapter.create(NetworkService.class);
        String cJayVersion = Utils.getAppVersionName(context);
        Session containerSessionById = cJayService.getContainerSessionById(token, cJayVersion, id);
        Logger.e(containerSessionById.getContainerId());
        return containerSessionById;
    }
}
