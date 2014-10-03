package com.cloudjay.cjay.api;

import com.cloudjay.cjay.model.User;

import retrofit.RestAdapter;

public class CustomRestAdapter {
	public RestAdapter getRestAdapter(String endpoint) {

		// TODO: Get current user token
		User user = null;

		// Init header, pass 3 params: token, app version, username
		String appVersion = "v2.0-alpha";
		String username = user.getUsername();
		String token = user.getToken();

		ApiHeaders headers = new ApiHeaders(token, appVersion, username);
		return new RestAdapter.Builder().setEndpoint(endpoint).setRequestInterceptor(headers).build();
	}
}