package com.cloudjay.cjay.network;

import java.lang.reflect.Type;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import com.cloudjay.cjay.dao.ComponentCodeDaoImpl;
import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.dao.DamageCodeDaoImpl;
import com.cloudjay.cjay.dao.OperatorDaoImpl;
import com.cloudjay.cjay.dao.RepairCodeDaoImpl;
import com.cloudjay.cjay.events.DataLoadedEvent;
import com.cloudjay.cjay.model.CJayResourceStatus;
import com.cloudjay.cjay.model.ComponentCode;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.DamageCode;
import com.cloudjay.cjay.model.IDatabaseManager;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.model.RepairCode;
import com.cloudjay.cjay.model.TmpContainerSession;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.Mapper;
import com.cloudjay.cjay.util.NoConnectionException;
import com.cloudjay.cjay.util.PreferencesUtil;
import com.cloudjay.cjay.util.Session;
import com.cloudjay.cjay.util.StringHelper;
import com.cloudjay.cjay.util.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import de.greenrobot.event.EventBus;

/**
 * 
 * CJay Network module
 * 
 * @author tieubao
 * 
 */
@SuppressLint("SimpleDateFormat")
public class CJayClient implements ICJayClient {

	private static final String LOG_TAG = "CJayClient";
	public static String BASE_URL = "https://cloudjay-web.appspot.com/api/jaypix/";

	private IHttpRequestWrapper requestWrapper;
	private IDatabaseManager databaseManager;
	private static CJayClient instance = null;

	public IHttpRequestWrapper getRequestWrapper() {
		return requestWrapper;
	}

	public void setRequestWrapper(IHttpRequestWrapper requestWrapper) {
		this.requestWrapper = requestWrapper;
	}

	public IDatabaseManager getDatabaseManager() {
		return databaseManager;
	}

	public void setDatabaseManager(IDatabaseManager databaseManager) {
		this.databaseManager = databaseManager;
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

	private HashMap<String, String> prepareHeadersWithToken(Context ctx) {

		User currentUser = null;
		currentUser = Session.restore(ctx).getCurrentUser();

		// User currentUser = ((CJayActivity) ctx).getCurrentUser();
		HashMap<String, String> headers = new HashMap<String, String>();
		String accessToken = currentUser.getAccessToken();
		headers.put("Authorization", "Token " + accessToken);
		return headers;
	}

	private HashMap<String, String> prepareHeadersWithToken(String token) {

		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Authorization", "Token " + token);
		return headers;
	}

	/**
	 * 
	 * fetch data based on current user role
	 * 
	 * - Giám định cổng:
	 * 
	 * 1. In: Hiển thị list container ở local và chưa được upload
	 * (upload_confirmation = false).
	 * 
	 * 2. Out: Hiện thị list container ở local + server, chưa xuất
	 * (check_out_time = null), chưa upload.
	 * 
	 * - Giám định sửa chữa:
	 * 
	 * 1. Chưa sửa chữa: hiển thị các container có các `CJayImage` chưa điền đầy
	 * đủ thông tin `Issue`
	 * 
	 * 2. Đã sửa chữa: hiển thị các container có đầy đủ thông tin về `Issue`
	 * 
	 * - Giám định sau sửa chữa:
	 * 
	 * 1.
	 * 
	 * @param ctx
	 * @throws NoConnectionException
	 */
	public void fetchData(Context ctx) throws NoConnectionException {
		Logger.Log(LOG_TAG, "fetching data ...");

		try {

			boolean hasNewData = false;

			Date now = new Date();

			// 2013-11-10T21:05:24 (do not have timezone info)
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					CJayConstant.CJAY_SERVER_DATETIME_FORMAT);
			String nowString = dateFormat.format(now);

			// 1. chưa có ISO code data
			Logger.Log(LOG_TAG, "no iso code");
			OperatorDaoImpl operatorDaoImpl = databaseManager.getHelper(ctx)
					.getOperatorDaoImpl();
			DamageCodeDaoImpl damageCodeDaoImpl = databaseManager
					.getHelper(ctx).getDamageCodeDaoImpl();
			RepairCodeDaoImpl repairCodeDaoImpl = databaseManager
					.getHelper(ctx).getRepairCodeDaoImpl();
			ComponentCodeDaoImpl componentCodeDaoImpl = databaseManager
					.getHelper(ctx).getComponentCodeDaoImpl();

			if (operatorDaoImpl.isEmpty()) {
				Logger.Log(LOG_TAG, "get list operators");
				PreferencesUtil.storePrefsValue(ctx,
						PreferencesUtil.PREF_RESOURCE_OPERATOR_LAST_UPDATE,
						nowString);

				List<Operator> operators = getOperators(ctx);
				if (null != operators)
					operatorDaoImpl.addListOperators(operators);
			}

			if (damageCodeDaoImpl.isEmpty()) {
				Logger.Log(LOG_TAG, "get list damage codes");
				PreferencesUtil.storePrefsValue(ctx,
						PreferencesUtil.PREF_RESOURCE_DAMAGE_LAST_UPDATE,
						nowString);
				List<DamageCode> damageCodes = getDamageCodes(ctx);
				if (null != damageCodes)
					damageCodeDaoImpl.addListDamageCodes(damageCodes);
			}

			if (repairCodeDaoImpl.isEmpty()) {
				Logger.Log(LOG_TAG, "get list repair codes");
				PreferencesUtil.storePrefsValue(ctx,
						PreferencesUtil.PREF_RESOURCE_REPAIR_LAST_UPDATE,
						nowString);

				List<RepairCode> repairCodes = getRepairCodes(ctx);
				if (null != repairCodes)
					repairCodeDaoImpl.addListRepairCodes(repairCodes);
			}

			if (componentCodeDaoImpl.isEmpty()) {
				Logger.Log(LOG_TAG, "get list of component codes");

				PreferencesUtil.storePrefsValue(ctx,
						PreferencesUtil.PREF_RESOURCE_COMPONENT_LAST_UPDATE,
						nowString);

				List<ComponentCode> componentCodes = getComponentCodes(ctx);
				if (null != componentCodes)
					componentCodeDaoImpl.addListComponentCodes(componentCodes);

			}

			// 2. fetch ISO CODE
			if (hasNewMetadata(ctx)) {
				Logger.Log(LOG_TAG, "fetch iso code");
			}

			ContainerSessionDaoImpl containerSessionDaoImpl = databaseManager
					.getHelper(ctx).getContainerSessionDaoImpl();

			User user = Session.restore(ctx).getCurrentUser();
			int userRole = user.getRole();
			int filterStatus = user.getFilterStatus();

			Logger.Log(LOG_TAG, "User Role: " + user.getRoleName()
					+ "\nFilter: " + Integer.toString(user.getFilterStatus()));

			// 3. Update list ContainerSessions
			Logger.Log(LOG_TAG, "get list container sessions");
			List<ContainerSession> containerSessions = null;

			if (containerSessionDaoImpl.isEmpty()) {

				Logger.Log(LOG_TAG,
						"get new list container sessions based on user role");

				// containerSessions = getContainerSessions(ctx, userRole,
				// filterStatus);

				containerSessions = getAllContainerSessions(ctx);

				PreferencesUtil.storePrefsValue(ctx,
						PreferencesUtil.PREF_CONTAINER_SESSION_LAST_UPDATE,
						nowString);

			} else {

				String date = PreferencesUtil.getPrefsValue(ctx,
						PreferencesUtil.PREF_CONTAINER_SESSION_LAST_UPDATE);

				Logger.Log(LOG_TAG,
						"get updated list container sessions from last time: "
								+ date);

				// update Last Update for each time convert container session
				// containerSessions = getContainerSessions(ctx, userRole,
				// filterStatus, date);

				containerSessions = getContainerSessions(ctx, date);

				// TODO: need to refactor after implement push notification
				PreferencesUtil.storePrefsValue(ctx,
						PreferencesUtil.PREF_CONTAINER_SESSION_LAST_UPDATE,
						nowString);

				if (containerSessions == null) {
					Logger.Log(LOG_TAG, "-----> NO new container sessions");
				} else {
					Logger.Log(LOG_TAG,
							"Has " + Integer.toString(containerSessions.size())
									+ " new container sessions");
				}

				Logger.Log(
						LOG_TAG,
						"----> Last update from "
								+ PreferencesUtil
										.getPrefsValue(
												ctx,
												PreferencesUtil.PREF_CONTAINER_SESSION_LAST_UPDATE));
			}

			if (null != containerSessions) {
				containerSessionDaoImpl
						.addListContainerSessions(containerSessions);
				EventBus.getDefault().post(new DataLoadedEvent());
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getUserToken(String username, String password, Context ctx)
			throws JSONException, SocketTimeoutException, NoConnectionException {

		if (Utils.hasNoConnection(ctx)) {
			Logger.Log(LOG_TAG, "No connection");
			throw new NoConnectionException();
		}

		Logger.Log("getting User Token ... ");

		JSONObject requestPacket = new JSONObject();
		requestPacket.put("username", username);
		requestPacket.put("password", password);

		String tokenResponseString = "";
		tokenResponseString = requestWrapper.sendJSONPost(CJayConstant.TOKEN,
				requestPacket);

		JsonElement jelement = new JsonParser().parse(tokenResponseString);

		String token = null;
		try {
			token = jelement.getAsJsonObject().get("token").getAsString();
		} catch (Exception ex) {
			token = null;
		}
		return token;
	}

	@Override
	public void addGCMDevice(String regid, Context ctx) throws JSONException,
			NoConnectionException {

		if (Utils.hasNoConnection(ctx)) {
			Logger.Log(LOG_TAG, "No connection");
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

		HashMap<String, String> headers = prepareHeadersWithToken(ctx);

		String response = "";
		try {
			response = requestWrapper.sendJSONPost(
					CJayConstant.API_ADD_GCM_DEVICE, requestPacket, headers);

			Logger.Log("GCM", response);
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
		}
	}

	@Override
	public User getCurrentUser(String token, Context ctx)
			throws NoConnectionException {

		Logger.Log("getting Current User ...");

		if (Utils.hasNoConnection(ctx)) {
			Logger.Log(LOG_TAG, "No connection");
			throw new NoConnectionException();
		}

		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Authorization", "Token " + token);
		String response = requestWrapper.sendGet(CJayConstant.CURRENT_USER,
				headers);

		Logger.Log(response);
		Gson gson = new Gson();
		Type userType = new TypeToken<User>() {
		}.getType();

		User user = gson.fromJson(response, userType);
		return user;
	}

	@Override
	public List<Operator> getOperators(Context ctx)
			throws NoConnectionException {

		if (Utils.hasNoConnection(ctx)) {
			Logger.Log(LOG_TAG, "No connection");
			throw new NoConnectionException();
		}

		HashMap<String, String> headers = prepareHeadersWithToken(ctx);
		String response = requestWrapper.sendGet(CJayConstant.LIST_OPERATORS,
				headers);

		Gson gson = new Gson();
		Type listType = new TypeToken<List<Operator>>() {
		}.getType();

		List<Operator> items = gson.fromJson(response, listType);
		return items;
	}

	@Override
	public List<DamageCode> getDamageCodes(Context ctx)
			throws NoConnectionException {

		if (Utils.hasNoConnection(ctx)) {
			throw new NoConnectionException();
		}

		HashMap<String, String> headers = prepareHeadersWithToken(ctx);
		String response = requestWrapper.sendGet(
				CJayConstant.LIST_DAMAGE_CODES, headers);
		Gson gson = new Gson();
		Type listType = new TypeToken<List<DamageCode>>() {
		}.getType();

		List<DamageCode> items = gson.fromJson(response, listType);
		return items;
	}

	@Override
	public List<RepairCode> getRepairCodes(Context ctx)
			throws NoConnectionException {

		if (Utils.hasNoConnection(ctx)) {
			throw new NoConnectionException();
		}

		HashMap<String, String> headers = prepareHeadersWithToken(ctx);
		String response = requestWrapper.sendGet(
				CJayConstant.LIST_REPAIR_CODES, headers);
		Gson gson = new Gson();
		Type listType = new TypeToken<List<RepairCode>>() {
		}.getType();

		List<RepairCode> items = gson.fromJson(response, listType);
		return items;
	}

	@Override
	public List<ComponentCode> getComponentCodes(Context ctx)
			throws NoConnectionException {

		if (Utils.hasNoConnection(ctx)) {
			throw new NoConnectionException();
		}

		HashMap<String, String> headers = prepareHeadersWithToken(ctx);
		String response = requestWrapper.sendGet(
				CJayConstant.LIST_COMPONENT_CODES, headers);
		Gson gson = new Gson();
		Type listType = new TypeToken<List<ComponentCode>>() {
		}.getType();

		List<ComponentCode> items = gson.fromJson(response, listType);
		return items;
	}

	@Override
	public List<ContainerSession> getAllContainerSessions(Context ctx)
			throws NoConnectionException {

		Logger.Log(LOG_TAG, "getAllContainerSessions(Context ctx)");

		if (Utils.hasNoConnection(ctx)) {
			throw new NoConnectionException();
		}

		HashMap<String, String> headers = prepareHeadersWithToken(ctx);
		String response = requestWrapper.sendGet(
				CJayConstant.LIST_CONTAINER_SESSIONS, headers);

		Logger.Log(LOG_TAG, response);

		Gson gson = new GsonBuilder().setDateFormat(
				CJayConstant.CJAY_SERVER_DATETIME_FORMAT).create();

		Type listType = new TypeToken<List<TmpContainerSession>>() {
		}.getType();

		List<TmpContainerSession> tmpContainerSessions = null;
		try {
			tmpContainerSessions = gson.fromJson(response, listType);

		} catch (Exception e) {
			e.printStackTrace();
		}

		// Parse to `ContainerSession`
		List<ContainerSession> items = new ArrayList<ContainerSession>();
		try {
			ContainerSessionDaoImpl containerSessionDaoImpl = databaseManager
					.getHelper(ctx).getContainerSessionDaoImpl();

			if (null != tmpContainerSessions) {
				for (TmpContainerSession tmpSession : tmpContainerSessions) {
					ContainerSession containerSession = Mapper.getInstance()
							.toContainerSession(tmpSession, ctx);

					if (null != containerSession) {
						containerSessionDaoImpl
								.addContainerSession(containerSession);
						items.add(containerSession);
					}
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return items;
	}

	@Override
	public List<ContainerSession> getContainerSessions(Context ctx,
			int userRole, int filterStatus) throws NoConnectionException {

		Logger.Log(LOG_TAG, "getContainerSessions(Context ctx, int userRole)");

		if (Utils.hasNoConnection(ctx)) {
			throw new NoConnectionException();
		}

		HashMap<String, String> headers = prepareHeadersWithToken(ctx);

		String response = "";
		String url = "";
		switch (userRole) {
		case User.ROLE_REPAIR_STAFF:
			url = String
					.format(CJayConstant.LIST_CONTAINER_SESSIONS_REPORT_LIST_WITH_FILTER,
							Integer.toString(filterStatus));
			break;

		case User.ROLE_GATE_KEEPER:
			// simply send request to get all containers that automatically
			// filter by server with `check_out_time = null`

			url = CJayConstant.LIST_CONTAINER_SESSIONS;
			break;

		case User.ROLE_AUDITOR:
		default:

			url = String.format(
					CJayConstant.LIST_CONTAINER_SESSIONS_WITH_FILTER,
					Integer.toString(filterStatus));
			break;
		}

		response = requestWrapper.sendGet(url, headers);

		if (TextUtils.isEmpty(response)) {
			Logger.Log(
					LOG_TAG,
					"No new items for user role "
							+ Integer.toString(filterStatus));
		} else {
			Logger.Log(LOG_TAG, "Response: " + response);

			Gson gson = new GsonBuilder().setDateFormat(
					CJayConstant.CJAY_SERVER_DATETIME_FORMAT).create();

			Type listType = new TypeToken<List<TmpContainerSession>>() {
			}.getType();

			List<TmpContainerSession> tmpContainerSessions = null;
			try {
				tmpContainerSessions = gson.fromJson(response, listType);
			} catch (Exception e) {
				e.printStackTrace();
			}

			// Parse to `ContainerSession`
			List<ContainerSession> items = new ArrayList<ContainerSession>();
			try {
				ContainerSessionDaoImpl containerSessionDaoImpl = databaseManager
						.getHelper(ctx).getContainerSessionDaoImpl();

				if (tmpContainerSessions != null) {
					for (TmpContainerSession tmpSession : tmpContainerSessions) {
						ContainerSession containerSession = Mapper
								.getInstance().toContainerSession(tmpSession,
										ctx);

						if (null != containerSession) {
							containerSessionDaoImpl
									.addContainerSession(containerSession);
							items.add(containerSession);
						}
					}

				}

			} catch (SQLException e) {
				e.printStackTrace();
			}

			return items;
		}

		return null;
	}

	@Override
	public List<ContainerSession> getContainerSessions(Context ctx,
			int userRole, int filterStatus, Date date)
			throws NoConnectionException {

		Logger.Log(LOG_TAG,
				"getContainerSessions(Context ctx, int userRole, Date date)");

		if (Utils.hasNoConnection(ctx)) {
			throw new NoConnectionException();
		}

		List<ContainerSession> items = new ArrayList<ContainerSession>();
		String formatedDate = StringHelper.getTimestamp(
				CJayConstant.CJAY_SERVER_DATETIME_FORMAT, date);

		items = getContainerSessions(ctx, userRole, filterStatus, formatedDate);
		return items;
	}

	@Override
	public synchronized List<ContainerSession> getContainerSessions(
			Context ctx, int userRole, int filterStatus, String date)
			throws NoConnectionException {

		Logger.Log(LOG_TAG,
				"getContainerSessions(Context ctx, int userRole, String date");

		if (Utils.hasNoConnection(ctx)) {
			throw new NoConnectionException();
		}

		HashMap<String, String> headers = prepareHeadersWithToken(ctx);

		String response = "";
		String url = "";
		switch (userRole) {
		case User.ROLE_REPAIR_STAFF:
			url = String
					.format(CJayConstant.LIST_CONTAINER_SESSIONS_REPORT_LIST_WITH_FILTER_AND_DATETIME,
							Integer.toString(filterStatus), date);
			break;

		case User.ROLE_GATE_KEEPER:
			url = String.format(
					CJayConstant.LIST_CONTAINER_SESSIONS_WITH_DATETIME, date);
			break;

		case User.ROLE_AUDITOR:
		default:
			url = String
					.format(CJayConstant.LIST_CONTAINER_SESSIONS_WITH_FILTER_AND_DATETIME,
							Integer.toString(filterStatus), date);
			break;
		}

		response = requestWrapper.sendGet(url, headers);

		if (TextUtils.isEmpty(response)) {
			Logger.Log(LOG_TAG, "No new items from: " + date
					+ " for user role: " + Integer.toString(userRole));

		} else {
			Logger.Log(LOG_TAG, response);

			Gson gson = new GsonBuilder().setDateFormat(
					CJayConstant.CJAY_SERVER_DATETIME_FORMAT).create();

			Type listType = new TypeToken<List<TmpContainerSession>>() {
			}.getType();

			List<TmpContainerSession> tmpContainerSessions = null;
			try {
				tmpContainerSessions = gson.fromJson(response, listType);
			} catch (Exception e) {
				e.printStackTrace();
			}

			// Parse to `ContainerSession`
			List<ContainerSession> items = new ArrayList<ContainerSession>();
			try {
				ContainerSessionDaoImpl containerSessionDaoImpl = databaseManager
						.getHelper(ctx).getContainerSessionDaoImpl();

				if (tmpContainerSessions != null) {
					for (TmpContainerSession tmpSession : tmpContainerSessions) {
						ContainerSession containerSession = Mapper
								.getInstance().toContainerSession(tmpSession,
										ctx);

						if (null != containerSession) {
							containerSessionDaoImpl
									.addContainerSession(containerSession);
							items.add(containerSession);
						}
					}

				}

			} catch (SQLException e) {
				e.printStackTrace();
			}

			return items;
		}

		return null;
	}

	@Override
	public List<ContainerSession> getContainerSessions(Context ctx, Date date)
			throws NoConnectionException {

		Logger.Log(LOG_TAG, "getContainerSessions(Context ctx, Date date)");

		if (Utils.hasNoConnection(ctx)) {
			throw new NoConnectionException();
		}

		List<ContainerSession> items = new ArrayList<ContainerSession>();
		String formatedDate = StringHelper.getTimestamp(
				CJayConstant.CJAY_SERVER_DATETIME_FORMAT, date);

		items = getContainerSessions(ctx, formatedDate);
		return items;
	}

	@Override
	public List<ContainerSession> getContainerSessions(Context ctx, String date)
			throws NoConnectionException {

		Logger.Log(LOG_TAG, "getContainerSessions(Context ctx, String date)");

		if (Utils.hasNoConnection(ctx)) {
			throw new NoConnectionException();
		}

		HashMap<String, String> headers = prepareHeadersWithToken(ctx);

		String response = requestWrapper.sendGet(String.format(
				CJayConstant.LIST_CONTAINER_SESSIONS_WITH_DATETIME, date),
				headers);

		if (TextUtils.isEmpty(response)) {
			Logger.Log(LOG_TAG, "No new items from " + date);
		} else {
			Logger.Log(LOG_TAG, response);

			Gson gson = new GsonBuilder().setDateFormat(
					CJayConstant.CJAY_SERVER_DATETIME_FORMAT).create();

			Type listType = new TypeToken<List<TmpContainerSession>>() {
			}.getType();

			List<TmpContainerSession> tmpContainerSessions = null;
			try {
				tmpContainerSessions = gson.fromJson(response, listType);
			} catch (Exception e) {
				e.printStackTrace();
			}

			// Parse to `ContainerSession`
			List<ContainerSession> items = new ArrayList<ContainerSession>();
			try {
				ContainerSessionDaoImpl containerSessionDaoImpl = databaseManager
						.getHelper(ctx).getContainerSessionDaoImpl();

				if (tmpContainerSessions != null) {
					for (TmpContainerSession tmpSession : tmpContainerSessions) {
						ContainerSession containerSession = Mapper
								.getInstance().toContainerSession(tmpSession,
										ctx);

						if (null != containerSession) {
							containerSessionDaoImpl
									.addContainerSession(containerSession);
							items.add(containerSession);
						}
					}

				}

			} catch (SQLException e) {
				e.printStackTrace();
			}

			return items;
		}

		return null;
	}

	@Override
	public List<Operator> getOperators(Context ctx, Date date) {
		return null;
	}

	@Override
	public List<Operator> getOperators(Context ctx, String date) {
		return null;
	}

	@Override
	public boolean hasNewMetadata(Context ctx) {
		return false;
	}

	@Override
	public List<CJayResourceStatus> getCJayResourceStatus(Context ctx)
			throws NoConnectionException {

		if (Utils.hasNoConnection(ctx)) {
			throw new NoConnectionException();
		}

		HashMap<String, String> headers = prepareHeadersWithToken(ctx);
		String response = requestWrapper.sendGet(
				CJayConstant.CJAY_RESOURCE_STATUS, headers);

		Gson gson = new Gson();
		Type listType = new TypeToken<List<CJayResourceStatus>>() {
		}.getType();

		List<CJayResourceStatus> items = gson.fromJson(response, listType);
		return items;
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
			String url = CJayConstant.CJAY_ITEMS;
			ret = requestWrapper.sendPost(url, data, "application/json",
					headers);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return ret;
	}

	@Override
	public String postContainerSessionReportList(Context ctx,
			TmpContainerSession item) throws NoConnectionException {

		if (Utils.hasNoConnection(ctx)) {
			Logger.Log("Network is not available");
			throw new NoConnectionException();
		}

		String ret = "";
		try {
			HashMap<String, String> headers = prepareHeadersWithToken(ctx);
			Gson gson = new Gson();

			String data = gson.toJson(item);
			String url = CJayConstant.LIST_CONTAINER_SESSIONS_REPORT_LIST;
			ret = requestWrapper.sendPost(url, data, "application/json",
					headers);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;

	}
}
