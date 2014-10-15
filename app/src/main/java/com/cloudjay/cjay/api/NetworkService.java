package com.cloudjay.cjay.api;

import com.cloudjay.cjay.model.IsoCode;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.model.User;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
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
    public void postContainer(@Body Session session, Callback<Response> responseCallback);

    // Check source v1, uploadType=media
    @POST(ApiEndpoint.CJAY_TMP_STORAGE_IMAGE)
    public void postImageFile(@Header("Content-Type") String contentType, @Query("uploadType") String uploadType, @Query("name") String imageName, @Body() TypedFile image, Callback<Response> responseCallback);
}