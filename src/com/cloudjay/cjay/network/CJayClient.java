package com.cloudjay.cjay.network;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.provider.Settings.Secure;
import android.util.Log;

import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.DamageCode;
import com.cloudjay.cjay.model.IDatabaseManager;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.model.RepairCode;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.CredentialManager;
import com.cloudjay.cjay.util.Logger;

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
		Gson gson = new Gson();
		currentUser = gson.fromJson(
				CredentialManager.getPrefsValue(ctx, CredentialManager.USER),
				User.class);

		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Authorization", "Token " + currentUser.getAccessToken());
		return headers;
	}

	private HashMap<String, String> prepareHeadersWithToken(String token) {

		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Authorization", "Token " + token);
		return headers;
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
		String response = requestWrapper.sendGet(CJayConstant.LIST_OPERATORS,
				headers);

		Logger.Log(response);

		Gson gson = new Gson();
		Type listType = new TypeToken<List<ContainerSession>>() {
		}.getType();

		List<ContainerSession> items = gson.fromJson(response, listType);
		return items;
	}

	@Override
	public List<Operator> getOperators(Context ctx, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Operator> getOperators(Context ctx, String date) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean checkIfServerHasNewMetadata() {
		return false;
	}
}
