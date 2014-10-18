package com.cloudjay.cjay.api;

import com.cloudjay.cjay.model.IsoCode;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.model.User;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import retrofit.Callback;
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
    public Session getContainerSessionById(@Path("id") int containerId);

    @GET(ApiEndpoint.CONTAINER_SESSIONS_API)
    public JsonObject searchContainer(@Query("search") String keyword);

    @POST(ApiEndpoint.CONTAINER_SESSIONS_API)
    public Session postContainer( @Body JsonObject jsonSessionString);

    @PUT(ApiEndpoint.CONTAINER_SESSION_CHECK_OUT_API)
    public void checkOutContainerSession(@Path("id") String containerId, @Query("gate_images") String gate_image);

    @PUT(ApiEndpoint.CONTAINER_SESSION_COMPLETE_AUDIT_API)
    public void completeAudit(@Path("id") String containerId);

    @PUT(ApiEndpoint.CONTAINER_SESSION_COMPLETE_REPAIR_API)
    public void completeRepair(@Path("id") String containerId, @Query("audit_items") String audit_item);

    @PUT(ApiEndpoint.CONTAINER_SESSION_POST_AUDIT_ITEM_API)
    public void postAudiItem(@Path("id") String containerId, @Query("damage_code_id") String damage_code_id, @Query("repair_code_id") String repair_code_id, @Query("component_code_id") String component_code_id, @Query("location_code") String location_code, @Query("length") Long length, @Query("height") Long height, @Query("quantity") Long quantity, @Query("audit_images") String audit_images);

    // Check source v1, uploadType=media
    @POST(ApiEndpoint.CJAY_TMP_STORAGE_IMAGE)
    public Response postImageFile(@Header("Content-Type") String contentType, @Query("uploadType") String uploadType, @Query("name") String imageName, @Body() TypedFile image);
}