package com.cloudjay.cjay.data.api;

import com.cloudjay.cjay.data.model.IsoCode;
import com.cloudjay.cjay.data.model.Operator;
import com.cloudjay.cjay.data.model.Session;
import com.cloudjay.cjay.data.model.User;
import com.google.gson.JsonObject;

import java.util.List;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

public interface NetworkService {

	@FormUrlEncoded
	@POST(ApiEndpoint.TOKEN_API)
	public JsonObject getToken(@Field("password") String password);

	@GET(ApiEndpoint.CURRENT_USER_API)
	public User getCurrentUser();

	@GET(ApiEndpoint.LIST_REPAIR_CODES_API)
	public List<IsoCode> getRepairCodes(@Query("modified_after") String lastModifiedDate);

	@GET(ApiEndpoint.LIST_DAMAGE_CODES_API)
	public List<IsoCode> getDamageCodes(@Query("modified_after") String lastModifiedDate);

	@GET(ApiEndpoint.LIST_COMPONENT_CODES_API)
	public List<IsoCode> getComponentCodes(@Query("modified_after") String lastModifiedDate);

	@GET(ApiEndpoint.LIST_OPERATORS_API)
	public List<Operator> getOperators(@Query("modified_since") String lastModifiedDate);

	@GET(ApiEndpoint.CONTAINER_SESSIONS_API)
	public JsonObject getContainerSessionsByPage(@Query("page") int page, @Query("modified_after") String lastModifiedDate);

	//TODO edit getContainerSessionById, postContainer and postImageFile

	@GET(ApiEndpoint.CONTAINER_SESSION_ITEM_API)
	public Session getContainerSessionById(@Path("id") int containerId);

	@POST(ApiEndpoint.CONTAINER_SESSIONS_API)
	public void postContainer(@Field("username") String username, @Field("password") String password);

	// Check source v1, uploadType=media
	@POST(ApiEndpoint.CJAY_TMP_STORAGE)
	public void postImageFile(String uploadType, String name);
}