package com.cloudjay.cjay.api;

import android.text.TextUtils;

import retrofit.RequestInterceptor;

public class ApiHeaders implements RequestInterceptor {

	private static final String AUTHORIZATION_PREFIX = "Token";
	private String authorizationValue = null;
	private String appVersion = null;
	private String username = null;

	public ApiHeaders(String token, String appVersion, String username) {
		if (!TextUtils.isEmpty(token)) {
			authorizationValue = AUTHORIZATION_PREFIX + " " + token;
		}
		this.appVersion = appVersion;
		this.username = username;
	}

	@Override
	public void intercept(RequestFacade request) {
		request.addHeader("Authorization", authorizationValue);
		request.addHeader("CJAY_VERSION", appVersion);
		request.addHeader("CJAY_USERNAME", username);
	}
}