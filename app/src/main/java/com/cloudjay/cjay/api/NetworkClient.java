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
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.Trace;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

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

	//region USER
	public String getToken(String username, String password) throws RetrofitError {

		JsonObject tokenJson;
		try {
			tokenJson = provider.getRestAdapter(context).create(NetworkService.class).getToken(username, password);
		} catch (RetrofitError e) {
			throw e;
		}

		String token = tokenJson.get("token").getAsString();
		Logger.Log("User Token: " + token);
		return token;
	}

	/**
	 * Get current logged in user information
	 *
	 * @param context
	 * @return
	 */
	public User getCurrentUser(Context context) {
		User result = provider.getRestAdapter(context).create(NetworkService.class).getCurrentUser();
		return result;
	}
	//endregion

	//region IMAGE

	/**
	 * Upload an image to Google Cloud Storage
	 *
	 * @param uri
	 * @param imageName
	 * @return
	 */
	public Response uploadImage(String uri, String imageName) {

		// Init explicit rest adapter for upload to Google Cloud Storage
		RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(ApiEndpoint.CJAY_TMP_STORAGE).build();
		File imageFile = new File(uri);
		TypedFile typedFile = new TypedFile("image/jpeg", imageFile);

		// Begin to post image
		Response response = restAdapter.create(NetworkService.class).postImageFile("image/jpeg", "media", imageName, typedFile);
		return response;
	}
	//endregion

	//region ISO CODE

	/**
	 * Get repair iso codes based on lastModifiedDate
	 *
	 * @param context
	 * @param lastModifiedDate
	 * @return
	 */
	public List<IsoCode> getRepairCodes(Context context, String lastModifiedDate) {
		List<IsoCode> results = provider.getRestAdapter(context).create(NetworkService.class).getRepairCodes
				(lastModifiedDate);
		return results;
	}

	/**
	 * Get damage iso codes based on lastModifiedDate
	 *
	 * @param context
	 * @param lastModifiedDate
	 * @return
	 */
	public List<IsoCode> getDamageCodes(Context context, String lastModifiedDate) {
		List<IsoCode> results = provider.getRestAdapter(context).create(NetworkService.class).getDamageCodes
				(lastModifiedDate);

		return results;
	}

	/**
	 * Get component iso codes based on lastModifiedDate
	 *
	 * @param context
	 * @param lastModifiedDate
	 * @return
	 */
	public List<IsoCode> getComponentCodes(Context context, String lastModifiedDate) {
		List<IsoCode> results = provider.getRestAdapter(context).create(NetworkService.class).getComponentCodes(lastModifiedDate);

		return results;
	}

	/**
	 * Get operator based on lastModifiedDate.
	 *
	 * @param context
	 * @param lastModifiedDate
	 * @return
	 */
	public List<Operator> getOperators(Context context, String lastModifiedDate) {

		List<Operator> result = provider.getRestAdapter(context).create(NetworkService.class).getOperators(lastModifiedDate);

		return result;
	}
	//endregion

	/**
	 * Get session by id
	 *
	 * @param id
	 * @return
	 */
	public Session getSessionById(int id) {
		Session result = provider.getRestAdapter(context).create(NetworkService.class).getContainerSessionById(id);
		if (id != result.getId()) {
			return null;
		} else {
			return result;
		}
	}

	@Trace(tag = "NetworkClient")
	public List<Session> getAllSessions(Context context, String modifiedDate) {

		List<Session> sessions = new ArrayList<Session>();
		JsonElement next;

		// If get by day, get fist page query by day
		// => get page from key "next"
		// => get all page after that page
		if (!TextUtils.isEmpty(modifiedDate)) {

			// Get by lastModified Day
			Logger.Log("Fetch data by time");

			JsonObject jsonObject = provider.getRestAdapter(context).create(NetworkService.class).getContainerSessionsByModifiedTime(modifiedDate);
			next = jsonObject.get("next");
			String nextString = next.toString();

			//get page number of fist page
			int page = Integer.parseInt(nextString.substring(nextString.lastIndexOf("=") + 1));

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

	/**
	 * Get all container sessions from server with input page.
	 * <p/>
	 * Trong khi còn có thể lấy được kết quả (next != null), thực hiện tăng trang và tiếp tục quá trình.
	 *
	 * @param context
	 * @param page
	 * @return
	 */
	public List<Session> getAllSessionsByPage(Context context, int page) {

		List<Session> sessions = new ArrayList<Session>();
		JsonElement next;
		do {
			JsonObject jsonObject = provider.getRestAdapter(context).create(NetworkService.class).getContainerSessionsByPage(page, null);

			// Parse list sessions nằm bên trong key `results` của kết quả trả về
			JsonArray results = jsonObject.getAsJsonArray("results");
			next = jsonObject.get("next");

			Gson gson = new Gson();
			Type listType = new TypeToken<List<Session>>() {
			}.getType();

			List<Session> sessionsPage = gson.fromJson(results, listType);
			sessions.addAll(sessionsPage);
			page = page + 1;

		} while (!next.isJsonNull());

		// Store the datetime that complete parsing process
		String currentTime = StringHelper.getCurrentTimestamp(CJayConstant.DAY_TIME_FORMAT);
		PreferencesUtil.storePrefsValue(context, PreferencesUtil.PREF_MODIFIED_DATE, currentTime);
		return sessions;
	}

	public List<Session> searchSessions(Context context, String keyword) {

		List<Session> sessions = new ArrayList<Session>();
		JsonObject jsonObject = provider.getRestAdapter(context).create(NetworkService.class).searchContainer(keyword);
		JsonArray results = jsonObject.getAsJsonArray("results");
		Gson gson = new Gson();
		Type listType = new TypeToken<List<Session>>() {
		}.getType();
		List<Session> sessionsPage = gson.fromJson(results, listType);
		sessions.addAll(sessionsPage);

		return sessions;
	}

	public Session uploadContainerSession(Context context, Session containerSession) {
		Logger.e(containerSession.getJsonSession().toString());

		Session uploadedSession = provider.getRestAdapter(context).create(NetworkService.class).postContainer(containerSession.getJsonSession());

		return uploadedSession;
	}
}
