package com.cloudjay.cjay.network;

import java.io.File;
import java.io.RandomAccessFile;
import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpStatus;
import org.json.JSONException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.Log;

import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.ComponentCode;
import com.cloudjay.cjay.model.ContainerSessionResult;
import com.cloudjay.cjay.model.DamageCode;
import com.cloudjay.cjay.model.IDatabaseManager;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.model.RepairCode;
import com.cloudjay.cjay.model.TmpContainerSession;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.CJaySession;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.MismatchDataException;
import com.cloudjay.cjay.util.NoConnectionException;
import com.cloudjay.cjay.util.NullSessionException;
import com.cloudjay.cjay.util.ServerInternalErrorException;
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

	public static final int REQUEST_TYPE_CREATED = 0;
	public static final int REQUEST_TYPE_MODIFIED = 1;

	public static CJayClient getInstance() {
		if (instance == null) {
			instance = new CJayClient();
		}
		return instance;
	}

	private IDatabaseManager databaseManager;

	private static CJayClient instance = null;

	private CJayClient() {
	}

	private CJayClient(IHttpRequestWrapper requestWrapper, IDatabaseManager databaseManager) {
		this.databaseManager = databaseManager;
	}

	@Override
	public void addGCMDevice(String regid, final Context ctx) throws NoConnectionException, NullSessionException,
																JSONException {

		if (Utils.hasNoConnection(ctx)) throw new NoConnectionException();

		String androidId = "";
		try {
			androidId = Secure.getString(ctx.getContentResolver(), Secure.ANDROID_ID);
		} catch (Exception e) {
			e.printStackTrace();
		}

		UUID deviceUuid = new UUID(androidId.hashCode(), androidId.hashCode());
		String deviceId = deviceUuid.toString();

		CJaySession session = CJaySession.restore(ctx);
		if (session == null) { throw new NullSessionException(); }

		User user = session.getCurrentUser();
		String accessToken = user.getAccessToken();

		JsonObject requestPacket = new JsonObject();
		requestPacket.addProperty("registration_id", regid);
		requestPacket.addProperty("device_id", deviceId);
		requestPacket.addProperty("app_code", "CJAY");
		requestPacket.addProperty("name", android.os.Build.MODEL);

		try {

			Response<JsonObject> response = Ion.with(ctx, CJayConstant.API_ADD_GCM_DEVICE)
												.setHeader("Authorization ", "Token " + accessToken)
												.setHeader("CJAY_VERSION", Utils.getAppVersionName(ctx))
												.setHeader("CJAY_USERNAME", user.getUserName())
												.setJsonObjectBody(requestPacket).asJsonObject().withResponse()
												.setCallback(new FutureCallback<Response<JsonObject>>() {
													@Override
													public void onCompleted(Exception arg0, Response<JsonObject> arg1) {

													}
												}).get();

			switch (response.getHeaders().getResponseCode()) {
				case HttpStatus.SC_FORBIDDEN: // User không có quyền truy cập
				case HttpStatus.SC_UNAUTHORIZED:
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

	@Override
	public List<ComponentCode> getComponentCodes(Context ctx, String date) throws NoConnectionException,
																			NullSessionException {

		Logger.Log("getComponentCodes from " + date);
		if (Utils.hasNoConnection(ctx)) throw new NoConnectionException();

		List<ComponentCode> items = null;
		try {
			String accessToken = CJaySession.restore(ctx).getAccessToken();
			Response<List<ComponentCode>> response = Ion.with(ctx, CJayConstant.LIST_COMPONENT_CODES)
														.setHeader("Authorization", "Token " + accessToken)
														.setHeader("CJAY_VERSION", Utils.getAppVersionName(ctx))
														.addQuery("modified_after", date)
														.as(new TypeToken<List<ComponentCode>>() {
														}).withResponse().get();

			switch (response.getHeaders().getResponseCode()) {
				case HttpStatus.SC_FORBIDDEN: // User không có quyền truy cập
				case HttpStatus.SC_UNAUTHORIZED:
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

	public TmpContainerSession getContainerSessionById(Context ctx, int id) throws NoConnectionException,
																			NullSessionException {

		if (id == 0) return null;
		if (Utils.hasNoConnection(ctx)) throw new NoConnectionException();

		CJaySession session = CJaySession.restore(ctx);
		if (null == session) { throw new NullSessionException(); }
		String accessToken = CJaySession.restore(ctx).getAccessToken();
		if (TextUtils.isEmpty(accessToken)) { throw new NullSessionException(); }

		String result = "";
		Response<String> response = null;
		try {

			response = Ion.with(ctx, String.format(CJayConstant.CONTAINER_SESSION_ITEM, Integer.toString(id)))
							.setHeader("Authorization", "Token " + accessToken)
							.setHeader("CJAY_VERSION", Utils.getAppVersionName(ctx)).asString().withResponse().get();

			switch (response.getHeaders().getResponseCode()) {
				case HttpStatus.SC_FORBIDDEN: // User không có quyền truy cập
				case HttpStatus.SC_UNAUTHORIZED:
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

		Logger.w("Result: " + result);
		Gson gson = new GsonBuilder().setDateFormat(CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE).create();
		Type listType = new TypeToken<TmpContainerSession>() {
		}.getType();

		TmpContainerSession item = null;
		try {
			item = gson.fromJson(result, listType);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return item;
	}

	@Override
	public ContainerSessionResult getContainerSessionsByPage(Context ctx, String date, int page, int type,
																String andAfter) throws NoConnectionException,
																				NullSessionException {

		if (Utils.hasNoConnection(ctx)) throw new NoConnectionException();

		String result = "";
		try {

			CJaySession session = CJaySession.restore(ctx);
			if (null == session) { throw new NullSessionException(); }

			String accessToken = CJaySession.restore(ctx).getAccessToken();
			if (TextUtils.isEmpty(accessToken)) { throw new NullSessionException(); }

			Response<String> response = null;
			if (type == REQUEST_TYPE_CREATED) {

				response = Ion.with(ctx, CJayConstant.CONTAINER_SESSIONS).setLogging("ION", Log.INFO)
								.setHeader("Authorization", "Token " + accessToken)
								.setHeader("CJAY_VERSION", Utils.getAppVersionName(ctx))
								.addQuery("page", Integer.toString(page)).addQuery("created_after", date).asString()
								.withResponse().get();

			} else {
				Logger.Log("Request based on modified time");
				response = Ion.with(ctx, CJayConstant.CONTAINER_SESSIONS)
								.setHeader("Authorization", "Token " + accessToken)
								.setHeader("CJAY_VERSION", Utils.getAppVersionName(ctx))
								.addQuery("page", Integer.toString(page)).addQuery("modified_after", date)
								.addQuery("and_after", andAfter).asString().withResponse().get();

			}

			switch (response.getHeaders().getResponseCode()) {
				case HttpStatus.SC_FORBIDDEN: // User không có quyền truy cập
				case HttpStatus.SC_UNAUTHORIZED:
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

		// Logger.w("Result: " + result);
		Gson gson = new GsonBuilder().setDateFormat(CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE).create();

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
	public User getCurrentUser(String token, Context ctx) throws NoConnectionException {

		if (Utils.hasNoConnection(ctx)) {
			Logger.w("No connection");
			throw new NoConnectionException();
		}

		User user = null;
		try {
			user = Ion.with(ctx, CJayConstant.CURRENT_USER).setHeader("Authorization", "Token " + token)
						.setHeader("CJAY_VERSION", Utils.getAppVersionName(ctx)).as(new TypeToken<User>() {
						}).get();

		} catch (InterruptedException e) {

			Logger.Log("InterruptedException");
			e.printStackTrace();
		} catch (ExecutionException e) {
			Logger.Log("ExecutionException");
			e.printStackTrace();
		}

		return user;
	}

	@Override
	public List<DamageCode> getDamageCodes(Context ctx, String date) throws NoConnectionException, NullSessionException {

		Logger.Log("getDamageCodes from " + date);
		if (Utils.hasNoConnection(ctx)) throw new NoConnectionException();

		List<DamageCode> items = null;
		try {
			String accessToken = CJaySession.restore(ctx).getAccessToken();
			Response<List<DamageCode>> response = Ion.with(ctx, CJayConstant.LIST_DAMAGE_CODES)
														.setHeader("Authorization", "Token " + accessToken)
														.setHeader("CJAY_VERSION", Utils.getAppVersionName(ctx))
														.addQuery("modified_after", date)
														.as(new TypeToken<List<DamageCode>>() {
														}).withResponse().get();

			switch (response.getHeaders().getResponseCode()) {
				case HttpStatus.SC_FORBIDDEN: // User không có quyền truy cập
				case HttpStatus.SC_UNAUTHORIZED:
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

	public IDatabaseManager getDatabaseManager() {
		return databaseManager;
	}

	@Override
	public List<Operator> getOperators(Context ctx, String date) throws NoConnectionException, NullSessionException {

		Logger.Log("getOperators from " + date);

		if (Utils.hasNoConnection(ctx)) {
			Logger.Log("No connection");
			throw new NoConnectionException();
		}

		List<Operator> items = null;
		try {

			String accessToken = CJaySession.restore(ctx).getAccessToken();

			Response<List<Operator>> response = Ion.with(ctx, CJayConstant.LIST_OPERATORS)
													.setHeader("Authorization", "Token " + accessToken)
													.setHeader("CJAY_VERSION", Utils.getAppVersionName(ctx))
													.addQuery("modified_after", date)
													.as(new TypeToken<List<Operator>>() {
													}).withResponse().get();

			switch (response.getHeaders().getResponseCode()) {
				case HttpStatus.SC_FORBIDDEN: // User không có quyền truy cập
				case HttpStatus.SC_UNAUTHORIZED:
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
	public List<RepairCode> getRepairCodes(Context ctx, String date) throws NoConnectionException, NullSessionException {

		Logger.Log("getRepairCodes from " + date);
		if (Utils.hasNoConnection(ctx)) throw new NoConnectionException();

		List<RepairCode> items = null;
		try {
			String accessToken = CJaySession.restore(ctx).getAccessToken();
			Response<List<RepairCode>> response = Ion.with(ctx, CJayConstant.LIST_REPAIR_CODES)
														.setHeader("Authorization", "Token " + accessToken)
														.setHeader("CJAY_VERSION", Utils.getAppVersionName(ctx))
														.addQuery("modified_after", date)
														.as(new TypeToken<List<RepairCode>>() {
														}).withResponse().get();

			switch (response.getHeaders().getResponseCode()) {
				case HttpStatus.SC_FORBIDDEN: // User không có quyền truy cập
				case HttpStatus.SC_UNAUTHORIZED:
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
	public String getUserToken(String username, String password, Context ctx) throws NoConnectionException {

		if (Utils.hasNoConnection(ctx)) throw new NoConnectionException();

		String token = "";
		try {
			JsonObject result = Ion.with(ctx, CJayConstant.TOKEN).setBodyParameter("username", username)
									.setBodyParameter("password", password).asJsonObject().get();

			Logger.w(result.toString());

			token = result.get("token").getAsString();

		} catch (InterruptedException e) {

			e.printStackTrace();

		} catch (ExecutionException e) {

			e.printStackTrace();
		}

		return token;
	}

	public void init(IHttpRequestWrapper requestWrapper, IDatabaseManager databaseManager) {
		instance = new CJayClient(requestWrapper, databaseManager);
	}

	public Response<String> uploadFile(Context ctx, CJayImage uploadItem) throws NoConnectionException {

		if (Utils.hasNoConnection(ctx)) {
			Logger.Log("Network is not available");
			throw new NoConnectionException();
		}

		String appVersion = Utils.getAppVersionName(ctx);
		File f = ctx.getFileStreamPath(uploadItem.getUuid());

		try {
			RandomAccessFile rf = new RandomAccessFile(f, "rw");
			rf.setLength(1024 * 1024 * 2);
			rf.close();

		} catch (Exception e) {
			System.err.println(e);
		}

		try {
			Response<String> response = Ion.with(	ctx,
													String.format(	CJayConstant.CJAY_TMP_STORAGE,
																	uploadItem.getImageName()))
											.setHeader("CJAY_VERSION", appVersion)
											.addHeader("Content-Type", "image/jpeg")
											.setMultipartFile(uploadItem.getImageName(), f).asString().withResponse()
											.get();

			Logger.w("Response code: " + response.getHeaders().getResponseMessage() + " | "
					+ Integer.toString(response.getHeaders().getResponseCode()));

			return response;
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new NoConnectionException();
		} catch (ExecutionException e) {
			e.printStackTrace();
			throw new NoConnectionException();
		}
	}

	@Override
	public String postContainerSession(Context ctx, TmpContainerSession item) throws NoConnectionException,
																				NullSessionException,
																				MismatchDataException,
																				ServerInternalErrorException {

		if (Utils.hasNoConnection(ctx)) {
			Logger.Log("Network is not available");
			throw new NoConnectionException();
		}

		String ret = "";
		String accessToken = CJaySession.restore(ctx).getAccessToken();
		String appVersion = Utils.getAppVersionName(ctx);

		try {

			Gson gson = new GsonBuilder().setDateFormat(CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE).create();
			Type listType = new TypeToken<TmpContainerSession>() {
			}.getType();

			String resultString = gson.toJson(item, listType);

			if (TextUtils.isEmpty(resultString)) {
				Logger.e("Cannot parse container " + item.getContainerId());
				throw new MismatchDataException();
			}
			Logger.Log(resultString);

			Response<String> response = Ion.with(ctx, CJayConstant.CONTAINER_SESSIONS).setLogging("ION", Log.INFO)
											.setHeader("Authorization", "Token " + accessToken)
											.setHeader("CJAY_VERSION", appVersion)
											.setJsonObjectBody(item, new TypeToken<TmpContainerSession>() {
											}).asString().withResponse()
											.setCallback(new FutureCallback<Response<String>>() {

												@Override
												public void onCompleted(Exception arg0, Response<String> arg1) {
												}

											}).get();

			Logger.w("Response code: " + response.getHeaders().getResponseMessage() + " | "
					+ Integer.toString(response.getHeaders().getResponseCode()));

			getDatabaseManager().getHelper(ctx).addUsageLog("Container "
																	+ item.getContainerId()
																	+ " | #Response code: "
																	+ response.getHeaders().getResponseMessage()
																	+ " | "
																	+ Integer.toString(response.getHeaders()
																								.getResponseCode()));

			switch (response.getHeaders().getResponseCode()) {

			// User không có quyền truy cập
				case HttpStatus.SC_UNAUTHORIZED:
				case HttpStatus.SC_FORBIDDEN:
					throw new NullSessionException();

				case HttpStatus.SC_INTERNAL_SERVER_ERROR: // Server bị vãi
					// Rollback
					throw new ServerInternalErrorException();

				case HttpStatus.SC_NOT_FOUND: // Không có dữ liệu tương ứng
				case HttpStatus.SC_BAD_REQUEST:
					// Server will process it
					// Set container to error
					throw new MismatchDataException();

				default:
					ret = response.getResult();
					break;
			}

		} catch (InterruptedException e) {

			e.printStackTrace();
			throw new NoConnectionException();

		} catch (ExecutionException e) {

			e.printStackTrace();
			throw new NoConnectionException();
		}

		return ret;
	}

	// public List<TmpContainerSession> getContainerSessions(Context ctx, String date) throws NoConnectionException,
	// NullSessionException {
	//
	// if (Utils.hasNoConnection(ctx)) throw new NoConnectionException();
	//
	// String accessToken = CJaySession.restore(ctx).getAccessToken();
	//
	// try {
	// Response<List<TmpContainerSession>> response = Ion.with(ctx, CJayConstant.CONTAINER_SESSIONS)
	// .setHeader("Authorization", "Token " + accessToken)
	// .setHeader("CJAY_VERSION", Utils.getAppVersionName(ctx))
	// .addQuery("created_after", date)
	// .as(new TypeToken<List<TmpContainerSession>>() {
	// }).withResponse().get();
	//
	// switch (response.getHeaders().getResponseCode()) {
	// case HttpStatus.SC_FORBIDDEN: // User không có quyền truy cập
	// case HttpStatus.SC_UNAUTHORIZED:
	// throw new NullSessionException();
	//
	// case HttpStatus.SC_INTERNAL_SERVER_ERROR: // Server bị vãi
	// break;
	//
	// case HttpStatus.SC_NOT_FOUND: // Không có dữ liệu tương ứng
	// break;
	//
	// default:
	// return response.getResult();
	// }
	//
	// } catch (InterruptedException e) {
	//
	// e.printStackTrace();
	// } catch (ExecutionException e) {
	//
	// e.printStackTrace();
	// }
	// return null;
	//
	// }

	// public
	// ContainerSessionResult
	// getContainerSessionsByPageAndStatus(Context ctx, String date, int page, int filterStatus, int type)
	// throws NoConnectionException,
	// NullSessionException {
	//
	// if (Utils.hasNoConnection(ctx)) throw new NoConnectionException();
	//
	// String result = "";
	// try {
	//
	// String accessToken = CJaySession.restore(ctx).getAccessToken();
	// if (TextUtils.isEmpty(accessToken)) { throw new NullSessionException(); }
	//
	// Response<String> response = null;
	// if (type == REQUEST_TYPE_CREATED) {
	//
	// response = Ion.with(ctx, CJayConstant.CONTAINER_SESSIONS)
	// .setHeader("Authorization", "Token " + accessToken)
	// .setHeader("CJAY_VERSION", Utils.getAppVersionName(ctx))
	// .addQuery("page", Integer.toString(page))
	// .addQuery("filter_status", Integer.toString(filterStatus))
	// .addQuery("created_after", date).asString().withResponse().get();
	//
	// } else {
	// Logger.Log("Request based on modified time");
	// response = Ion.with(ctx, CJayConstant.CONTAINER_SESSIONS)
	// .setHeader("Authorization", "Token " + accessToken)
	// .setHeader("CJAY_VERSION", Utils.getAppVersionName(ctx))
	// .addQuery("page", Integer.toString(page))
	// .addQuery("filter_status", Integer.toString(filterStatus))
	// .addQuery("modified_after", date).asString().withResponse().get();
	//
	// }
	//
	// switch (response.getHeaders().getResponseCode()) {
	// case HttpStatus.SC_FORBIDDEN: // User không có quyền truy cập
	// case HttpStatus.SC_UNAUTHORIZED:
	// throw new NullSessionException();
	//
	// case HttpStatus.SC_INTERNAL_SERVER_ERROR: // Server bị vãi
	// break;
	//
	// case HttpStatus.SC_NOT_FOUND: // Không có dữ liệu tương ứng
	// break;
	//
	// default:
	// result = response.getResult();
	//
	// break;
	// }
	//
	// } catch (InterruptedException e) {
	// e.printStackTrace();
	// } catch (ExecutionException e) {
	// e.printStackTrace();
	// }
	//
	// // Logger.w("Result: " + result);
	// Gson gson = new GsonBuilder().setDateFormat(CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE).create();
	//
	// Type listType = new TypeToken<ContainerSessionResult>() {
	// }.getType();
	//
	// ContainerSessionResult item = null;
	// try {
	// item = gson.fromJson(result, listType);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	//
	// return item;
	// }
}
