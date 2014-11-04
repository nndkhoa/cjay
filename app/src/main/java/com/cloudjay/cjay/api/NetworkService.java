package com.cloudjay.cjay.api;

import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.model.IsoCode;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.model.User;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.mime.TypedFile;

public interface NetworkService {

	@FormUrlEncoded
	@POST(ApiEndpoint.TOKEN_API)
	public JsonObject getToken(@Field("username") String username, @Field("password") String password);

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

	@GET(ApiEndpoint.CONTAINER_SESSIONS_API)
	public JsonObject getContainerSessionsByModifiedTime(@Query("modified_after") String lastModifiedDate);

	@GET(ApiEndpoint.CONTAINER_SESSION_ITEM_API)
	public Session getContainerSessionById(@Path("id") long containerId);

	@GET(ApiEndpoint.CONTAINER_SESSIONS_API)
	public JsonObject searchContainer(@Query("search") String keyword);

	@POST(ApiEndpoint.CONTAINER_SESSIONS_API)
	public Session postContainer(@Body JsonObject jsonSessionString);

	@PUT(ApiEndpoint.CONTAINER_SESSION_CHECK_OUT_API)
	public Session checkOutContainerSession(@Path("id") long containerPk, @Body JsonArray jsonGateImage);

	@PUT(ApiEndpoint.CONTAINER_SESSION_COMPLETE_AUDIT_API)
	public Session completeAudit(@Path("id") long containerPk);

	@PUT(ApiEndpoint.CONTAINER_SESSION_COMPLETE_REPAIR_API)
	public Session completeRepair(@Path("id") long containerPk, @Body JsonArray audit_items);

	@PUT(ApiEndpoint.CONTAINER_SESSION_POST_AUDIT_ITEM_API)
	public Session postAudiItem(@Path("id") long containerPk, @Body JsonObject audit_item);

	@PUT(ApiEndpoint.CONTAINER_SESSION_ADD_AUDIT_IMAGES_API)
	public AuditItem addAuditImages(@Path("id") String auditId, @Body JsonArray auditImages);

	@PUT(ApiEndpoint.CONTAINER_SESSION_HAND_CLEANING)
	public Session setHandCleaningSession(@Path("id") long containerPk);

	// Check source v1, uploadType=media
	@POST(ApiEndpoint.CJAY_TMP_STORAGE_IMAGE)
	public Response postImageFile(@Header("Content-Type") String contentType, @Query("uploadType") String uploadType, @Query("name") String imageName, @Body() TypedFile image);

	//Use for PubNub
	@GET(ApiEndpoint.PUBNUB_AUDIT_ITEM)
	public AuditItem getAuditItemById(@Path("id") long id);

	@GET(ApiEndpoint.PUBNUB_DAMAGE_CODE)
	public IsoCode getDamageCodesById(@Path("id") long id);

	@GET(ApiEndpoint.PUBNUB_REPAIR_CODE)
	public IsoCode getRepairCodeById(@Path("id") long id);

	@GET(ApiEndpoint.PUBNUB_COMPONENT_CODE)
	public IsoCode getComponentCodeById(@Path("id") long id);

	@GET(ApiEndpoint.PUBNUB_OPERATOR)
	Operator getOperatorById(@Path("id") long id);

	@FormUrlEncoded
	@POST(ApiEndpoint.PUBNUB_GOT_MESSAGE)
	public Response gotMessageFromPubNub(@Field("receiver_channel") String channel, @Field("message_id") String messageId);
}