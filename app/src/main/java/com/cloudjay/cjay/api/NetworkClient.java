package com.cloudjay.cjay.api;


import android.content.Context;

import com.cloudjay.cjay.model.IsoCode;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.model.User;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import retrofit.RetrofitError;

@EBean(scope = EBean.Scope.Singleton)
public class NetworkClient {

	@Bean
	RestAdapterProvider provider = new RestAdapterProvider();

	Context context;

	public NetworkClient(Context context) {
		this.context = context;
	}

	public String getToken(String username, String password) throws RetrofitError {

		JsonObject tokenJson;
		try {
			tokenJson = provider.getRestAdapter(context).create(NetworkService.class).getToken(username, password);
		} catch (RetrofitError e) {
			throw e;
		}

		String token = tokenJson.get("token").getAsString();
		return token;
	}

	/**
	 * Get current logged in user information
	 *
	 * @param context
	 * @return
	 */
	public User getCurrentUser(Context context) {
		JsonObject result = provider.getRestAdapter(context).create(NetworkService.class).getCurrentUser();

		// WTF is this shit :| Fuck Realm
		Realm realm = Realm.getInstance(context);
		realm.beginTransaction();
		User user = realm.createObject(User.class);
		user.setId(result.get("id").getAsLong());
		user.setFirstName(result.get("first_name").getAsString());
		user.setLastName(result.get("last_name").getAsString());
		user.setUsername(result.get("username").getAsString());
		user.setEmail(result.get("email").getAsString());
		user.setFullName(result.get("full_name").getAsString());
		user.setRole(result.get("role").getAsLong());
		user.setRoleName(result.get("role_name").getAsString());
		user.setDepotCode(result.get("depot_code").getAsString());
		user.setAvatarUrl(result.get("avatar_url").getAsString());
		realm.commitTransaction();

		return user;
	}

	/**
	 * Get repair iso codes based on lastModifiedDate
	 *
	 * @param context
	 * @param lastModifiedDate
	 * @return
	 */
	public List<IsoCode> getRepairCodes(Context context, String lastModifiedDate) {

		JsonArray results = provider.getRestAdapter(context).create(NetworkService.class).getRepairCodes
				(lastModifiedDate);

		List<IsoCode> items = new ArrayList<IsoCode>();

		// Store the retrieved items to the Realm
		Realm realm = Realm.getInstance(context);

		// Open a transaction to store items into the realm
		realm.beginTransaction();
		for (JsonElement e : results) {

			// Create a realm capable object
			IsoCode code = realm.createObject(IsoCode.class);
			code.setId(e.getAsJsonObject().get("id").getAsLong());
			code.setCode(e.getAsJsonObject().get("code").getAsString());
			code.setFullName(e.getAsJsonObject().get("full_name").getAsString());
			items.add(code);
		}
		realm.commitTransaction();

		return items;
	}

	/**
	 * Get damage iso codes based on lastModifiedDate
	 *
	 * @param context
	 * @param lastModifiedDate
	 * @return
	 */
	public List<IsoCode> getDamageCodes(Context context, String lastModifiedDate) {
		JsonArray results = provider.getRestAdapter(context).create(NetworkService.class).getDamageCodes
				(lastModifiedDate);

		List<IsoCode> items = new ArrayList<IsoCode>();

		// Store the retrieved items to the Realm
		Realm realm = Realm.getInstance(context);

		// Open a transaction to store items into the realm
		realm.beginTransaction();
		for (JsonElement e : results) {

			// Create a realm capable object
			IsoCode code = realm.createObject(IsoCode.class);
			code.setId(e.getAsJsonObject().get("id").getAsLong());
			code.setCode(e.getAsJsonObject().get("code").getAsString());
			code.setFullName(e.getAsJsonObject().get("full_name").getAsString());
			items.add(code);
		}
		realm.commitTransaction();

		return items;
	}

	/**
	 * Get component iso codes based on lastModifiedDate
	 *
	 * @param context
	 * @param lastModifiedDate
	 * @return
	 */
	public List<IsoCode> getComponentCodes(Context context, String lastModifiedDate) {
		JsonArray results = provider.getRestAdapter(context).create(NetworkService.class).getComponentCodes(lastModifiedDate);

		List<IsoCode> items = new ArrayList<IsoCode>();

		// Store the retrieved items to the Realm
		Realm realm = Realm.getInstance(context);

		// Open a transaction to store items into the realm
		realm.beginTransaction();
		for (JsonElement e : results) {

			// Create a realm capable object
			IsoCode code = realm.createObject(IsoCode.class);
			code.setId(e.getAsJsonObject().get("id").getAsLong());
			code.setCode(e.getAsJsonObject().get("code").getAsString());
			code.setFullName(e.getAsJsonObject().get("full_name").getAsString());
			items.add(code);
		}
		realm.commitTransaction();

		return items;
	}

	/**
	 * Get operator based on lastModifiedDate.
	 *
	 * @param context
	 * @param lastModifiedDate
	 * @return
	 */
	public List<Operator> getOperators(Context context, String lastModifiedDate) {

		JsonArray results = provider.getRestAdapter(context).create(NetworkService.class).getOperators(lastModifiedDate);
		List<Operator> items = new ArrayList<Operator>();

//		// Clear the realm from last time
//		Realm.deleteRealmFile(context);

		// Store the retrieved items to the Realm
		Realm realm = Realm.getInstance(context);

		// Open a transaction to store items into the realm
		realm.beginTransaction();
		for (JsonElement e : results) {

			// Create a realm capable object
			Operator code = realm.createObject(Operator.class);
			code.setId(e.getAsJsonObject().get("id").getAsLong());
			code.setOperatorCode(e.getAsJsonObject().get("operator_code").getAsString());
			code.setOperatorName(e.getAsJsonObject().get("operator_name").getAsString());
			items.add(code);
		}
		realm.commitTransaction();

		return items;
	}

	/**
	 * @param page
	 * @param lastModifiedDate
	 * @return
	 */
	public List<Session> getContainerSessionsByPage(int page, String lastModifiedDate) {

		JsonObject jsonObject = provider.getRestAdapter(context).create(NetworkService.class).getContainerSessionsByPage(page, lastModifiedDate);
		JsonArray jsonArray = jsonObject.getAsJsonArray("results");
		Gson gson = new Gson();
		Type listType = new TypeToken<List<Session>>() {
		}.getType();
		List<Session> containerSessionsByPage = gson.fromJson(jsonArray.toString(), listType);
		return containerSessionsByPage;
	}

	public Session getContainerSessionById(int id) {

		JsonObject result = provider.getRestAdapter(context).create(NetworkService.class).getContainerSessionById(id);

		// check if result has values
		if (id != result.get("id").getAsLong()) {
			return null;
		} else {
			// WTF is this shit :| Fuck Realm
			Realm realm = Realm.getInstance(context);
			realm.beginTransaction();
			Session session = realm.createObject(Session.class);

			User user = realm.createObject(User.class);
			user.setId(result.get("id").getAsLong());
			user.setFirstName(result.get("first_name").getAsString());
			user.setLastName(result.get("last_name").getAsString());
			user.setUsername(result.get("username").getAsString());
			user.setEmail(result.get("email").getAsString());
			user.setFullName(result.get("full_name").getAsString());
			user.setRole(result.get("role").getAsLong());
			user.setRoleName(result.get("role_name").getAsString());
			user.setDepotCode(result.get("depot_code").getAsString());
			user.setAvatarUrl(result.get("avatar_url").getAsString());

			realm.commitTransaction();
			return session;
		}
	}

	public void getAllSessions(Context context) {
	}

	public List<Session> searchSessions(Context context, String keyword) {
		return null;
	}
}
