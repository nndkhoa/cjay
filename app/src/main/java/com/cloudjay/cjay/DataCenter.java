package com.cloudjay.cjay;

import android.content.Context;

import com.cloudjay.cjay.api.NetworkClient;
import com.cloudjay.cjay.event.AuditImagesGotEvent;
import com.cloudjay.cjay.event.GateImagesGotEvent;
import com.cloudjay.cjay.event.IssueUpdatedEvent;
import com.cloudjay.cjay.event.issue.IssueDeletedEvent;
import com.cloudjay.cjay.event.issue.IssueMergedEvent;
import com.cloudjay.cjay.event.operator.OperatorsGotEvent;
import com.cloudjay.cjay.event.session.ContainerSearchedEvent;
import com.cloudjay.cjay.event.session.SearchAsyncStartedEvent;
import com.cloudjay.cjay.event.session.WorkingSessionCreatedEvent;
import com.cloudjay.cjay.event.upload.UploadedEvent;
import com.cloudjay.cjay.model.AuditImage;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.model.GateImage;
import com.cloudjay.cjay.model.IsoCode;
import com.cloudjay.cjay.model.LogItem;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.PreferencesUtil;
import com.cloudjay.cjay.util.StringUtils;
import com.cloudjay.cjay.util.Utils;
import com.cloudjay.cjay.util.enums.ImageType;
import com.cloudjay.cjay.util.enums.Step;
import com.cloudjay.cjay.util.enums.UploadStatus;
import com.cloudjay.cjay.util.exception.NullCredentialException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.snappydb.DB;
import com.snappydb.SnappydbException;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.Trace;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;

@EBean(scope = EBean.Scope.Singleton)
public class DataCenter {

	// region DECLARE
	// Inject the rest client
	@Bean
	NetworkClient networkClient;

	Context context;

	public static final String NETWORK = "NETWORK";
	public static final String CACHE = "CACHE";

	public DataCenter(Context context) {
		this.context = context;
	}
	//endregion

	//region USER
	public String getToken(String email, String password) {
		return networkClient.getToken(email, password);
	}

	public User getUser(Context context) throws SnappydbException, NullCredentialException {

		DB db = App.getDB(context);
		User user = db.getObject(CJayConstant.PREFIX_USER, User.class);


		if (null == user) {
			return getCurrentUserAsync(context);
		} else {
			return user;
		}
	}

	public User getCurrentUserAsync(Context context) throws SnappydbException, NullCredentialException {

		User user = networkClient.getCurrentUser(context);

		if (null == user) {
			throw new NullCredentialException();
		}

		// User is not null, then we need to store them to database add shared preference
		PreferencesUtil.storePrefsValue(context, PreferencesUtil.PREF_USER_NAME, user.getFullName());
		PreferencesUtil.storePrefsValue(context, PreferencesUtil.PREF_USER_ROLE_NAME, user.getRoleName());
		PreferencesUtil.storePrefsValue(context, PreferencesUtil.PREF_USER_ROLE, user.getRole() + "");
		PreferencesUtil.storePrefsValue(context, PreferencesUtil.PREF_USER_DEPOT, user.getDepotCode());

		DB db = App.getDB(context);
		db.put(CJayConstant.PREFIX_USER, user);


		return user;
	}
	//endregion

	//region OPERATOR

	/**
	 * Fetch and save all operators to database.
	 * Call it in Background.
	 *
	 * @param context
	 * @throws SnappydbException
	 */
	public void fetchOperators(Context context) throws SnappydbException {

		DB db = App.getDB(context);
		List<Operator> operators = networkClient.getOperators(context, null);
		for (Operator operator : operators) {
			db.put(CJayConstant.PREFIX_OPERATOR + operator.getOperatorCode(), operator);
		}

	}

	/**
	 * Search for operator
	 *
	 * @param keyword
	 */
	public void searchOperator(String keyword) {
		try {
			List<Operator> operators = new ArrayList<Operator>();
			DB db = App.getDB(context);
			String[] keysResult = db.findKeys(CJayConstant.PREFIX_OPERATOR + keyword);
			for (String result : keysResult) {
				Operator operator = db.getObject(result, Operator.class);
				operators.add(operator);
			}

			EventBus.getDefault().post(new OperatorsGotEvent(operators));
		} catch (SnappydbException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get operator from database.
	 * Chỉ sử dụng khi biết chắc có operator ở trong database.
	 *
	 * @param context
	 * @param operatorCode
	 * @return
	 */
	public Operator getOperator(Context context, String operatorCode) {

		try {
			DB db = App.getDB(context);
			String key = CJayConstant.PREFIX_OPERATOR + operatorCode;
			Operator operator = db.getObject(key, Operator.class);
			return operator;

		} catch (SnappydbException e) {
			Logger.w(e.getMessage());
			return null;
		}
	}

	/**
	 * Get operator from server by id
	 *
	 * @param context
	 * @param id
	 */
	public void getOperatorById(Context context, long id) {

		Operator operator = networkClient.getOperatorById(id);
		try {
			DB db = App.getDB(context);
			String key = CJayConstant.PREFIX_OPERATOR + operator.getOperatorCode();
			db.put(key, operator);

		} catch (SnappydbException e) {
			e.printStackTrace();
		}
	}
	//endregion

	//region ISO CODE

	/**
	 * Fetch and save all iso codes to database.
	 * Call it in Background
	 *
	 * @param context
	 * @throws SnappydbException
	 */
	public void fetchIsoCodes(Context context) throws SnappydbException {
		fetchDamageCodes(context);
		fetchRepairCodes(context);
		fetchComponentCodes(context);
	}

	/**
	 * fetch damage codes from server
	 *
	 * @param context
	 * @throws SnappydbException
	 */
	public void fetchDamageCodes(Context context) throws SnappydbException {

		DB db = App.getDB(context);
		List<IsoCode> damageCodes = networkClient.getDamageCodes(context, null);
		for (IsoCode code : damageCodes) {
			String key = CJayConstant.PREFIX_DAMAGE_CODE + code.getCode();
			db.put(key, code);
		}

	}

	/**
	 * fetch repair codes from server
	 *
	 * @param context
	 * @throws SnappydbException
	 */
	public void fetchRepairCodes(Context context) throws SnappydbException {
		DB db = App.getDB(context);
		List<IsoCode> repairCodes = networkClient.getRepairCodes(context, null);
		for (IsoCode code : repairCodes) {
			String key = CJayConstant.PREFIX_REPAIR_CODE + code.getCode();
			db.put(key, code);
		}

	}

	/**
	 * fetch component code from server
	 *
	 * @param context
	 * @throws SnappydbException
	 */
	public void fetchComponentCodes(Context context) throws SnappydbException {
		DB db = App.getDB(context);
		List<IsoCode> componentCodes = networkClient.getComponentCodes(context, null);
		for (IsoCode code : componentCodes) {
			String key = CJayConstant.PREFIX_COMPONENT_CODE + code.getCode();
			db.put(key, code);
		}

	}

	/**
	 * Use it to add single iso code to DB
	 *
	 * @param context
	 * @param isoCode
	 * @param type
	 */
	public void addIsoCode(Context context, IsoCode isoCode, IsoCode.Type type) {
		try {
			DB db = App.getDB(context);
			String key;
			switch (type) {
				case DAMAGE:
					key = CJayConstant.PREFIX_DAMAGE_CODE + isoCode.getCode();
					break;
				case REPAIR:
					key = CJayConstant.PREFIX_REPAIR_CODE + isoCode.getCode();
					break;
				case COMPONENT:
				default:
					key = CJayConstant.PREFIX_COMPONENT_CODE + isoCode.getCode();
					break;
			}
			db.put(key, isoCode);

		} catch (SnappydbException e) {
			Logger.e(e.getMessage());
		}
	}

	/**
	 * get single damage code by Id
	 *
	 * @param context
	 * @param id
	 */
	public void getDamageCodeById(Context context, long id) {
		IsoCode isoCode = networkClient.getDamageCodeById(id);
		addIsoCode(context, isoCode, IsoCode.Type.DAMAGE);
	}

	/**
	 * get single repair code by Id
	 *
	 * @param context
	 * @param id
	 */
	public void getRepairCodeById(Context context, long id) {
		IsoCode isoCode = networkClient.getRepairCodeById(id);
		addIsoCode(context, isoCode, IsoCode.Type.REPAIR);
	}

	/**
	 * get single component code by Id
	 *
	 * @param context
	 * @param id
	 */
	public void getComponentCodeById(Context context, long id) {
		IsoCode isoCode = networkClient.getComponentCodeById(id);
		addIsoCode(context, isoCode, IsoCode.Type.COMPONENT);
	}
	//endregion

	//region SESSION

	//
	public Session getSessionById(Context context, long id) {
		Session session = networkClient.getSessionById(id);
		Session localSession = getSession(context, session.getContainerId());

		if (localSession == null) {
			addSession(session);
		} else {
			// Container session is already existed
			// TODO: merge session with the existed one
		}

		return session;
	}

	/**
	 * Only use when search container session from db.
	 * Chỉ sử dụng khi biết chắc session đã ở trong db.
	 *
	 * @param context
	 * @param containerId
	 * @return
	 */
	@Trace
	public Session getSession(Context context, String containerId) {

		try {

			DB db = App.getDB(context);
			String key = containerId;
			Session session = db.getObject(key, Session.class);


			return session;
		} catch (SnappydbException e) {
			Logger.w(e.getMessage());
			return null;
		}
	}

	/**
	 * Get list container sessions based on param `prefix`
	 *
	 * @param context
	 * @param prefix
	 * @return
	 */
	public List<Session> getListSessions(Context context, String prefix) {

		int len = prefix.length();

		DB db;
		String[] keysResult;
		List<Session> sessions = new ArrayList<>();

		try {
			db = App.getDB(context);
			keysResult = db.findKeys(prefix);
		} catch (SnappydbException e) {
			Logger.e(e.getMessage());
			return null;
		}

		for (String result : keysResult) {

			String newKey = result.substring(len);
			addLog(context, newKey, prefix + " | Cannot retrieve this container");
			Session session;
			try {
				session = db.getObject(newKey, Session.class);
				sessions.add(session);
			} catch (SnappydbException e) {
				e.printStackTrace();

			}
		}

		return sessions;
	}

	//endregion

	/**
	 * Fetch all container session with last modified datetime
	 *
	 * @param context
	 * @param lastModifiedDate
	 * @throws SnappydbException
	 */
	@Trace
	public void fetchSession(Context context, String lastModifiedDate) throws SnappydbException {
		List<Session> sessions = networkClient.getAllSessions(context, lastModifiedDate);
		DB db = App.getDB(context);
		for (Session session : sessions) {

			String key = session.getContainerId();
			session.setLocalStep(session.getStep());
			
			db.put(key, session);
		}

	}

	/**
	 * Search container session from device database.
	 * <p/>
	 * > FLOW
	 * <p/>
	 * 1. Tìm kiếm từ database với keyword được cung cấp(không hỗ trợ full text search)
	 * 2. Post kết quả tìm được (nếu có) thông qua EventBus
	 * 3. Nếu không tìm thấy ở trên client thì tiến hành search ở server.
	 *
	 * @param context
	 * @param keyword
	 */
	@Background
	public void search(Context context, String keyword) {

		String[] keysResult;
		try {
			DB db = App.getDB(context);

			// try to search from client database
			keysResult = db.findKeys(keyword);
			List<Session> sessions = new ArrayList<>();

			for (String result : keysResult) {
				Session session = db.getObject(result, Session.class);
				sessions.add(session);
			}


			// Check if local search has results
			if (sessions.size() != 0) {

				EventBus.getDefault().post(new ContainerSearchedEvent(sessions));
			} else {

				// If there was not result in local, send search request to server
				//  --> alert to user about that no results was found in local
				EventBus.getDefault().post(new SearchAsyncStartedEvent(context.getResources().getString(R.string.search_on_server)));
				searchAsync(context, keyword);
			}
		} catch (SnappydbException e) {
			Logger.e(e.getMessage());
		}
	}

	/**
	 * Search container session từ server
	 * <p/>
	 * > FLOW
	 * <p/>
	 * 1. Call NetworkClient#search để lấy list container sessions từ server
	 * 2. Post kết quả trả về thông qua EventBus
	 *
	 * @param context
	 * @param keyword
	 */
	@Background
	@Trace
	public void searchAsync(Context context, String keyword) {

		try {
			Logger.Log("Begin to search container from server");
			List<Session> sessions = networkClient.searchSessions(context, keyword);

			if (sessions.size() != 0) {
				DB db = App.getDB(context);

				for (Session session : sessions) {
					String key = session.getContainerId();
					db.put(key, session);
				}
			}

			EventBus.getDefault().post(new ContainerSearchedEvent(sessions));
		} catch (SnappydbException e) {
			Logger.w(e.getMessage());
		} catch (RetrofitError error) {
			Logger.w(error.getMessage());
			EventBus.getDefault().post(new ContainerSearchedEvent(true));
		}
	}

	/**
	 * Thêm container session mới vào database
	 *
	 * @param session
	 */
	public void addSession(Session session) {
		try {
			DB db = App.getDB(context);

			// Add normal session
			String key = session.getContainerId();
			db.put(key, session);

			// Close db

		} catch (SnappydbException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Add container session vào list working session in database
	 *
	 * @param session
	 */
	public void addWorkingSession(Session session) {

		try {
			DB db = App.getDB(context);

			String key = CJayConstant.PREFIX_WORKING + session.getContainerId();
			db.put(key, session);


			// Notify to Working Fragment
			EventBus.getDefault().post(new WorkingSessionCreatedEvent(session));

		} catch (SnappydbException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Add container session vào list uploading session in database
	 *
	 * @param containerId
	 * @throws SnappydbException
	 */
	public void addUploadSession(String containerId) {

		try {
			DB db = App.getDB(context);
			Session session = db.getObject(containerId, Session.class);
			String key = CJayConstant.PREFIX_UPLOADING + containerId;
			db.put(key, session);


		} catch (SnappydbException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Add image to container Session
	 *
	 * @param image
	 * @param containerId
	 * @throws SnappydbException
	 */
	public void addGateImage(Context context, GateImage image, String containerId) throws SnappydbException {

		DB db = App.getDB(context);

		// Add gate image to normal container session
		Session session = db.getObject(containerId, Session.class);
		session.getGateImages().add(image);

		Logger.Log("Size: " + session.getGateImages().size());

		String key = containerId;
		db.put(key, session);

//		// Add gate image to on working container session
//		key = CJayConstant.PREFIX_WORKING + containerId;
//		db.put(key, session);


	}

	public void addAuditImage(Context context, AuditImage auditImage, String containerId) throws SnappydbException {
		DB db = App.getDB(context);

		Session session = db.getObject(containerId, Session.class);

		// Generate random one UUID to save auditItem
		String uuid = UUID.randomUUID().toString();

		// Create new audit item to save
		AuditItem auditItem = new AuditItem();
		auditItem.setId(0);
		auditItem.setAuditItemUUID(uuid);

		// this audit item has not been audited yet
		auditItem.setAudited(false);

		// this audit item has not been approved yet
		auditItem.setApproved(false);

		// this audit item has not been allowed to repair yet
		auditItem.setIsAllowed(false);

		// this audit item has not been repaired  yet
		auditItem.setRepaired(false);

		// this audit item has not been uploaded yet
		auditItem.setUploadStatus(UploadStatus.NONE);

		// Get list session's audit items
		List<AuditItem> auditItems = session.getAuditItems();
		if (auditItems == null) {
			auditItems = new ArrayList<AuditItem>();
		}

		List<AuditImage> auditImages = auditItem.getAuditImages();
		if (auditImages == null) {
			auditImages = new ArrayList<AuditImage>();
		}
		// Add audit image to list audit images
		auditImages.add(auditImage);

		// Set list audit images to audit item
		auditItem.setAuditImages(auditImages);

		// Add audit item to List session's audit items
		auditItems.add(auditItem);

		// Add audit item to Session
		session.setAuditItems(auditItems);

		db.put(containerId, session);

		Logger.Log("insert audit image successfully");

	}

	public void getGateImages(Context context, String containerId) throws SnappydbException {
		DB db = App.getDB(context);
		Session session = db.getObject(containerId, Session.class);


		List<GateImage> gateImages = session.getGateImages();
		EventBus.getDefault().post(new GateImagesGotEvent(gateImages));
	}

	public void getSessionByContainerId(Context context, String containerId) {
		String[] keysResult;
		try {
			DB db = App.getDB(context);
			keysResult = db.findKeys(containerId);
			List<Session> sessions = new ArrayList<Session>();
			for (String result : keysResult) {
				Session tmp = db.getObject(result, Session.class);
				sessions.add(tmp);
			}


			EventBus.getDefault().post(new ContainerSearchedEvent(sessions));
		} catch (SnappydbException e) {
			e.printStackTrace();
		}

	}

	public void getAllGateImagesByContainerId(Context context, String containerId) {
		try {
			DB db = App.getDB(context);
			Session session = db.getObject(containerId, Session.class);
			List<GateImage> gateImages = session.getGateImages();

			Logger.Log("gate images count in dataCenter: " + gateImages.size());


			EventBus.getDefault().post(new GateImagesGotEvent(gateImages));
		} catch (SnappydbException e) {
			e.printStackTrace();
		}
	}

	//region UPLOAD

	/**
	 * 1. Tìm session với containerId trong list uploading.
	 * 2. Upload hình và gán field uploaded ngược vào list uploading
	 *
	 * @param context
	 * @param uri
	 * @param imageName
	 * @param containerId
	 * @throws SnappydbException
	 */

	public boolean uploadImage(Context context, String uri, String imageName, String containerId, ImageType imageType) throws SnappydbException {

		try {
			//Call network client to upload image
			networkClient.uploadImage(uri, imageName);

			// Change image status to COMPLETE
			setImageUploadStatus(context, containerId, imageName, imageType, UploadStatus.COMPLETE);

			return true;
		} catch (RetrofitError e) {
			e.printStackTrace();
			return false;
		}
	}

	private void setImageUploadStatus(Context context, String containerId, String imageName, ImageType imageType, UploadStatus status) throws SnappydbException {

		Logger.Log("imageName: " + imageName);

		DB db = App.getDB(context);

		// Change status image in db
		String key = containerId;
		Session session = db.getObject(key, Session.class);

		if (session != null) {
			switch (imageType) {

				case AUDIT:
					for (AuditItem auditItem : session.getAuditItems()) {
						for (AuditImage auditImage : auditItem.getListIssueImages()) {

							Logger.Log("auditImage: " + auditImage.getName());

							if (auditImage.getName().equals(imageName) && auditImage.getType() == imageType.value) {
								auditImage.setUploadStatus(status);
							}
						}
					}
				case REPAIRED:
					for (AuditItem auditItem : session.getAuditItems()) {
						for (AuditImage auditImage : auditItem.getListRepairedImages()) {

							Logger.Log("auditImage: " + auditImage.getName());

							if (auditImage.getName().equals(imageName) && auditImage.getType() == imageType.value) {
								auditImage.setUploadStatus(status);
							}
						}
					}
					break;

				case IMPORT:
				case EXPORT:
				default:
					for (GateImage gateImage : session.getGateImages()) {
						if (gateImage.getName().equals(imageName) && gateImage.getType() == imageType.value) {
							Logger.Log(imageName + " " + gateImage.getType());
							gateImage.setUploadStatus(status);
							break;
						}
					}
					break;
			}

			db.put(key, session);
		}

	}

	/**
	 * -1. Change local step to import
	 * 0. Check for make sure all gate image have uploaded
	 * 1. Upload container session
	 * 2. Change status uploaded of session to COMPLETE
	 * 3. Remove this container from TAB WORKING
	 *
	 * @param context
	 * @param containerId
	 * @throws SnappydbException
	 */
	public void uploadImportSession(Context context, String containerId) throws SnappydbException {
		DB db = App.getDB(context);
		Session oldSession = db.getObject(containerId, Session.class);

		// Check for make sure all gate image have uploaded
		for (GateImage gateImage : oldSession.getGateImages()) {
			if (gateImage.getUploadStatus() != UploadStatus.COMPLETE.value && gateImage.getType() == ImageType.IMPORT.value) {
				//TODO Note to Khoa this upload import session have to retry upload Image @Han
				uploadImage(context, Utils.parseUrltoUri(gateImage.getUrl()), gateImage.getName(), oldSession.getContainerId(), ImageType.IMPORT);
			}
		}


		// Upload container session to server
		Session result = networkClient.uploadSession(context, oldSession);

		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();

		Logger.Log("result: " + gson.toJson(result));

		if (result != null) {
			//merge session
			oldSession.mergeSession(result);

			// Update container back to database
			String key = result.getContainerId();
			db.put(key, oldSession);

		}

	}
	//endregion

	public void getAuditImages(Context context, String containerId) {
		Session session = null;
		try {
			DB db = App.getDB(context);
			session = db.getObject(containerId, Session.class);

		} catch (SnappydbException e) {
			Logger.w(e.getMessage());
		}

		// Audit and Repaired Images
		List<AuditImage> auditImages = new ArrayList<>();

		// Get list audit images of each audit item and add to audit images list
		for (AuditItem auditItem : session.getAuditItems()) {
			List<AuditImage> childAuditImageList = auditItem.getAuditImages();
			if (childAuditImageList != null) {
				auditImages.addAll(childAuditImageList);
			}
		}
		EventBus.getDefault().post(new AuditImagesGotEvent(auditImages));
	}

	/**
	 * Get List audit item for normal session
	 *
	 * @param context
	 * @param containerId
	 * @return List Audit Item
	 */
	public List<AuditItem> getListAuditItems(Context context, String containerId) {
		try {
			DB db = App.getDB(context);

			Session session = db.getObject(containerId, Session.class);
			List<AuditItem> auditItems = session.getAuditItems();


			return auditItems;
		} catch (SnappydbException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Set session have containerId is hand cleaning session, upload this to server then add new session return from server to database
	 *
	 * @param context
	 * @param containerId
	 * @return
	 */
	public Session setHandCleaningSession(Context context, String containerId) {
		try {
			DB db = App.getDB(context);
			Session oldSession = db.getObject(containerId, Session.class);
			Session newSession = networkClient.setHandCleaningSession(context, oldSession);
			db.put(newSession.getContainerId(), newSession);

			return newSession;
		} catch (SnappydbException e) {
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * Get list log upload for view in Log activity by search key "LOG"
	 *
	 * @param context
	 * @param logUploadKey
	 * @return
	 */
	public List<LogItem> getListLogItems(Context context, String logUploadKey) {
		try {
			DB db = App.getDB(context);
			String[] keysResult = db.findKeys(logUploadKey);
			List<LogItem> logUploads = new ArrayList<>();

			for (String result : keysResult) {
				LogItem logUpload = db.getObject(result, LogItem.class);
				logUploads.add(logUpload);
			}

			return logUploads;
		} catch (SnappydbException e) {
			Logger.w(e.getMessage());
			return null;
		}
	}

	/**
	 * Search list log upload for view in Log activity by search key "LOG" + containerId
	 *
	 * @param context
	 * @param searchKey
	 * @return
	 */
	public List<LogItem> searchLog(Context context, String searchKey) {
		try {
			DB db = App.getDB(context);
			String[] keysResult = db.findKeys(CJayConstant.PREFIX_LOG + searchKey);
			List<LogItem> logUploads = new ArrayList<>();

			for (String result : keysResult) {
				LogItem logUpload = db.getObject(result, LogItem.class);
				logUploads.add(logUpload);
			}


			return logUploads;
		} catch (SnappydbException e) {
			Logger.w(e.getMessage());
			return null;
		}
	}

	// Thệm lỗi đã giám định
	public void addIssue(Context context, AuditItem auditItem, String containerId) {
		try {

			Logger.Log("auditItem.getAudited() = " + auditItem.getAudited());

			DB db = App.getDB(context);
			Session session = db.getObject(containerId, Session.class);

			// Get list session's audit items
			List<AuditItem> auditItems = session.getAuditItems();
			if (auditItems == null) {
				auditItems = new ArrayList<>();
			}

			// Add audit item to List session's audit items
			auditItems.add(auditItem);

			// Add audit item to Session
			session.setAuditItems(auditItems);

			db.put(containerId, session);

			Logger.Log("insert issue audited successfully");


		} catch (SnappydbException e) {
			Logger.w(e.getMessage());
		}

	}

	/**
	 * Merge hình vào lỗi đã giám định.
	 *
	 * @param context
	 * @param containerId
	 * @param auditItemUUID
	 * @param auditItemRemove
	 * @param auditImageUUID
	 */
	public void addAuditImageToAuditedIssue(Context context,
	                                        String containerId,
	                                        String auditItemUUID,
	                                        String auditItemRemove,
	                                        String auditImageUUID) {

		try {
			DB db = App.getDB(context);
			Session session = db.getObject(containerId, Session.class);

			// Get audit image of audit item will be merged
			AuditImage auditImage = getAuditImageByUUId(context, containerId,
					auditItemRemove, auditImageUUID);

			// Remove audit item will be merged
			AuditItem removeItem = getAuditItemByUUID(context, containerId, auditItemRemove);
			if (removeItem != null) {
				Logger.Log("Begin to remove");
				session.getAuditItems().remove(removeItem);
			}

			AuditItem itemMerged = getAuditItemByUUID(context, containerId, auditItemUUID);
			if (itemMerged != null) {
				AuditItem tmp = itemMerged;
				tmp.getAuditImages().add(auditImage);

				session.getAuditItems().add(tmp);
				session.getAuditItems().remove(itemMerged);
			}

			db.put(containerId, session);
			Logger.Log("add AuditImage To AuditedIssue successfully");


		} catch (SnappydbException e) {
			Logger.w(e.getMessage());
		}

		EventBus.getDefault().post(new IssueMergedEvent(containerId, auditItemRemove));
	}

	/**
	 * Get audit Image by UUID
	 *
	 * @param context
	 * @param containerId
	 * @param auditItemUUID
	 * @param auditImageUUID
	 * @return
	 */
	public AuditImage getAuditImageByUUId(Context context, String containerId, String auditItemUUID, String auditImageUUID) {

		try {
			DB db = App.getDB(context);
			Session localSession = db.getObject(containerId, Session.class);
			if (localSession != null) {
				for (AuditItem auditItem : localSession.getAuditItems()) {
					if (auditItem.getAuditItemUUID().equals(auditItemUUID)) {
						for (AuditImage auditImage : auditItem.getAuditImages()) {
							if (auditImage.getAuditImageUUID().equals(auditImageUUID))
								return auditImage;
						}
					}
				}
			}
		} catch (SnappydbException e) {
			e.printStackTrace();
		}
		return null;

	}

    public AuditImage getAuditImageByUUId(Context context, String auditImageUUID) {
        try {
            DB db = App.getDB(context);
            return db.getObject(auditImageUUID, AuditImage.class);
        } catch (SnappydbException e) {
            Logger.e(e.getMessage());
        }
        return null;
    }

	/**
	 * Add log message to database. This method will be called from:
	 * - Add container to queue
	 * - Add image to queue
	 * - Add issue/audit item to queue
	 * - Begin to upload container
	 * - Upload container successfully
	 * - Upload container failed
	 * - Start QueueService Task
	 *
	 * @param context
	 * @param containerId
	 * @param message
	 */
	public void addLog(Context context, String containerId, String message) {
		String currentTime = StringUtils.getCurrentTimestamp(CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE);

		LogItem log = new LogItem();
		log.setContainerId(containerId);
		log.setMessage(message);
		log.setTime(currentTime);

		addLog(context, log);
	}

	public void addLog(Context context, LogItem log) {
		try {
			DB db = App.getDB(context);
			db.put(CJayConstant.PREFIX_LOG + log.getContainerId() + log.getTime(), log);


		} catch (SnappydbException e) {
			Logger.w(e.getMessage());
		}
	}

	public void upLoadAuditItem(Context context, String containerId, String oldAuditItemUUID) throws SnappydbException {
		// Upload audit item session to server
		DB db = App.getDB(context);

		Logger.Log("old auditem item uuid: " + oldAuditItemUUID);

		Session oldSession = db.getObject(containerId, Session.class);
		AuditItem oldAuditItem = getAuditItemByUUID(context, containerId, oldAuditItemUUID);
		Session result = networkClient.postAuditItem(context, oldSession, oldAuditItem);
		String key = result.getContainerId();

		List<AuditItem> listLocal = oldSession.getAuditItems();

		for (int i = 0; i < listLocal.size(); i++) {
			for (int j = 0; j < result.getAuditItems().size(); j++) {

				if (listLocal.get(i).getAuditItemUUID().equals(
						result.getAuditItems().get(j).getAuditItemUUID())) {
					AuditItem itemLocal = listLocal.get(i);

					itemLocal.setId(result.getAuditItems().get(j).getId());
					itemLocal.setUploadStatus(UploadStatus.COMPLETE.value);
					itemLocal.setAuditImages(result.getAuditItems().get(j).getAuditImages());
				}

			}
		}

		oldSession.setAuditItems(listLocal);
		db.put(key, oldSession);

		EventBus.getDefault().post(new UploadedEvent(result.getContainerId()));
	}

	public void uploadExportSession(Context context, String containerId) throws SnappydbException {

		// Upload audit item session to server
		DB db = App.getDB(context);
		Session oldSession = db.getObject(containerId, Session.class);
		Session result = networkClient.checkOutContainerSession(context, oldSession);
		Logger.Log("Add AuditItem to Session Id: " + result.getId());

		if (result != null) {

			// Update container back to database
			String key = result.getContainerId();
			result.setUploadStatus(UploadStatus.COMPLETE);
			db.put(key, result);

			// Then remove them from WORKING
			String workingKey = CJayConstant.PREFIX_WORKING + key;
			db.del(workingKey);
		}

		EventBus.getDefault().post(new UploadedEvent(result.getContainerId()));
	}

	/**
	 * Upload audited container session
	 *
	 * @param context
	 * @param containerId
	 * @throws SnappydbException
	 */
	public void uploadAuditedSession(Context context, String containerId) throws SnappydbException {
		// Upload complete audit session to server
		DB db = App.getDB(context);
		Session oldSession = db.getObject(containerId, Session.class);
		Session result = networkClient.completeAudit(context, oldSession);

		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();

		Logger.Log("result: " + gson.toJson(result));

		if (result != null) {

			// Update container back to database
			String key = result.getContainerId();
			result.setUploadStatus(UploadStatus.COMPLETE);
			db.put(key, result);

			// Then remove them from WORKING
			String workingKey = CJayConstant.PREFIX_WORKING + key;
			db.del(workingKey);
		}

		EventBus.getDefault().post(new UploadedEvent(result.getContainerId()));

	}

	/**
	 * Upload repaired container session
	 *
	 * @param context
	 * @param containerId
	 * @throws SnappydbException
	 */
	public void uploadRepairedSession(Context context, String containerId) throws SnappydbException {

		// Upload complete repair session to server
		DB db = App.getDB(context);
		Session oldSession = db.getObject(containerId, Session.class);
		Session result = networkClient.completeRepairSession(context, oldSession);
		Logger.Log("Add AuditItem to Session Id: " + result.getId());

		if (result != null) {

			// Update container back to database
			String key = result.getContainerId();
			result.setUploadStatus(UploadStatus.COMPLETE);
			db.put(key, result);

			// Then remove them from WORKING
			String workingKey = CJayConstant.PREFIX_WORKING + key;
			db.del(workingKey);
		}

		EventBus.getDefault().post(new UploadedEvent(result.getContainerId()));
	}

	/**
	 * Xóa lỗi sau khi merge
	 *
	 * @param context
	 * @param containerId
	 * @param auditItemUUID
	 */
	public void deleteAuditItemAfterMerge(Context context, String containerId, String auditItemUUID) {
		try {
			DB db = App.getDB(context);
			Session session = db.getObject(containerId, Session.class);

			for (AuditItem auditItem : session.getAuditItems()) {
				if (auditItem.getAuditItemUUID().equals(auditItemUUID)) {
					session.getAuditItems().remove(auditItem);
					break;
				}
			}

			db.put(containerId, session);
			Logger.Log("delete audit item successfully");

		} catch (SnappydbException e) {
			e.printStackTrace();
		}

		EventBus.getDefault().post(new IssueDeletedEvent(containerId));
	}

	/**
	 * Set lỗi thuộc loại vệ sinh.
	 * 1. Get Water Wash Damage Code
	 * 2. Get Water Wash Repair Code
	 * 3. Get Water Wash Component Code
	 * 4. Tạo danh sách các Audit Item cần xóa, bao gồm:
	 * - Các audit item là lỗi vệ sinh
	 * - Audit Item hiện tại chưa là lỗi vệ sinh
	 * 5. Xóa list này
	 * 6. Thêm lỗi đã cập nhật thành lỗi vệ sinh vào database
	 *
	 * @param context
	 * @param item
	 * @throws SnappydbException
	 */
	public void setWaterWashType(Context context, final AuditItem item, String containerId) throws SnappydbException {

		Logger.Log("setWaterWashType");

		DB db = App.getDB(context);

		// Add Iso Code
		String damageKey = CJayConstant.PREFIX_DAMAGE_CODE + "DB";
		String repairKey = CJayConstant.PREFIX_REPAIR_CODE + "WW";
		String componentKey = CJayConstant.PREFIX_COMPONENT_CODE + "FWA";

		IsoCode damageCode = db.getObject(damageKey, IsoCode.class);
		IsoCode repairCode = db.getObject(repairKey, IsoCode.class);
		IsoCode componentCode = db.getObject(componentKey, IsoCode.class);

		item.setDamageCodeId(damageCode.getId());
		item.setDamageCode(damageCode.getCode());

		item.setRepairCodeId(repairCode.getId());
		item.setRepairCode(repairCode.getCode());

		item.setComponentCodeId(componentCode.getId());
		item.setComponentCode(componentCode.getCode());

		item.setLocationCode("BXXX");
		item.setAudited(true);

		Session session = db.getObject(containerId, Session.class);

		List<AuditItem> removeList = new ArrayList<AuditItem>();

		// --> Nếu nhiều image cùng thuộc một lỗi vệ sinh, thì tính là một
		for (AuditItem auditItem : session.getAuditItems()) {

			if (auditItem != null) {
				if (auditItem.getAuditItemUUID() != null &&
						auditItem.getAuditItemUUID().equals(item.getAuditItemUUID())) {
					removeList.add(auditItem);
				}

				if (auditItem.getComponentCode() != null
						&& auditItem.getComponentCode().equals(componentCode.getCode())
						&& auditItem.getDamageCode() != null
						&& auditItem.getDamageCode().equals(damageCode.getCode())
						&& auditItem.getRepairCode() != null
						&& auditItem.getRepairCode().equals(repairCode.getCode())
						&& auditItem.getLocationCode() != null
						&& auditItem.getLocationCode().equals("BXXX")) {
					removeList.add(auditItem);
				}
			}
		}

		session.getAuditItems().removeAll(removeList);
		if (session.getAuditItems() == null) {
			List<AuditItem> newList = new ArrayList<AuditItem>();
			session.setAuditItems(newList);
		}
		session.getAuditItems().add(item);
		db.put(containerId, session);

		EventBus.getDefault().post(new IssueDeletedEvent(containerId));
	}

	public List<IsoCode> getListIsoCodes(Context context, String prefix) {
		try {
			DB db = App.getDB(context);
			String[] keyResults = db.findKeys(prefix);
			List<IsoCode> isoCodes = new ArrayList<IsoCode>();
			for (String result : keyResults) {
				IsoCode isoCode = db.getObject(result, IsoCode.class);
				isoCodes.add(isoCode);
				Logger.Log("getCode: " + isoCode.getCode());
				Logger.Log("getId: " + isoCode.getId());
			}

			return isoCodes;

		} catch (SnappydbException e) {
			Logger.e(e.getMessage());
			return null;
		}
	}

	/**
	 * Get random iso codes include
	 *
	 * @param context
	 * @param prefix
	 * @return
	 */
	public IsoCode getIsoCode(Context context, String prefix) {
		try {
			DB db = App.getDB(context);
			String[] keyResults = db.findKeys(prefix);
			IsoCode isoCode = db.getObject(
					keyResults[new Random().nextInt(keyResults.length)], IsoCode.class);

			Logger.Log("getCode: " + isoCode.getCode());
			Logger.Log("getId: " + isoCode.getId());

			return isoCode;

		} catch (SnappydbException e) {
			Logger.e(e.getMessage());
			return null;
		}
	}

    public IsoCode getIsoCode(Context context, String prefix, String code) {
        try {
            DB db = App.getDB(context);
            String[] keyResults = db.findKeys(prefix + code);
            if (keyResults.length > 0) {
                IsoCode isoCode = db.getObject(keyResults[0], IsoCode.class);

                Logger.Log("getCode: " + isoCode.getCode());
                Logger.Log("getId: " + isoCode.getId());

                return isoCode;
            } else {
                return null;
            }


        } catch (SnappydbException e) {
            Logger.e(e.getMessage());
            return null;
        }
    }

	public void changeUploadState(Context context, String containerId,
	                              AuditItem auditItem) {
		try {
			DB db = App.getDB(context);
			Session session = db.getObject(containerId, Session.class);

			List<AuditItem> list = session.getAuditItems();

			for (AuditItem item : list) {
				if (item.getAuditItemUUID() == auditItem.getAuditItemUUID()) {
					item.setUploadStatus(auditItem.getUploadStatus());
				}
			}

			session.setAuditItems(list);
			db.put(containerId, session);


		} catch (SnappydbException e) {
			e.printStackTrace();
		}

	}

	public void updateAuditItem(Context context, String containerId,
	                            AuditItem auditItem) {
		try {
			DB db = App.getDB(context);
			Session session = db.getObject(containerId, Session.class);

			AuditItem temp = getAuditItemByUUID(context, containerId, auditItem.getAuditItemUUID());
			if (temp != null) {
				session.getAuditItems().remove(temp);
				session.getAuditItems().add(auditItem);
			}

			db.put(containerId, session);

            EventBus.getDefault().post(new IssueUpdatedEvent(containerId));

			Logger.Log("update successfully");

		} catch (SnappydbException e) {
			e.printStackTrace();
		}
	}

	public AuditItem getAuditItemByUUID(Context context, String containerId,
	                                    String auditItemUUID) {
		try {
			DB db = App.getDB(context);
			Session session = db.getObject(containerId, Session.class);

			for (AuditItem auditItem : session.getAuditItems()) {
				if (auditItem.getAuditItemUUID().equals(auditItemUUID)) {
					return auditItem;
				}
			}
			return null;

		} catch (SnappydbException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Change upload status of session
	 *
	 * @param context
	 * @param containerId
	 * @param status
	 * @throws SnappydbException
	 */
	public void changeUploadState(Context context, String containerId, UploadStatus status) throws SnappydbException {

		DB db = App.getDB(context);
		String key = containerId;
		Session session = db.getObject(key, Session.class);
		session.setUploadStatus(status);

		Logger.Log(session.getContainerId() + " -> Upload Status: " + status.name());
		db.put(containerId, session);
	}

	/**
	 * Change local step of given session
	 *
	 * @param context
	 * @param containerId
	 * @param step
	 * @throws SnappydbException
	 */
	public void changeSessionLocalStep(Context context, String containerId, Step step) throws SnappydbException {

		DB db = App.getDB(context);
		Session session = db.getObject(containerId, Session.class);
		session.setLocalStep(step.value);
		db.put(containerId, session);

	}

	public void removeWorkingSession(Context context, String containerId) {

		try {
			DB db = App.getDB(context);
			String workingKey = CJayConstant.PREFIX_WORKING + containerId;
			db.del(workingKey);

		} catch (SnappydbException e) {
			e.printStackTrace();
		}
	}


	//region NOTIFICATION

	/**
	 * Gửi request lên server thông báo đã nhận được notification.
	 */
	@Background
	public void gotMessage(Context context, String channel, String messageId) {
		networkClient.gotMessageFromPubNub(channel, messageId);
	}

	// endregion

	//region AUDIT ITEM

	/**
	 * Get audit item from server by id.
	 * <p/>
	 * 1. Đầu tiên sẽ lấy session từ server.
	 * 2. Kiểm tra trong db có tồn tại session này chưa
	 * 2.1 Nếu có thì update
	 * 2.2 Không có thì tạo mới
	 *
	 * @param context
	 * @param id
	 */
	public void getAuditItemById(Context context, long id) {

		AuditItem auditItem = networkClient.getAuditItemById(id);

		long sessionId = auditItem.getSession();
		Session session = getSessionById(context, sessionId);
		String key = session.getContainerId();

		try {
			DB db = App.getDB(context);
			Session localSession = db.getObject(key, Session.class);
			if (localSession == null) { // container không tồn tại ở client
				db.put(key, localSession);
			} else {
				// TODO: merge audit item to this container session
			}
		} catch (SnappydbException e) {
			e.printStackTrace();
		}
	}
	//endregion
}
