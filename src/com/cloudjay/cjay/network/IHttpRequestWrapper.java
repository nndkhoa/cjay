package com.cloudjay.cjay.network;

import java.net.SocketTimeoutException;
import java.util.Map;

import org.json.JSONObject;

import com.cloudjay.cjay.util.NoConnectionException;

/**
 * Early throws for compiled-time errors
 * 
 * @author tieubao
 * 
 */
public interface IHttpRequestWrapper {

	String sendPost(String url, String data) throws SocketTimeoutException,
			NoConnectionException;

	String sendJSONPost(String url, JSONObject data)
			throws SocketTimeoutException, NoConnectionException;

	String sendJSONPost(String url, JSONObject data, Map<String, String> headers)
			throws SocketTimeoutException, NoConnectionException;

	String sendPost(String url, String data, String contentType)
			throws SocketTimeoutException, NoConnectionException;

	String sendPost(String url, String data, String contentType,
			Map<String, String> headers) throws SocketTimeoutException,
			NoConnectionException;

	String sendGet(String url) throws NoConnectionException;

	String sendGet(String url, Map<String, String> headers)
			throws NoConnectionException;
}
