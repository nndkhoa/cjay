package com.cloudjay.cjay.network;

import com.cloudjay.cjay.util.ApiEndpoint;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.Query;

public interface NetworkService {

	@FormUrlEncoded
	@POST(ApiEndpoint.TOKEN_API)
	public JsonObject getToken(@Field("username") String username, @Field("password") String password);

	@GET(ApiEndpoint.CURRENT_USER_API)
	public JsonObject getCurrentUser(@Header("Authorization") String token, @Header("CJAY_VERSION") String cJayVersion);

	@GET(ApiEndpoint.LIST_REPAIR_CODES_API)
	public JsonArray getRepairCodes(@Header("Authorization") String token, @Header("CJAY_VERSION") String cJayVersion, @Query("modified_after") String lastModifiedDate);

	@GET(ApiEndpoint.LIST_DAMAGE_CODES_API)
	public JsonArray getDamageCodes(@Header("Authorization") String token, @Header("CJAY_VERSION") String cJayVersion, @Query("modified_after") String lastModifiedDate);

	@GET(ApiEndpoint.LIST_COMPONENT_CODES_API)
	public JsonArray getComponentCodes(@Header("Authorization") String token, @Header("CJAY_VERSION") String cJayVersion, @Query("modified_after") String lastModifiedDate);

	@GET(ApiEndpoint.LIST_OPERATORS_API)
	public JsonArray getOperators(@Header("Authorization") String token, @Header("CJAY_VERSION") String cJayVersion, @Query("modified_after") String lastModifiedDate);

	@GET(ApiEndpoint.CONTAINER_SESSIONS_API)
	public JsonArray getContainerSessionsByPage(@Header("Authorization") String token, @Header("CJAY_VERSION") String cJayVersion, @Query("page") int page, @Query("modified_after") String lastModifiedDate);

	//TODO edit getContainerSessionById, postContainer and postImageFile
	@GET(ApiEndpoint.CONTAINER_SESSION_ITEM_API)
	public JsonArray getContainerSessionById(@Header("Authorization") String token, @Header("CJAY_VERSION") String cJayVersion, @Query("page") int containerId, @Query("modified_after") String lastModifiedDate);

	@POST(ApiEndpoint.CONTAINER_SESSIONS_API)
	public void postContainer(@Header("Authorization") String token, @Header("CJAY_VERSION") String cJayVersion, @Field("username") String username, @Field("password") String password);

	// Check source v1, uploadType=media
	@POST(ApiEndpoint.CJAY_TMP_STORAGE)
	public void postImageFile(String uploadType, String name);
}