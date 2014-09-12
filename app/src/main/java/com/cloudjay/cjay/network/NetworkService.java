package com.cloudjay.cjay.network;

import com.cloudjay.cjay.util.ApiEndpoint;
import com.google.gson.JsonObject;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

public interface NetworkService {

	@FormUrlEncoded
	@POST(ApiEndpoint.TOKEN_API)
	public void getToken(@Field("username") String username, @Field("password") String password, Callback<JsonObject> callback);

	public void postContainer();
	public void getCurrentUser();


}