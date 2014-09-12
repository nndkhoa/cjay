package com.cloudjay.cjay.network;

import com.cloudjay.cjay.util.ApiEndpoint;
import com.google.gson.JsonObject;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;

public interface NetworkService {

	@FormUrlEncoded
	@POST(ApiEndpoint.TOKEN_API)
	public void getToken(@Field("username") String username, @Field("password") String password, Callback<JsonObject> callback);

	@GET(ApiEndpoint.CURRENT_USER_API)
	public void getCurrentUser();

	@GET(ApiEndpoint.LIST_REPAIR_CODES_API)
	public void getRepairCodes(String lastModifiedDate);

	@GET(ApiEndpoint.LIST_DAMAGE_CODES_API)
	public void getDamageCodes(String lastModifiedDate);

	@GET(ApiEndpoint.LIST_COMPONENT_CODES_API)
	public void getComponentCodes(String lastModifiedDate);

	@GET(ApiEndpoint.LIST_OPERATORS_API)
	public void getOperators(String lastModifiedDate);

	@GET(ApiEndpoint.CONTAINER_SESSIONS_API)
	public void getContainerSessionsByPage(int page);

	@GET(ApiEndpoint.CONTAINER_SESSION_ITEM_API)
	public void getContainerSessionById(int containerId);

	@POST(ApiEndpoint.CONTAINER_SESSIONS_API)
	public void postContainer();

	// Check source v1, uploadType=media
	@POST(ApiEndpoint.CJAY_TMP_STORAGE)
	public void postImageFile(String uploadType, String name);
}