package com.cloudjay.cjay.network;

import com.cloudjay.cjay.model.IsoCode;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.util.ApiEndpoint;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

public interface NetworkService {

	@FormUrlEncoded
	@POST(ApiEndpoint.TOKEN_API)
	public JsonObject getToken(@Field("username") String username, @Field("password") String password);

	@GET(ApiEndpoint.CURRENT_USER_API)
	public User getCurrentUser(@Header("Authorization") String token, @Header("CJAY_VERSION") String cJayVersion);

	@GET(ApiEndpoint.LIST_REPAIR_CODES_API)
	public List<IsoCode> getRepairCodes(@Header("Authorization") String token, @Header("CJAY_VERSION") String cJayVersion, @Query("modified_after") String lastModifiedDate);

	@GET(ApiEndpoint.LIST_DAMAGE_CODES_API)
	public List<IsoCode> getDamageCodes(@Header("Authorization") String token, @Header("CJAY_VERSION") String cJayVersion, @Query("modified_after") String lastModifiedDate);

	@GET(ApiEndpoint.LIST_COMPONENT_CODES_API)
	public List<IsoCode> getComponentCodes(@Header("Authorization") String token, @Header("CJAY_VERSION") String cJayVersion, @Query("modified_after") String lastModifiedDate);

	@GET(ApiEndpoint.LIST_OPERATORS_API)
	public List<Operator> getOperators(@Header("Authorization") String token, @Header("CJAY_VERSION") String cJayVersion, @Query("modified_after") String lastModifiedDate);

	@GET(ApiEndpoint.CONTAINER_SESSIONS_API)
	public List<Session> getContainerSessionsByPage(@Header("Authorization") String token, @Header("CJAY_VERSION") String cJayVersion, @Query("page") int page, @Query("modified_after") String lastModifiedDate);

	//TODO edit getContainerSessionById, postContainer and postImageFile

	@GET(ApiEndpoint.CONTAINER_SESSION_ITEM_API)
	public Session getContainerSessionById(@Header("Authorization") String token, @Header("CJAY_VERSION") String cJayVersion, @Path("id") int containerId);

	@POST(ApiEndpoint.CONTAINER_SESSIONS_API)
	public void postContainer(@Header("Authorization") String token, @Header("CJAY_VERSION") String cJayVersion, @Field("username") String username, @Field("password") String password);

	// Check source v1, uploadType=media
	@POST(ApiEndpoint.CJAY_TMP_STORAGE)
	public void postImageFile(String uploadType, String name);
}