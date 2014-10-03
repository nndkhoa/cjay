package com.cloudjay.cjay.data.util;

import com.cloudjay.cjay.data.api.NetworkService;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DataCenter {

	private final NetworkService networkService;

	@Inject
	public DataCenter(NetworkService networkService) {
		this.networkService = networkService;
	}
}