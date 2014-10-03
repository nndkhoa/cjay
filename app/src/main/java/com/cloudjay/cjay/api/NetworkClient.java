package com.cloudjay.cjay.api;


import com.cloudjay.cjay.model.IsoCode;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.model.User;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import retrofit.RetrofitError;

public class NetworkClient {

	static NetworkClient INSTANCE;
	public static NetworkClient getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new NetworkClient();
		}
		return INSTANCE;
	}

	public NetworkClient() {
	}

	CustomRestAdapter restAdapter;
	NetworkService cJayService;

	void init() {
		cJayService = restAdapter.getRestAdapter(ApiEndpoint.ROOT_API).create(NetworkService.class);
	}

	public String getToken(String username, String password) throws RetrofitError {
		JsonObject tokenJson = null;
		try {
			tokenJson = cJayService.getToken(username, password);
		} catch (RetrofitError e) {
			throw e;
		}

		String token = tokenJson.get("token").getAsString();
		return token;
	}

	public User getCurrentUser(String token) {
		User user = cJayService.getCurrentUser();
		return user;
	}

	public List<IsoCode> getRepairCodes(String lastModifiedDate) {
		List<IsoCode> repairCodes = cJayService.getRepairCodes(lastModifiedDate);
		return repairCodes;
	}

	public List<IsoCode> getDamageCodes(String lastModifiedDate) {
		List<IsoCode> damageCodes = cJayService.getDamageCodes(lastModifiedDate);
		return damageCodes;
	}

	public List<IsoCode> getComponentCodes(String lastModifiedDate) {
		List<IsoCode> componentCodes = cJayService.getComponentCodes(lastModifiedDate);
		return componentCodes;
	}

	public List<Operator> getOperators(String lastModifiedDate) {
		List<Operator> operators = cJayService.getOperators(lastModifiedDate);
		return operators;
	}

	public List<Session> getContainerSessionsByPage(int page, String lastModifiedDate) {
		JsonObject jsonObject = cJayService.getContainerSessionsByPage(page, lastModifiedDate);
		JsonArray jsonArray = jsonObject.getAsJsonArray("results");
		Gson gson = new Gson();
		Type listType = new TypeToken<List<Session>>() {
		}.getType();
		List<Session> containerSessionsByPage = gson.fromJson(jsonArray.toString(), listType);
		return containerSessionsByPage;
	}

	public Session getContainerSessionById(int id) {
		Session containerSessionById = cJayService.getContainerSessionById(id);
		return containerSessionById;
	}
}
