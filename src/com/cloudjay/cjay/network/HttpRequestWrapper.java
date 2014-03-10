package com.cloudjay.cjay.network;

import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.util.Log;

import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.NoConnectionException;

public class HttpRequestWrapper implements IHttpRequestWrapper {
	private DefaultHttpClient httpClient;
	private HttpContext localContext;
	private HttpResponse response = null;
	private HttpPost httpPost = null;
	private HttpGet httpGet = null;

	public static final String DEFAULT_ACCEPT_HEADER = "text/html,application/xml,application/xhtml+xml,text/html,application/json;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5";
	public static final String DEFAULT_CONTENT_TYPE = "application/x-www-form-urlencoded";
	public static final String JSON_CONTENT_TYPE = "application/json";

	public HttpRequestWrapper() {
		HttpParams myParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(myParams, 10000);

		// this will cause SocketTimeout
		// HttpConnectionParams.setSoTimeout(myParams, 10000);

		HttpProtocolParams.setVersion(myParams, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(myParams,
				HTTP.DEFAULT_CONTENT_CHARSET);
		HttpProtocolParams.setUseExpectContinue(myParams, true);

		SchemeRegistry schReg = new SchemeRegistry();
		schReg.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		schReg.register(new Scheme("https",
				SSLSocketFactory.getSocketFactory(), 443));
		ClientConnectionManager conMgr = new ThreadSafeClientConnManager(
				myParams, schReg);

		httpClient = new DefaultHttpClient(conMgr, myParams);
		localContext = new BasicHttpContext();
	}

	public String sendPost(String url, String data)
			throws SocketTimeoutException, NoConnectionException {
		return sendPost(url, data, null);
	}

	public String sendJSONPost(String url, JSONObject data)
			throws SocketTimeoutException, NoConnectionException {
		Map<String, String> headers = new HashMap<String, String>();
		return sendJSONPost(url, data, headers);
	}

	public String sendJSONPost(String url, JSONObject data,
			Map<String, String> headers) throws SocketTimeoutException,
			NoConnectionException {

		String ret = "";
		try {
			ret = sendPost(url, data.toString(), "application/json", headers);
		} catch (Exception e) {

		}

		return ret;
	}

	// final call
	public String sendPost(String url, String data, String contentType,
			Map<String, String> headers) throws SocketTimeoutException,
			NoConnectionException {

		Logger.Log("URL: " + url);
		Logger.Log("Data: " + data);
		Logger.Log("Content Type: " + contentType);
		Logger.Log("Header: " + headers.toString());

		httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY,
				CookiePolicy.RFC_2109);

		httpPost = new HttpPost(url);
		StringEntity postEntity = null;

		headers.put("Accept", DEFAULT_ACCEPT_HEADER);
		Iterator<Entry<String, String>> iterator = headers.entrySet()
				.iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, String> pairs = (Map.Entry<String, String>) iterator
					.next();
			String value = pairs.getValue();
			String Key = pairs.getKey();
			httpPost.setHeader(Key, value);
		}
		if (contentType != null) {
			httpPost.setHeader("Content-Type", contentType);
		} else {
			httpPost.setHeader("Content-Type",
					"application/x-www-form-urlencoded");
		}

		try {
			postEntity = new StringEntity(data, "UTF-8");
		} catch (UnsupportedEncodingException e) {

		}
		httpPost.setEntity(postEntity);
		String ret = null;

		try {
			response = httpClient.execute(httpPost, localContext);

			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK
					|| response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED
					|| response.getStatusLine().getStatusCode() == HttpStatus.SC_ACCEPTED) {

				ret = EntityUtils.toString(response.getEntity());
				Logger.Log("Return from server: " + ret);

			} else {
				Log.i("FOO", "Screw up with http - "
						+ response.getStatusLine().getStatusCode());
			}

		} catch (SocketTimeoutException se) {
			throw new SocketTimeoutException();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	public String sendPost(String url, String data, String contentType)
			throws SocketTimeoutException, NoConnectionException {
		Map<String, String> headers = new HashMap<String, String>();
		return sendPost(url, data, contentType, headers);
	}

	public String sendGet(String url) throws NoConnectionException {
		Map<String, String> headers = new HashMap<String, String>();
		return sendGet(url, headers);
	}

	public String sendGet(String url, Map<String, String> headers)
			throws NoConnectionException {

		Logger.Log("Url: " + url);
		Logger.Log("Header: " + headers.toString());

		httpGet = new HttpGet(url);

		headers.put("Accept", DEFAULT_ACCEPT_HEADER);
		headers.put("Content-Type", "text/plain; charset=utf-8");
		Iterator<Entry<String, String>> iterator = headers.entrySet()
				.iterator();

		while (iterator.hasNext()) {
			Map.Entry<String, String> pairs = (Map.Entry<String, String>) iterator
					.next();
			String value = pairs.getValue();
			String Key = pairs.getKey();
			httpGet.setHeader(Key, value);
		}

		String ret = null;
		try {
			response = httpClient.execute(httpGet);
			int responseCode = response.getStatusLine().getStatusCode();

			if (responseCode == HttpStatus.SC_FORBIDDEN) {
				// Log user out
				Logger.e("Token was expired.");
			}

			ret = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ret;
	}
}
