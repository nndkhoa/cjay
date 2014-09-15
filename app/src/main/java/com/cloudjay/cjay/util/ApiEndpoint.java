package com.cloudjay.cjay.util;

public class ApiEndpoint {

	public static final String ROOT_API = "https://cloudjay-web.appspot.com";
	public static final String TOKEN_API = "/api-token-auth/";
	public static final String API_ADD_GCM_DEVICE_API = "/mobile/gcm-devices.json";
	public static final String CURRENT_USER_API = "/cjay/current-user.json";
	public static final String LIST_OPERATORS_API = "/cjay/container-operators.json";
	public static final String LIST_DAMAGE_CODES_API = "/cjay/damage-codes.json";
	public static final String LIST_REPAIR_CODES_API = "/cjay/repair-codes.json";
	public static final String LIST_COMPONENT_CODES_API = "/cjay/component-codes.json";
	public static final String CONTAINER_SESSIONS_API = "/cjay/container-sessions.json";
	public static final String CONTAINER_SESSION_ITEM_API = "/cjay/container-sessions/%s.json";

//	public static final String CJAY_TMP_STORAGE = "https://www.googleapis.com/upload/storage/v1beta2/b/cjaytmp/o?uploadType=media&name=%s";
	public static final String CJAY_TMP_STORAGE = "https://www.googleapis.com/upload/storage/v1beta2/b/cjaytmp/o";
}