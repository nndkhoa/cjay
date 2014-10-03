package com.cloudjay.cjay.data.api;

import retrofit.RequestInterceptor;

public final class ApiHeaders implements RequestInterceptor {
	private static final String AUTHORIZATION_PREFIX = "Token";
	private final String authorizationValue;

	public ApiHeaders(String clientId) {
		authorizationValue = AUTHORIZATION_PREFIX + " " + clientId;
	}

	@Override
	public void intercept(RequestFacade request) {
		request.addHeader("Authorization", authorizationValue);
		request.addHeader("CJAY_VERSION", "TEST");
	}
}
