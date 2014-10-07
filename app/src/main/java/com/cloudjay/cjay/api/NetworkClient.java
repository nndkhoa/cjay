package com.cloudjay.cjay.api;


import android.content.Context;

import com.cloudjay.cjay.model.AuditImage;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.model.GateImage;
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

import java.io.File;
import java.lang.reflect.Type;
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

	public void uploadContainerSession(Context context, Session containerSession) {
		provider.getRestAdapter(context).create(NetworkService.class).postContainer("", "");
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

    public List<Session> getAllSessions(Context context, boolean isGetByModifiedDay) {
        List<Session> sessions = new ArrayList<Session>();
        JsonElement next;
        //If get by day, get fist page query by day=>get page form key "next"=>get all page after that page
        if (isGetByModifiedDay) {
            Logger.e("Fetch by time");
            //Get session fist page when query by day
            String lastModifiedDate = PreferencesUtil.getPrefsValue(context, PreferencesUtil.PREF_MODIFIED_DATE);
            Logger.e(lastModifiedDate);
            //If don't have lastmodifiedDay fetch all by day
            if (lastModifiedDate.isEmpty()) {
                sessions = getAllSessionsByPage(context, 1);
            } else {
                //Get by lastModifiedDay
                JsonObject jsonObject = provider.getRestAdapter(context).create(NetworkService.class).getContainerSessionsByModifiedTime(lastModifiedDate);
                next = jsonObject.get("next");
                String nextString = next.toString();
                //get page number of fist page
                int page = Integer.parseInt(nextString.substring(nextString.lastIndexOf("=") + 1));
                Logger.e(String.valueOf(page));
                //get all session have page number greater then fist page
                List<Session> sessionsByPage = this.getAllSessionsByPage(context, page);
                sessions.addAll(sessionsByPage);
                //Update Modified day in preferences
                PreferencesUtil.storePrefsValue(context, PreferencesUtil.PREF_MODIFIED_DATE, StringHelper.getCurrentTimestamp(CJayConstant.DAY_TIME_FORMAT));
            }

        }
        //If not, get all sessions start form page 1
        else {
            Logger.e("Fetching all page");
            sessions = getAllSessionsByPage(context, 1);
        }

        return sessions;
    }

    public List<Session> getAllSessionsByPage(Context context, int page) {
        List<Session> sessions = new ArrayList<Session>();
        JsonElement next;
        //TODO: set nullable to some fill
        do {
            Logger.e("Running do while");
            JsonObject jsonObject = provider.getRestAdapter(context).create(NetworkService.class).getContainerSessionsByPage(page, null);
            JsonArray jsonArray = jsonObject.getAsJsonArray("results");
            next = jsonObject.get("next");
            Logger.e(next.toString());
            for (JsonElement e : jsonArray) {
                Realm realm = Realm.getInstance(context);
                realm.beginTransaction();
                Session session = realm.createObject(Session.class);
                Logger.e("Adding session " + e.getAsJsonObject().get("id").toString() + " " + e.getAsJsonObject().get("container_id").toString());

                session.setId(Long.parseLong(e.getAsJsonObject().get("id").toString()));
                session.setContainerId(e.getAsJsonObject().get("container_id").toString());
                session.setCheckInTime(e.getAsJsonObject().get("check_in_time").toString());
                session.setCheckOutTime(e.getAsJsonObject().get("check_out_time").toString());
                session.setDepotCode(e.getAsJsonObject().get("depot_code").toString());
                session.setDepotId(Long.parseLong(e.getAsJsonObject().get("depot_id").toString()));
                session.setOperatorCode(e.getAsJsonObject().get("container_id").toString());
                session.setOperatorId(Long.parseLong(e.getAsJsonObject().get("operator_id").toString()));
                session.setPreStatus(Long.parseLong(e.getAsJsonObject().get("pre_status").toString()));
                session.setStatus(Long.parseLong(e.getAsJsonObject().get("status").toString()));
                session.setStep(Long.parseLong(e.getAsJsonObject().get("step").toString()));

                Logger.e("Added Session");
                JsonArray auditItems = e.getAsJsonObject().getAsJsonArray("audit_items");
                for (JsonElement audit : auditItems) {
                    AuditItem item = realm.createObject(AuditItem.class);

                    item.setComponentCode(audit.getAsJsonObject().get("component_code").toString());
                    item.setComponentCodeId(Long.parseLong(audit.getAsJsonObject().get("component_code_id").toString()));
                    item.setComponentName(audit.getAsJsonObject().get("component_name").toString());
                    item.setCreatedAt(audit.getAsJsonObject().get("created_at").toString());
                    item.setDamageCode(audit.getAsJsonObject().get("damage_code").toString());
                    item.setDamageCodeId(Long.parseLong(audit.getAsJsonObject().get("damage_code_id").toString()));
                    if (audit.getAsJsonObject().get("height").isJsonNull()) {
                        item.setHeight(0);
                    } else {
                        item.setHeight(Double.valueOf(audit.getAsJsonObject().get("height").toString()));
                    }

                    if (audit.getAsJsonObject().get("length").isJsonNull()) {
                        item.setHeight(0);
                    } else {
                        item.setHeight(Double.valueOf(audit.getAsJsonObject().get("length").toString()));
                    }

                    item.setId(Long.parseLong(audit.getAsJsonObject().get("id").toString()));
                    item.setIsAllowed(Boolean.parseBoolean(audit.getAsJsonObject().get("is_allowed").toString()));
                    item.setLocationCode(audit.getAsJsonObject().get("location_code").toString());
                    item.setModifiedAt(audit.getAsJsonObject().get("modified_at").toString());
                    if (audit.getAsJsonObject().get("quantity").isJsonNull()) {
                        item.setQuantity(0);
                    } else {
                        item.setQuantity(Long.valueOf(audit.getAsJsonObject().get("quantity").toString()));
                    }

                    item.setRepairCode(audit.getAsJsonObject().get("repair_code").toString());
                    item.setRepairCodeId(Long.parseLong(audit.getAsJsonObject().get("repair_code_id").toString()));

                    Logger.e("Adding auditItems");

                    JsonArray auditImage = audit.getAsJsonObject().getAsJsonArray("audit_images");
                    for (JsonElement imageAudit : auditImage) {
                        AuditImage imageAuditItem = realm.createObject(AuditImage.class);

                        imageAuditItem.setId(Long.parseLong(imageAudit.getAsJsonObject().get("id").toString()));
                        imageAuditItem.setType(Long.parseLong(imageAudit.getAsJsonObject().get("type").toString()));
                        imageAuditItem.setUrl(imageAudit.getAsJsonObject().get("url").toString());

                        Logger.e("Adding auditImage");
                    }
                }
                JsonArray gateImage = e.getAsJsonObject().getAsJsonArray("gate_images");
                for (JsonElement image : gateImage) {
                    GateImage imageItem = realm.createObject(GateImage.class);

                    imageItem.setId(Long.parseLong(image.getAsJsonObject().get("id").toString()));
                    imageItem.setType(Long.parseLong(image.getAsJsonObject().get("type").toString()));
                    imageItem.setUrl(image.getAsJsonObject().get("url").toString());

                    Logger.e("Adding gateImage");
                }
                sessions.add(session);
                realm.commitTransaction();
            }

            page = page + 1;
            Logger.e("Loading page: " + String.valueOf(page));

        } while (!next.isJsonNull());
        PreferencesUtil.storePrefsValue(context, PreferencesUtil.PREF_MODIFIED_DATE, StringHelper.getCurrentTimestamp(CJayConstant.DAY_TIME_FORMAT));
        return sessions;
    }

    public List<Session> searchSessions(Context context, String keyword) {
        return null;
    }


}
