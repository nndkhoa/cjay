package com.cloudjay.cjay.util;

public class ApiEndpoint {

	public static final String ROOT_API = "https://beta-dot-cloudjay-web.appspot.com";
	public static final String TOKEN_API = "/api/auth";
	public static final String CURRENT_USER_API = "/api/cjay/me";
	public static final String LIST_OPERATORS_API = "/api/cjay/operators";
	public static final String LIST_DAMAGE_CODES_API = "/api/cjay/damage-codes";
	public static final String LIST_REPAIR_CODES_API = "/api/cjay/repair-codes";
	public static final String LIST_COMPONENT_CODES_API = "/api/cjay/component-codes";
	public static final String CONTAINER_SESSIONS_API = "/api/cjay/full-containers";
	public static final String CONTAINER_SESSION_ITEM_API = "/api/cjay/full-containers/{id}";

//	public static final String CJAY_TMP_STORAGE = "https://www.googleapis.com/upload/storage/v1beta2/b/cjaytmp/o?uploadType=media&name=%s";
	public static final String CJAY_TMP_STORAGE = "https://www.googleapis.com/upload/storage/v1beta2/b/cjaytmp/o";
}
