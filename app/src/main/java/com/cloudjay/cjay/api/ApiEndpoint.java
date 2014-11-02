package com.cloudjay.cjay.api;


public class ApiEndpoint {

	public static final String ROOT_API = "https://beta-dot-cloudjay-web.appspot.com";
	public static final String TOKEN_API = "/api/auth";
	public static final String CURRENT_USER_API = "/api/cjay/me";
	public static final String LIST_OPERATORS_API = "/api/cjay/operators";
	public static final String LIST_DAMAGE_CODES_API = "/api/cjay/damage-codes";
	public static final String LIST_REPAIR_CODES_API = "/api/cjay/repair-codes";
	public static final String LIST_COMPONENT_CODES_API = "/api/cjay/component-codes";
	public static final String CONTAINER_SESSIONS_API = "/api/cjay/containers";
	public static final String CONTAINER_SESSION_ITEM_API = "/api/cjay/containers/{id}";

	public static final String CONTAINER_SESSION_CHECK_OUT_API = "/api/cjay/containers/{id}/check-out";
	public static final String CONTAINER_SESSION_COMPLETE_REPAIR_API = "/api/cjay/containers/{id}/complete-repair";
	public static final String CONTAINER_SESSION_POST_AUDIT_ITEM_API = "/api/cjay/containers/{id}/post-audit-item";
	public static final String CONTAINER_SESSION_COMPLETE_AUDIT_API = "/api/cjay/containers/{id}/complete-audit";
	public static final String CONTAINER_SESSION_ADD_AUDIT_IMAGES_API = "/api/cjay/containers/{id}/add-audit-images";

    public static final String CONTAINER_SESSION_HAND_CLEANING = "/api/cjay/containers/{id}/set-available";

	public static final String CJAY_TMP_STORAGE = "https://www.googleapis.com";
	public static final String CJAY_TMP_STORAGE_IMAGE = "/upload/storage/v1beta2/b/cjaytmp/o";

	public static final String PUBNUB_AUDITITEM = "/api/cjay/audit-items/{id}";
	public static final String PUBNUB_DAMAGECODE = "/api/cjay/damage-codes/{id}";
	public static final String PUBNUB_REPAIRCODE = "/api/cjay/repair-codes/{id}";
	public static final String PUBNUB_COMPONENTCODE = "/api/cjay/component-codes/{id}";
	public static final String PUBNUB_OPERATOR = "/api/cjay/repair-codes/{id}";
	public static final String PUBNUB_GOTMESSAGE = "/pubnub-retry/got-message";
}
