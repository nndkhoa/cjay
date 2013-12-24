package com.cloudjay.cjay.network;

import java.lang.reflect.Type;
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
import android.util.Log;

import com.cloudjay.cjay.CJayActivity;
import com.cloudjay.cjay.LoginActivity;
import com.cloudjay.cjay.dao.CJayImageDaoImpl;
import com.cloudjay.cjay.dao.ContainerDaoImpl;
import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.dao.DamageCodeDaoImpl;
import com.cloudjay.cjay.dao.DepotDaoImpl;
import com.cloudjay.cjay.dao.OperatorDaoImpl;
import com.cloudjay.cjay.dao.RepairCodeDaoImpl;
import com.cloudjay.cjay.model.AuditReportItem;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.CJayResourceStatus;
import com.cloudjay.cjay.model.Container;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.DamageCode;
import com.cloudjay.cjay.model.Depot;
import com.cloudjay.cjay.model.GateReportImage;
import com.cloudjay.cjay.model.IDatabaseManager;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.model.RepairCode;
import com.cloudjay.cjay.model.TmpContainerSession;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.PreferencesUtil;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.Session;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

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

		// User currentUser = null;
		// currentUser = Session.restore(ctx).getCurrentUser();

		User currentUser = ((CJayActivity) ctx).getCurrentUser();
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Authorization", "Token " + currentUser.getAccessToken());
		return headers;
	}

	private HashMap<String, String> prepareHeadersWithToken(String token) {

		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Authorization", "Token " + token);
		return headers;
	}

	public void fetchData(Context ctx) {
		Logger.Log(LOG_TAG, "fetching data ...");

		try {

			Date now = new Date();

			// 2013-11-10T21:05:24+08:00
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"yyyy-MM-dd'T'HH:mm:ssZZ");
			String nowString = dateFormat.format(now);

			// 1. chưa có data
			Logger.Log(LOG_TAG, "no iso code");
			OperatorDaoImpl operatorDaoImpl = databaseManager.getHelper(ctx)
					.getOperatorDaoImpl();
			DamageCodeDaoImpl damageCodeDaoImpl = databaseManager
					.getHelper(ctx).getDamageCodeDaoImpl();
			RepairCodeDaoImpl repairCodeDaoImpl = databaseManager
					.getHelper(ctx).getRepairCodeDaoImpl();

			if (operatorDaoImpl.isEmpty()) {
				Logger.Log(LOG_TAG, "get list operators");
				PreferencesUtil.storePrefsValue(ctx,
						PreferencesUtil.RESOURCE_OPERATOR_LAST_UPDATE,
						nowString);

				List<Operator> operators = getOperators(ctx);
				operatorDaoImpl.addListOperators(operators);
			}

			if (damageCodeDaoImpl.isEmpty()) {
				Logger.Log(LOG_TAG, "get list damage codes");
				PreferencesUtil.storePrefsValue(ctx,
						PreferencesUtil.RESOURCE_DAMAGE_LAST_UPDATE, nowString);
				List<DamageCode> damageCodes = getDamageCodes(ctx);
				damageCodeDaoImpl.addListDamageCodes(damageCodes);
			}

			if (repairCodeDaoImpl.isEmpty()) {
				Logger.Log(LOG_TAG, "get list repair codes");
				PreferencesUtil.storePrefsValue(ctx,
						PreferencesUtil.RESOURCE_REPAIR_LAST_UPDATE, nowString);
				List<RepairCode> repairCodes = getRepairCodes(ctx);
				repairCodeDaoImpl.addListRepairCodes(repairCodes);
			}

			// 2. fetch ISO CODE
			if (hasNewMetadata(ctx)) {
				Logger.Log(LOG_TAG, "fetch iso code");
			}

			ContainerSessionDaoImpl containerSessionDaoImpl = databaseManager
					.getHelper(ctx).getContainerSessionDaoImpl();

			// Update list ContainerSessions
			Logger.Log(LOG_TAG, "get list container sessions");
			if (containerSessionDaoImpl.isEmpty()) { // temporary
				List<ContainerSession> containerSessions = getContainerSessions(ctx);
				containerSessionDaoImpl
						.addListContainerSessions(containerSessions);
			} else {
				// List<ContainerSession> containerSessions = getClass()

			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getUserToken(String username, String password, Context ctx)
			throws JSONException {

		Logger.Log("getting User Token ... ");

		JSONObject requestPacket = new JSONObject();
		requestPacket.put("username", username);
		requestPacket.put("password", password);

		Logger.Log(CJayConstant.TOKEN);

		String tokenResponseString = requestWrapper.sendJSONPost(
				CJayConstant.TOKEN, requestPacket);

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
	public String getGoogleCloudToken(String token) {
		HashMap<String, String> headers = prepareHeadersWithToken(token);
		String response = requestWrapper.sendGet(
				CJayConstant.API_GOOGLE_CLOUD_STORAGE_TOKEN, headers);
		return response;
	}

	@Override
	public void addGCMDevice(String regid, Context ctx) throws JSONException {
		JSONObject requestPacket = new JSONObject();
		requestPacket.put("registration_id", regid);
		String androidId = Secure.getString(ctx.getContentResolver(),
				Secure.ANDROID_ID);

		UUID deviceUuid = new UUID(androidId.hashCode(), androidId.hashCode());
		String deviceId = deviceUuid.toString();
		requestPacket.put("device_id", deviceId);
		requestPacket.put("name", android.os.Build.MODEL);

		HashMap<String, String> headers = prepareHeadersWithToken(ctx);

		String response = requestWrapper.sendJSONPost(
				CJayConstant.API_ADD_GCM_DEVICE, requestPacket, headers);
		Log.i("GCM", response);
	}

	// @Override
	// public List<ItemModel> getNewItems(Context ctx) {
	// HashMap<String, String> headers = prepareHeadersWithToken(ctx);
	// String response = requestWrapper.sendGet(
	// "https://cloudjay-web.appspot.com/api/jaypix/jaypix-items",
	// headers);
	//
	// Gson gson = new Gson();
	// Type listType = new TypeToken<List<ItemModel>>() {
	// }.getType();
	// List<ItemModel> items = gson.fromJson(response, listType);
	// return items;
	// }
	//
	// @Override
	// public ItemTeamResultModel getNewItemsByTeam(Context ctx,
	// UserModel currentUser, int page) {
	// HashMap<String, String> headers = prepareHeadersWithToken(ctx);
	// String formatStr =
	// "https://cloudjay-web.appspot.com/api/jaypix/jaypix-items-by-team?team_id=%s&page=%s";
	// String url = String.format(formatStr, currentUser.getTeams().get(0)
	// .getId(), page);
	// String response = requestWrapper.sendGet(url, headers);
	//
	// Gson gson = new Gson();
	// Type listType = new TypeToken<ItemTeamResultModel>() {
	// }.getType();
	// ItemTeamResultModel result = gson.fromJson(response, listType);
	// for (ItemModel item : result.getResults()) {
	// String imageUrl = item.getImageUrl();
	// item.setImageUrl(StringHelper.addThumbExtensionUrl(imageUrl));
	// }
	// return result;
	// }

	@Override
	public User getCurrentUser(String token, Context ctx) {
		Logger.Log("getting Current User ...");

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

	// @Override
	// public List<UserModel> getTeamMembers(UserModel currentUser, Context ctx)
	// throws SQLException {
	// ITeamDao teamDaoImpl = databaseManager.getHelper(ctx).getTeamImpl();
	// IUserDao userDaoImpl = databaseManager.getHelper(ctx).getUserImpl();
	// teamDaoImpl.deleteAllTeams();
	//
	// HashMap<String, String> headers = new HashMap<String, String>();
	// headers.put("Authorization", "Token " + currentUser.getToken());
	//
	// List<UserModel> teamMembers = new ArrayList<UserModel>();
	// List<TeamModel> teams = currentUser.getTeams();
	// for (TeamModel teamModel : teams) {
	// String url =
	// "https://cloudjay-web.appspot.com/api/jaypix/jaypix-users-by-team.json?team_id="
	// + teamModel.getId();
	// String response = requestWrapper.sendGet(url, headers);
	// Gson gson = new Gson();
	// Type userListType = new TypeToken<List<UserModel>>() {
	// }.getType();
	// List<UserModel> tempTeamMembers = gson.fromJson(response,
	// userListType);
	// teamMembers.addAll(tempTeamMembers);
	//
	// Team team = new Team();
	// team.setId(teamModel.getId());
	// team.setTeam_code(teamModel.getTeamCode());
	// team.setTeam_name(teamModel.getTeamName());
	// teamDaoImpl.addTeam(team);
	//
	// for (UserModel userModel : tempTeamMembers) {
	// User user = MapperUtils.ToUser(userModel);
	// user.setTeam(team);
	// userDaoImpl.addUser(user);
	// }
	// System.out.println();
	// }
	// currentUser.setSameTeamMembers(teamMembers);
	// return teamMembers;
	// }
	//
	// @Override
	// public void uploadItem(Context ctx, ItemModel item) {
	//
	// try {
	// if (NetworkHelper.isConnected(ctx)) {
	// HashMap<String, String> headers = prepareHeadersWithToken(ctx);
	// Gson gson = new Gson();
	// String data = gson.toJson(item);
	// String url = JayPixMetaData.JAYPIX_ITEMS;
	// requestWrapper.sendPost(url, data, "application/json", headers);
	// } else {
	// Logger.Log("Network is not available");
	// UIHelper.toast(ctx, "Network is not available");
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	//
	// }
	//
	// @Override
	// public ItemModel getItemModel(Context ctx, UserModel currentUser, int
	// itemId) {
	// // TODO Auto-generated method stub
	// return null;
	// }

	@Override
	public List<Operator> getOperators(Context ctx) {
		HashMap<String, String> headers = null;
		String response = null;

		try {
			headers = prepareHeadersWithToken(ctx);
			response = requestWrapper.sendGet(CJayConstant.LIST_OPERATORS,
					headers);
		} catch (Exception e) {
			// Logout User then back to Login Activity

		}

		Gson gson = new Gson();
		Type listType = new TypeToken<List<Operator>>() {
		}.getType();

		List<Operator> items = gson.fromJson(response, listType);
		return items;
	}

	@Override
	public List<DamageCode> getDamageCodes(Context ctx) {
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
	public List<RepairCode> getRepairCodes(Context ctx) {
		HashMap<String, String> headers = prepareHeadersWithToken(ctx);
		String response = requestWrapper.sendGet(CJayConstant.LIST_OPERATORS,
				headers);
		Gson gson = new Gson();
		Type listType = new TypeToken<List<RepairCode>>() {
		}.getType();

		List<RepairCode> items = gson.fromJson(response, listType);
		return items;
	}

	@Override
	public List<ContainerSession> getContainerSessions(Context ctx) {
		HashMap<String, String> headers = prepareHeadersWithToken(ctx);
		String response = requestWrapper.sendGet(
				CJayConstant.LIST_CONTAINER_SESSIONS, headers);

		Logger.Log(LOG_TAG, response);

		Gson gson = new Gson();
		Type listType = new TypeToken<List<TmpContainerSession>>() {
		}.getType();

		List<TmpContainerSession> tmpContainerSessions = gson.fromJson(
				response, listType);

		// Parse to `ContainerSession`
		List<ContainerSession> items = new ArrayList<ContainerSession>();
		try {
			ContainerDaoImpl containerDaoImpl = databaseManager.getHelper(ctx)
					.getContainerDaoImpl();
			DepotDaoImpl depotDaoImpl = databaseManager.getHelper(ctx)
					.getDepotDaoImpl();
			OperatorDaoImpl operatorDaoImpl = databaseManager.getHelper(ctx)
					.getOperatorDaoImpl();
			ContainerSessionDaoImpl containerSessionDaoImpl = databaseManager
					.getHelper(ctx).getContainerSessionDaoImpl();
			CJayImageDaoImpl cJayImageDaoImpl = databaseManager.getHelper(ctx)
					.getCJayImageDaoImpl();

			for (TmpContainerSession tmpSession : tmpContainerSessions) {

				// Create `operator` object if needed
				Operator operator = null;
				List<Operator> listOperators = operatorDaoImpl.queryForEq(
						Operator.CODE, tmpSession.getOperatorCode());

				if (listOperators.isEmpty()) {
					operator = new Operator();
					operator.setCode(tmpSession.getOperatorCode());
					operator.setName(tmpSession.getOperatorCode());
					operatorDaoImpl.addOperator(operator);
				}

				// Create `depot` object if needed
				Depot depot = null;
				List<Depot> listDepots = depotDaoImpl.queryForEq(
						Depot.DEPOT_CODE, tmpSession.getDepotCode());
				if (listDepots.isEmpty()) {
					depot = new Depot();
					depot.setDepotCode(tmpSession.getDepotCode());
					depot.setDepotName(tmpSession.getDepotCode());
					depotDaoImpl.addDepot(depot);
				}

				// Create `container` object if needed
				Container container = null;
				List<Container> list = containerDaoImpl.queryForEq(
						Container.CONTAINER_ID, tmpSession.getContainerId());
				if (list.isEmpty()) {
					container = new Container();
					container.setContainerId(tmpSession.getContainerId());
					if (null != operator)
						container.setOperator(operator);

					if (null != depot)
						container.setDepot(depot);

					containerDaoImpl.addContainer(container);
				}

				// Create `container session` object
				ContainerSession containerSession = new ContainerSession();
				containerSession.setId(tmpSession.getId());
				containerSession.setCheckInTime(tmpSession.getCheckInTime());
				containerSession.setCheckOutTime(tmpSession.getCheckOutTime());
				containerSession.setImageIdPath(tmpSession.getImageIdPath());
				if (null != container)
					containerSession.setContainer(container);

				containerSessionDaoImpl.addContainerSessions(containerSession);

				// process audit report item

				for (AuditReportItem auditReportItem : tmpSession
						.getAuditReportItems()) {

				}

				// process gate report images
				List<CJayImage> listImages = new ArrayList<CJayImage>();
				for (GateReportImage gateReportImage : tmpSession
						.getGateReportImages()) {

					CJayImage image = new CJayImage(gateReportImage.getId(),
							gateReportImage.getType(),
							gateReportImage.getTimePosted(),
							gateReportImage.getImageName());

					if (null != image)
						image.setContainerSession(containerSession);

					cJayImageDaoImpl.addCJayImage(image);

					// data for returning
					listImages.add(image);
				}

				containerSession.setCJayImages(listImages);

				// data for returning
				items.add(containerSession);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return items;
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

		// List<CJayResourceStatus> cJayResourceStatus =
		// getCJayResourceStatus(ctx);
		// for (CJayResourceStatus item : cJayResourceStatus) {
		//
		// }

		return false;
	}

	@Override
	public List<ContainerSession> getContainerSessions(Context ctx, Date date) {

		return null;
	}

	@Override
	public List<ContainerSession> getContainerSessions(Context ctx, String date) {

		return null;
	}

	@Override
	public List<CJayResourceStatus> getCJayResourceStatus(Context ctx) {
		HashMap<String, String> headers = prepareHeadersWithToken(ctx);
		String response = requestWrapper.sendGet(
				CJayConstant.CJAY_RESOURCE_STATUS, headers);

		Gson gson = new Gson();
		Type listType = new TypeToken<List<CJayResourceStatus>>() {
		}.getType();

		List<CJayResourceStatus> items = gson.fromJson(response, listType);
		return items;
	}
}
