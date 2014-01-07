package com.cloudjay.cjay.network;

import java.net.SocketTimeoutException;
import java.util.Map;

import org.json.JSONObject;

public interface IHttpRequestWrapper {
	String sendPost(String url, String data) throws SocketTimeoutException;

	String sendJSONPost(String url, JSONObject data)
			throws SocketTimeoutException;

	String sendJSONPost(String url, JSONObject data, Map<String, String> headers)
			throws SocketTimeoutException;

	String sendPost(String url, String data, String contentType)
			throws SocketTimeoutException;

	String sendPost(String url, String data, String contentType,
			Map<String, String> headers) throws SocketTimeoutException;

	String sendGet(String url);

	String sendGet(String url, Map<String, String> headers);
}
