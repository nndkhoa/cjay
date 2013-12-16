package com.cloudjay.cjay.network;

import java.util.Map;

import org.json.JSONObject;

public interface IHttpRequestWrapper {
	String sendPost(String url, String data);
	String sendJSONPost(String url, JSONObject data);
	String sendJSONPost(String url, JSONObject data, Map<String, String> headers);
	String sendPost(String url, String data, String contentType);
	String sendPost(String url,String data,String contentType,Map<String,String> headers);
	
	String sendGet(String url);
	String sendGet(String url,Map<String,String> headers);
}
