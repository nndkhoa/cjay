package com.cloudjay.cjay.network;

import java.lang.reflect.Type;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings.Secure;
import com.cloudjay.cjay.model.ComponentCode;
import com.cloudjay.cjay.model.ContainerSessionResult;
import com.cloudjay.cjay.model.DamageCode;
import com.cloudjay.cjay.model.IDatabaseManager;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.model.RepairCode;
import com.cloudjay.cjay.model.TmpContainerSession;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.NoConnectionException;
import com.cloudjay.cjay.util.NullSessionException;
import com.cloudjay.cjay.util.CJaySession;
import com.cloudjay.cjay.util.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.ion.Ion;

/**
 * 
 * CJay Network module
 * 
 * @author tieubao
 * 
 */
@SuppressLint("SimpleDateFormat")
public class CJayClient implements ICJayClient {

	private IHttpRequestWrapper requestWrapper;
	private IDatabaseManager databaseManager;
	private static CJayClient instance = null;

	public IDatabaseManager getDatabaseManager() {
		return databaseManager;
	}

	private CJayClient() {
	}

	private CJayClient(IHttpRequestWrapper requestWrapper,
			IDatabaseManager databaseManager) {
		this.requestWrapper = requestWrapper;
		this.databaseManager = databaseManager;
	}

	public static CJayClient getInstance() {
		if (instance == null) {
			instance = new CJayClient();
		}
		return instance;
	}

	public void init(IHttpRequestWrapper requestWrapper,
			IDatabaseManager databaseManager) {
		instance = new CJayClient(requestWrapper, databaseManager);
	}

	private HashMap<String, String> prepareHeadersWithToken(Context ctx)
			throws NullSessionException {

		if (ctx == null) {
			return null;
		}

		User currentUser = null;
		currentUser = CJaySession.restore(ctx).getCurrentUser();

		if (currentUser == null) {
			throw new NullSessionException();
		} else {
			HashMap<String, String> headers = new HashMap<String, String>();
			String accessToken = currentUser.getAccessToken();
			headers.put("Authorization", "Token " + accessToken);
			return headers;
		}
	}

	@Override
	public void addGCMDevice(String regid, Context ctx) throws JSONException,
			NoConnectionException {

		if (Utils.hasNoConnection(ctx)) {
			throw new NoConnectionException();
		}

		JSONObject requestPacket = new JSONObject();
		requestPacket.put("registration_id", regid);
		String androidId = "";
		try {
			androidId = Secure.getString(ctx.getContentResolver(),
					Secure.ANDROID_ID);
		} catch (Exception e) {
			e.printStackTrace();
		}

		UUID deviceUuid = new UUID(androidId.hashCode(), androidId.hashCode());
		String deviceId = deviceUuid.toString();
		requestPacket.put("device_id", deviceId);
		requestPacket.put("app_code", "CJAY");
		requestPacket.put("name", android.os.Build.MODEL);

		try {
			HashMap<String, String> headers = prepareHeadersWithToken(ctx);
			String response = requestWrapper.sendJSONPost(
					CJayConstant.API_ADD_GCM_DEVICE, requestPacket, headers);

		} catch (SocketTimeoutException e) {
			e.printStackTrace();
		} catch (NullSessionException e) {

		}

	}

	@Override
	public String getUserToken(String username, String password, Context ctx)
			throws NoConnectionException {

		if (Utils.hasNoConnection(ctx)) {
			Logger.w("No connection");
			throw new NoConnectionException();
		}

		String token = "";
		try {
			JsonObject result = Ion.with(ctx, CJayConstant.TOKEN)
					.setBodyParameter("username", username)
					.setBodyParameter("password", password).asJsonObject()
					.get();

			token = result.get("token").getAsString();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		return token;
	}

	@Override
	public User getCurrentUser(String token, Context ctx)
			throws NoConnectionException {

		if (Utils.hasNoConnection(ctx)) {
			Logger.w("No connection");
			throw new NoConnectionException();
		}

		User user = null;
		try {
			user = Ion.with(ctx, CJayConstant.CURRENT_USER)
					.setHeader("Authorization", "Token " + token)
					.as(new TypeToken<User>() {
					}).get();

		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		return user;
	}

	@Override
	public List<Operator> getOperators(Context ctx, String date)
			throws NoConnectionException, NullSessionException {

		Logger.Log("getOperators from " + date);

		if (Utils.hasNoConnection(ctx)) {
			Logger.Log("No connection");
			throw new NoConnectionException();
		}

		List<Operator> items = null;
		try {

			String accessToken = CJaySession.restore(ctx).getAccessToken();
			items = Ion.with(ctx, CJayConstant.LIST_OPERATORS)
					.setHeader("Authorization", "Token " + accessToken)
					.addQuery("modified_after", date)
					.as(new TypeToken<List<Operator>>() {
					}).get();

		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		return items;
	}

	@Override
	public List<DamageCode> getDamageCodes(Context ctx, String date)
			throws NoConnectionException, NullSessionException {

		Logger.Log("getDamageCodes from " + date);
		if (Utils.hasNoConnection(ctx)) {
			throw new NoConnectionException();
		}

		List<DamageCode> items = null;
		try {
			String accessToken = CJaySession.restore(ctx).getAccessToken();
			items = Ion.with(ctx, CJayConstant.LIST_DAMAGE_CODES)
					.setHeader("Authorization", "Token " + accessToken)
					.addQuery("modified_after", date)
					.as(new TypeToken<List<DamageCode>>() {
					}).get();

		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return items;

	}

	@Override
	public List<RepairCode> getRepairCodes(Context ctx, String date)
			throws NoConnectionException, NullSessionException {

		Logger.Log("getRepairCodes from " + date);
		if (Utils.hasNoConnection(ctx)) {
			throw new NoConnectionException();
		}

		List<RepairCode> items = null;
		try {
			String accessToken = CJaySession.restore(ctx).getAccessToken();
			items = Ion.with(ctx, CJayConstant.LIST_REPAIR_CODES)
					.setHeader("Authorization", "Token " + accessToken)
					.addQuery("modified_after", date)
					.as(new TypeToken<List<RepairCode>>() {
					}).get();

		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return items;

	}

	@Override
	public List<ComponentCode> getComponentCodes(Context ctx, String date)
			throws NoConnectionException, NullSessionException {

		Logger.Log("getComponentCodes from " + date);
		if (Utils.hasNoConnection(ctx)) {
			throw new NoConnectionException();
		}

		List<ComponentCode> items = null;
		try {
			String accessToken = CJaySession.restore(ctx).getAccessToken();
			items = Ion.with(ctx, CJayConstant.LIST_COMPONENT_CODES)
					.setHeader("Authorization", "Token " + accessToken)
					.addQuery("modified_after", date)
					.as(new TypeToken<List<ComponentCode>>() {
					}).get();

		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return items;
	}

	public ContainerSessionResult getContainerSessionsByPage(Context ctx,
			String date, int page) throws NoConnectionException,
			NullSessionException {

		if (Utils.hasNoConnection(ctx)) {
			throw new NoConnectionException();
		}

		String response = "";
		try {
			String accessToken = CJaySession.restore(ctx).getAccessToken();
			response = Ion.with(ctx, CJayConstant.CONTAINER_SESSIONS)
					.setHeader("Authorization", "Token " + accessToken)
					.addQuery("page", Integer.toString(page))
					.addQuery("created_after", date).asString().get();

		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		// Logger.Log( response);

		Gson gson = new GsonBuilder().setDateFormat(
				CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE).create();

		Type listType = new TypeToken<ContainerSessionResult>() {
		}.getType();

		ContainerSessionResult result = null;
		try {
			result = gson.fromJson(response, listType);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	@Override
	public String postContainerSession(Context ctx, TmpContainerSession item)
			throws NoConnectionException {

		if (Utils.hasNoConnection(ctx)) {
			Logger.Log("Network is not available");
			throw new NoConnectionException();
		}

		String ret = "";
		try {

			HashMap<String, String> headers = prepareHeadersWithToken(ctx);
			Gson gson = new Gson();

			String data = gson.toJson(item);
			String url = CJayConstant.CONTAINER_SESSIONS;
			ret = requestWrapper.sendPost(url, data, "application/json",
					headers);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return ret;
	}

}
