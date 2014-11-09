package com.cloudjay.cjay.api;


import android.content.Context;
import android.text.TextUtils;

import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.model.IsoCode;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.PreferencesUtil;
import com.cloudjay.cjay.util.enums.Step;
import com.cloudjay.cjay.util.enums.UploadStatus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

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
		imageFile.exists();
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
	public Session getSessionById(long id) {
		Session result = provider.getRestAdapter(context).create(NetworkService.class).getContainerSessionById(id);
		if (id != result.getId()) {
			return null;
		} else {
			return result;
		}
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
	public List<Session> getSessionByPage(Context context, int page, String modifiedDate) {
		List<Session> sessions = new ArrayList<>();

		JsonObject jsonObject = provider.getRestAdapter(context).create(NetworkService.class).getContainerSessionsByPage(page, modifiedDate);

		// Parse list sessions nằm bên trong key `results` của kết quả trả về
		JsonArray results = jsonObject.getAsJsonArray("results");
		JsonElement next = jsonObject.get("next");

		Gson gson = new Gson();
		Type listType = new TypeToken<List<Session>>() {
		}.getType();

		List<Session> sessionsPage = gson.fromJson(results, listType);
		sessions.addAll(sessionsPage);

		if (!next.isJsonNull()) {
			//Store first page modified_since to retry immediately after fetched
			if (page == 1) {
				String firstPageTime = jsonObject.get("request_time").getAsString();
				PreferencesUtil.storePrefsValue(context, PreferencesUtil.PREF_FIRST_PAGE_MODIFIED_DATE, firstPageTime);
			}

			//Update current page
			String currentPage = String.valueOf(page);
			PreferencesUtil.storePrefsValue(context, PreferencesUtil.PREF_MODIFIED_PAGE, currentPage);

		} else {
			// Store the datetime that complete parsing process
			String currentTime = jsonObject.get("request_time").getAsString();
			PreferencesUtil.storePrefsValue(context, PreferencesUtil.PREF_MODIFIED_DATE, currentTime);
		}

		return sessions;
	}

	public List<Session> searchSessions(Context context, String keyword) {

		List<Session> sessions = new ArrayList<>();
		JsonObject jsonObject = provider.getRestAdapter(context).create(NetworkService.class).searchContainer(keyword);
		JsonArray results = jsonObject.getAsJsonArray("results");
		Gson gson = new Gson();
		Type listType = new TypeToken<List<Session>>() {
		}.getType();
		List<Session> sessionsPage = gson.fromJson(results, listType);
		sessions.addAll(sessionsPage);

		return sessions;
	}

	/**
	 * Upload container Session
	 *
	 * @param context
	 * @param session
	 * @return
	 */
	public Session uploadSession(Context context, Session session) {

		Logger.Log("Session: " + session.getJsonSession().toString());
		Session result = provider.getRestAdapter(context).create(NetworkService.class).postContainer(session.getJsonSession());
		return result;
	}

	/**
	 * check out container Session
	 *
	 * @param context
	 * @param containerSession
	 * @return
	 */
	public Session checkOutContainerSession(Context context, Session containerSession) {

		try {
			Session checkOutSession = provider.getRestAdapter(context).create(NetworkService.class).checkOutContainerSession(containerSession.getId(), containerSession.getGateOutImageToUpLoad());
			return checkOutSession;
		} catch (RetrofitError e) {
			Logger.Log(e.getResponse().getReason().toString());
			Logger.Log(e.getResponse().getBody().toString());
			Logger.Log(e.getResponse().getStatus() + "");
		}

		return null;
	}

	/**
	 * put container id to server to note that this session comoleted audit
	 *
	 * @param context
	 * @param containerSession
	 * @return
	 */
	public Session completeAudit(Context context, Session containerSession) {

		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();

		Logger.Log("containerSession: " + gson.toJson(containerSession));

		Session completeAuditSession = provider.getRestAdapter(context).create(NetworkService.class).completeAudit(containerSession.getId());
		return completeAuditSession;
	}

	/**
	 * put to repaired session to server
	 *
	 * @param context
	 * @param containerSession
	 * @return
	 */
	public Session completeRepairSession(Context context, Session containerSession) {
		Logger.e(containerSession.getRepairedAuditItemToUpLoad().toString());
		Session completeRepairSession = provider.getRestAdapter(context).create(NetworkService.class).completeRepair(containerSession.getId(), containerSession.getRepairedAuditItemToUpLoad());
		return completeRepairSession;
	}

	/**
	 * put audit item to server
	 *
	 * @param context
	 * @param containerSession
	 * @param auditItem
	 * @return
	 */
	public Session postAuditItem(Context context, Session containerSession, AuditItem auditItem) {

		Logger.Log("containerSession: " + containerSession.getId());
		Logger.Log("auditItem: " + auditItem.getAuditItemToUpload());

		String auditItemUUID = auditItem.getUuid();
		Session result = provider.getRestAdapter(context).create(NetworkService.class).postAudiItem(containerSession.getId(), auditItem.getAuditItemToUpload());
		List<AuditItem> list = result.getAuditItems();
		for (AuditItem item : list) {
			if (item.equals(auditItem)) {
				Logger.Log("Set here");
				item.setUuid(auditItemUUID);
				item.setUploadStatus(UploadStatus.COMPLETE);
			}
		}
		result.setAuditItems(list);
		return result;
	}

	/**
	 * put audit images look like [{'name': 'XXX'}, {'name: 'AAA'}, ...] to add to audit item
	 *
	 * @param context
	 * @param auditItem
	 * @return
	 */
	public AuditItem addAuditImage(Context context, AuditItem auditItem) {

		String uuid = auditItem.getUuid();
		AuditItem result = provider.getRestAdapter(context).create(NetworkService.class).addAuditImages(String.valueOf(auditItem.getId()), auditItem.getAuditImagesToUpLoad());
		result.setUuid(uuid);
		return result;
	}

	/**
	 * Set session is hand cleaning
	 *
	 * @param context
	 * @param session
	 * @return
	 */
	public Session setHandCleaningSession(Context context, Session session) {
		Session handCleaningSession = provider.getRestAdapter(context).create(NetworkService.class).setHandCleaningSession(session.getId());
		return handCleaningSession;
	}


	/**
	 * Get audit item through id by pubnub
	 *
	 * @param id
	 */
	public AuditItem getAuditItemById(long id) {
		AuditItem auditItem = provider.getRestAdapter(context).create(NetworkService.class).getAuditItemById(id);
		return auditItem;
	}

	/**
	 * Get damageCode through id by pubnub
	 *
	 * @param id
	 */
	public IsoCode getDamageCodeById(long id) {
		IsoCode damageCode = provider.getRestAdapter(context).create(NetworkService.class).getDamageCodesById(id);
		return damageCode;
	}

	/**
	 * Get repairCode through id by pubnub
	 *
	 * @param id
	 */
	public IsoCode getRepairCodeById(long id) {
		IsoCode repairCode = provider.getRestAdapter(context).create(NetworkService.class).getRepairCodeById(id);
		return repairCode;
	}

	/**
	 * Get componetCode through id by pubnub
	 *
	 * @param id
	 */
	public IsoCode getComponentCodeById(long id) {
		IsoCode componentCode = provider.getRestAdapter(context).create(NetworkService.class).getComponentCodeById(id);
		return componentCode;
	}

	/**
	 * Get operator through id by pubnub
	 *
	 * @param id
	 */
	public Operator getOperatorById(long id) {
		Operator operator = provider.getRestAdapter(context).create(NetworkService.class).getOperatorById(id);
		return operator;
	}

	/**
	 * Notify to server that we already receive notification message
	 *
	 * @param channel
	 * @param messageId
	 */
	public void gotMessageFromPubNub(String channel, String messageId) {

		Response response = provider.getRestAdapter(context).create(NetworkService.class).gotMessageFromPubNub(channel, messageId);

//		Logger.Log(response.getHeaders().toString());
//		Logger.Log(response.getBody().toString());
	}

}
