package com.cloudjay.cjay.network;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpStatus;
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
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

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

	@Override
	public String getUserToken(String username, String password, Context ctx)
			throws NoConnectionException {

		if (Utils.hasNoConnection(ctx)) {
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

			Response<List<Operator>> response = Ion
					.with(ctx, CJayConstant.LIST_OPERATORS)
					.setHeader("Authorization", "Token " + accessToken)
					.addQuery("modified_after", date)
					.as(new TypeToken<List<Operator>>() {
					}).withResponse().get();

			switch (response.getHeaders().getResponseCode()) {
			case HttpStatus.SC_FORBIDDEN: // User không có quyền truy cập
				throw new NullSessionException();

			case HttpStatus.SC_INTERNAL_SERVER_ERROR: // Server bị vãi
				break;

			case HttpStatus.SC_NOT_FOUND: // Không có dữ liệu tương ứng
				break;

			default:
				items = response.getResult();
				break;
			}

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
			Response<List<DamageCode>> response = Ion
					.with(ctx, CJayConstant.LIST_DAMAGE_CODES)
					.setHeader("Authorization", "Token " + accessToken)
					.addQuery("modified_after", date)
					.as(new TypeToken<List<DamageCode>>() {
					}).withResponse().get();

			switch (response.getHeaders().getResponseCode()) {
			case HttpStatus.SC_FORBIDDEN: // User không có quyền truy cập
				throw new NullSessionException();

			case HttpStatus.SC_INTERNAL_SERVER_ERROR: // Server bị vãi
				break;

			case HttpStatus.SC_NOT_FOUND: // Không có dữ liệu tương ứng
				break;

			default:
				items = response.getResult();
				break;
			}

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
			Response<List<RepairCode>> response = Ion
					.with(ctx, CJayConstant.LIST_REPAIR_CODES)
					.setHeader("Authorization", "Token " + accessToken)
					.addQuery("modified_after", date)
					.as(new TypeToken<List<RepairCode>>() {
					}).withResponse().get();

			switch (response.getHeaders().getResponseCode()) {
			case HttpStatus.SC_FORBIDDEN: // User không có quyền truy cập
				throw new NullSessionException();

			case HttpStatus.SC_INTERNAL_SERVER_ERROR: // Server bị vãi
				break;

			case HttpStatus.SC_NOT_FOUND: // Không có dữ liệu tương ứng
				break;

			default:
				items = response.getResult();
				break;
			}

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
			Response<List<ComponentCode>> response = Ion
					.with(ctx, CJayConstant.LIST_COMPONENT_CODES)
					.setHeader("Authorization", "Token " + accessToken)
					.addQuery("modified_after", date)
					.as(new TypeToken<List<ComponentCode>>() {
					}).withResponse().get();

			switch (response.getHeaders().getResponseCode()) {
			case HttpStatus.SC_FORBIDDEN: // User không có quyền truy cập
				throw new NullSessionException();

			case HttpStatus.SC_INTERNAL_SERVER_ERROR: // Server bị vãi
				break;

			case HttpStatus.SC_NOT_FOUND: // Không có dữ liệu tương ứng
				break;

			default:
				items = response.getResult();
				break;
			}

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

		String result = "";
		try {
			String accessToken = CJaySession.restore(ctx).getAccessToken();
			Response<String> response = Ion
					.with(ctx, CJayConstant.CONTAINER_SESSIONS)
					.setHeader("Authorization", "Token " + accessToken)
					.addQuery("page", Integer.toString(page))
					.addQuery("created_after", date).asString().withResponse()
					.get();

			switch (response.getHeaders().getResponseCode()) {
			case HttpStatus.SC_FORBIDDEN: // User không có quyền truy cập
				throw new NullSessionException();

			case HttpStatus.SC_INTERNAL_SERVER_ERROR: // Server bị vãi
				break;

			case HttpStatus.SC_NOT_FOUND: // Không có dữ liệu tương ứng
				break;

			default:
				result = response.getResult();
				break;
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		// Logger.Log(result);

		Gson gson = new GsonBuilder().setDateFormat(
				CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE).create();

		Type listType = new TypeToken<ContainerSessionResult>() {
		}.getType();

		ContainerSessionResult item = null;
		try {
			item = gson.fromJson(result, listType);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return item;
	}

	@Override
	public String postContainerSession(Context ctx, TmpContainerSession item)
			throws NoConnectionException, NullSessionException {

		if (Utils.hasNoConnection(ctx)) {
			Logger.Log("Network is not available");
			throw new NoConnectionException();
		}

		String ret = "";
		String accessToken = CJaySession.restore(ctx).getAccessToken();

		try {

			Response<String> response = Ion
					.with(ctx, CJayConstant.CONTAINER_SESSIONS)
					.setHeader("Authorization", "Token " + accessToken)
					.setJsonObjectBody(item,
							new TypeToken<TmpContainerSession>() {
							}).asString().withResponse()
					.setCallback(new FutureCallback<Response<String>>() {

						@Override
						public void onCompleted(Exception arg0,
								Response<String> arg1) {
						}
					}).get();

			switch (response.getHeaders().getResponseCode()) {
			case HttpStatus.SC_FORBIDDEN: // User không có quyền truy cập
				throw new NullSessionException();

			case HttpStatus.SC_INTERNAL_SERVER_ERROR: // Server bị vãi
				break;

			case HttpStatus.SC_NOT_FOUND: // Không có dữ liệu tương ứng
				break;

			default:
				ret = response.getResult();
				break;
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		return ret;
	}

	@Override
	public void addGCMDevice(String regid, final Context ctx)
			throws NoConnectionException, NullSessionException, JSONException {

		if (Utils.hasNoConnection(ctx)) {
			throw new NoConnectionException();
		}

		String androidId = "";
		try {
			androidId = Secure.getString(ctx.getContentResolver(),
					Secure.ANDROID_ID);
		} catch (Exception e) {
			e.printStackTrace();
		}

		UUID deviceUuid = new UUID(androidId.hashCode(), androidId.hashCode());
		String deviceId = deviceUuid.toString();

		JSONObject requestPacket = new JSONObject();
		requestPacket.put("registration_id", regid);
		requestPacket.put("device_id", deviceId);
		requestPacket.put("app_code", "CJAY");
		requestPacket.put("name", android.os.Build.MODEL);

		String accessToken = CJaySession.restore(ctx).getAccessToken();
		try {
			Response<JsonObject> response = Ion
					.with(ctx, CJayConstant.API_ADD_GCM_DEVICE)
					.setHeader("Authorization ", accessToken)
					.setJsonObjectBody(requestPacket).asJsonObject()
					.withResponse()
					.setCallback(new FutureCallback<Response<JsonObject>>() {

						@Override
						public void onCompleted(Exception arg0,
								Response<JsonObject> arg1) {

						}
					}).get();

			switch (response.getHeaders().getResponseCode()) {
			case HttpStatus.SC_FORBIDDEN: // User không có quyền truy cập
				throw new NullSessionException();

			case HttpStatus.SC_INTERNAL_SERVER_ERROR: // Server bị vãi
				break;

			case HttpStatus.SC_NOT_FOUND: // Không có dữ liệu tương ứng
				break;

			default:
				break;
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

	}
}
