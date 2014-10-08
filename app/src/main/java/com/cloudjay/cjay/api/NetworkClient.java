package com.cloudjay.cjay.api;


import android.content.Context;
import android.text.TextUtils;

import com.cloudjay.cjay.model.IsoCode;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.PreferencesUtil;
import com.cloudjay.cjay.util.StringHelper;
import com.cloudjay.cjay.util.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.Trace;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

@EBean(scope = EBean.Scope.Singleton)
public class NetworkClient {

	@Bean
	RestAdapterProvider provider = new RestAdapterProvider();

	Context context;

	public NetworkClient(Context context) {
		this.context = context;
	}

	//region User
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
	//endregion

	public void uploadImage(Context context) {
		RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(ApiEndpoint.CJAY_TMP_STORAGE).build();
		File image = new File("storage/sdcard0/DCIM/CJay/DemoDepotCode/2014-10-06/gate-in/ContainerId/DemoDepotCode-2014-10-06-DemoimageType-ContainerId-DemoOperatorCode-ee58e92d-77c0-493c-9a33-2de38a626bd7.jpg");
		TypedFile typedFile = new TypedFile("image/png", image);
		restAdapter.create(NetworkService.class).postImageFile("image/jpeg", "media", "DemoDepotCode-2014-10-06-DemoimageType-ContainerId-DemoOperatorCode-ee58e92d-77c0-493c-9a33-2de38a626bd7.jpg", typedFile, new Callback<Response>() {
			@Override
			public void success(Response response, Response response2) {

				Logger.e(response.getUrl());
				Logger.e(response.getHeaders().toString());
			}

			@Override
			public void failure(RetrofitError error) {
				Logger.e(error.toString());
			}
		});
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

	public Session getContainerSessionById(int id) {
		JsonObject result = provider.getRestAdapter(context).create(NetworkService.class).getContainerSessionById(id);
		if (id != result.get("id").getAsLong()) {
			return null;
		} else {
			return Utils.parseSession(context, result);
		}
	}

	@Trace(tag = "NetworkClient")
	public List<Session> getAllSessions(Context context, String modifiedDate) {

		List<Session> sessions = new ArrayList<Session>();
		JsonElement next;

		// If get by day, get fist page query by day
		// => get page form key "next"
		// => get all page after that page
		if (!TextUtils.isEmpty(modifiedDate)) {

			// Get by lastModified Day
			Logger.Log("Fetch data by time");

			JsonObject jsonObject = provider.getRestAdapter(context).create(NetworkService.class).getContainerSessionsByModifiedTime(modifiedDate);
			next = jsonObject.get("next");
			String nextString = next.toString();

			//get page number of fist page
			int page = Integer.parseInt(nextString.substring(nextString.lastIndexOf("=") + 1));
			Logger.e(String.valueOf(page));

			//get all session have page number greater then fist page
			List<Session> sessionsByPage = this.getAllSessionsByPage(context, page);
			sessions.addAll(sessionsByPage);

			// Update Modified day in preferences
			PreferencesUtil.storePrefsValue(context, PreferencesUtil.PREF_MODIFIED_DATE, StringHelper.getCurrentTimestamp(CJayConstant.DAY_TIME_FORMAT));

			//Get session fist page when query by day
			String lastModifiedDate = PreferencesUtil.getPrefsValue(context, PreferencesUtil.PREF_MODIFIED_DATE);
			Logger.Log("Last modified date: " + lastModifiedDate);
		}

		//If not, get all sessions start form page 1
		else {
			Logger.Log("Fetching all page");
			sessions = getAllSessionsByPage(context, 1);
		}

		return sessions;
	}

	public List<Session> getAllSessionsByPage(Context context, int page) {
		List<Session> sessions = new ArrayList<Session>();
		JsonElement next;

		do {
			JsonObject jsonObject = provider.getRestAdapter(context).create(NetworkService.class).getContainerSessionsByPage(page, null);
			JsonArray jsonArray = jsonObject.getAsJsonArray("results");
			next = jsonObject.get("next");

			for (JsonElement e : jsonArray) {
				sessions.add(Utils.parseSession(context, e.getAsJsonObject()));
			}
			page = page + 1;

		} while (!next.isJsonNull());

		//
		PreferencesUtil.storePrefsValue(context, PreferencesUtil.PREF_MODIFIED_DATE, StringHelper.getCurrentTimestamp(CJayConstant.DAY_TIME_FORMAT));
		return sessions;
	}

	public List<Session> searchSessions(Context context, String keyword) {
		return null;
	}

	public void uploadContainerSession(Context context, Session containerSession) {
		provider.getRestAdapter(context).create(NetworkService.class).postContainer("", "");
	}

}
