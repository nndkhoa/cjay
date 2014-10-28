package com.cloudjay.cjay;

import android.content.Context;

import com.cloudjay.cjay.api.NetworkClient;
import com.cloudjay.cjay.event.AuditImagesGotEvent;
import com.cloudjay.cjay.event.ContainerSearchedEvent;
import com.cloudjay.cjay.event.GateImagesGotEvent;
import com.cloudjay.cjay.event.IssueDeletedEvent;
import com.cloudjay.cjay.event.OperatorsGotEvent;
import com.cloudjay.cjay.event.SearchAsyncStartedEvent;
import com.cloudjay.cjay.event.WorkingSessionCreatedEvent;
import com.cloudjay.cjay.event.upload.UploadedEvent;
import com.cloudjay.cjay.model.AuditImage;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.model.GateImage;
import com.cloudjay.cjay.model.IsoCode;
import com.cloudjay.cjay.model.LogUpload;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.PreferencesUtil;
import com.cloudjay.cjay.util.enums.UploadStatus;
import com.cloudjay.cjay.util.exception.NullCredentialException;
import com.snappydb.DB;
import com.snappydb.SnappydbException;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.Trace;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.greenrobot.event.EventBus;

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
		db.close();

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
		db.close();

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
		db.close();
	}

	/**
	 * Search for operator
	 *
	 * @param keyword
	 */
	@Background(serial = CACHE)
	public void searchOperator(String keyword) {
		try {
			List<Operator> operators = new ArrayList<Operator>();
			DB db = App.getDB(context);
			String[] keysResult = db.findKeys(CJayConstant.PREFIX_OPERATOR + keyword);
			for (String result : keysResult) {
				Operator operator = db.getObject(result, Operator.class);
				operators.add(operator);
			}
			db.close();
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
			db.close();
			return operator;

		} catch (SnappydbException e) {
			Logger.w(e.getMessage());
			return null;
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

	public void fetchDamageCodes(Context context) throws SnappydbException {
		DB db = App.getDB(context);
		List<IsoCode> damageCodes = networkClient.getDamageCodes(context, null);
		for (IsoCode code : damageCodes) {
			String key = CJayConstant.PREFIX_DAMAGE_CODE + code.getCode();
			db.put(key, code);
		}
		db.close();
	}

	public void fetchRepairCodes(Context context) throws SnappydbException {
		DB db = App.getDB(context);
		List<IsoCode> repairCodes = networkClient.getRepairCodes(context, null);
		for (IsoCode code : repairCodes) {
			String key = CJayConstant.PREFIX_REPAIR_CODE + code.getCode();
			db.put(key, code);
		}
		db.close();
	}

	public void fetchComponentCodes(Context context) throws SnappydbException {
		DB db = App.getDB(context);
		List<IsoCode> componentCodes = networkClient.getComponentCodes(context, null);
		for (IsoCode code : componentCodes) {
			String key = CJayConstant.PREFIX_COMPONENT_CODE + code.getCode();
			db.put(key, code);
		}
		db.close();
	}
	//endregion

	//region SESSION

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
			db.close();

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

		try {

			DB db = App.getDB(context);
			String[] keysResult = db.findKeys(prefix);
			List<Session> sessions = new ArrayList<>();

			for (String result : keysResult) {
				Session session = db.getObject(result, Session.class);
				sessions.add(session);
			}
			db.close();

			return sessions;
		} catch (SnappydbException e) {
			Logger.w(e.getMessage());
			return null;
		}
	}

	//endregion

	/**
	 * Fetch all container session with last modified datetime
	 *
	 * @param context
	 * @param lastModifiedDate
	 * @throws SnappydbException
	 */
	public void fetchSession(Context context, String lastModifiedDate) throws SnappydbException {
		Logger.Log("Fetching Session");
		List<Session> sessions = networkClient.getAllSessions(context, lastModifiedDate);
		DB db = App.getDB(context);
		for (Session session : sessions) {
			String key = session.getContainerId();
			db.put(key, session);
		}
		db.close();
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
	@Background(serial = CACHE)
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

			db.close();

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
	@Background(serial = NETWORK)
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

				db.close();
			}

			EventBus.getDefault().post(new ContainerSearchedEvent(sessions));
		} catch (SnappydbException e) {
			Logger.w(e.getMessage());
		}
	}

	/**
	 * Thêm container session mới vào database
	 *
	 * @param session
	 */
	@Background(serial = CACHE)
	public void addSession(Session session) {
		try {
			DB db = App.getDB(context);

			// Add normal session
			String key = session.getContainerId();
			db.put(key, session);

			// Close db
			db.close();
		} catch (SnappydbException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Add container session vào list working session in database
	 *
	 * @param session
	 */
	@Background(serial = CACHE)
	public void addWorkingSession(Session session) {

		try {
			DB db = App.getDB(context);

			String key = CJayConstant.PREFIX_WORKING + session.getContainerId();
			session.setProcessing(true);
			db.put(key, session);

			db.close();

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
	@Background(serial = CACHE)
	public void addUploadingSession(String containerId) {

		try {
			DB db = App.getDB(context);
			Session session = db.getObject(containerId, Session.class);

			String key = CJayConstant.PREFIX_UPLOADING + containerId;
			db.put(key, session);

			db.close();
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

		db.close();
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
		db.close();
	}

	public void getGateImages(Context context, String containerId) throws SnappydbException {
		DB db = App.getDB(context);
		Session session = db.getObject(containerId, Session.class);
		db.close();

		List<GateImage> gateImages = session.getGateImages();
		EventBus.getDefault().post(new GateImagesGotEvent(gateImages));
	}

	@Background(serial = CACHE)
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

			db.close();
			EventBus.getDefault().post(new ContainerSearchedEvent(sessions));
		} catch (SnappydbException e) {
			e.printStackTrace();
		}

	}

	@Background(serial = CACHE)
	public void getAllGateImagesByContainerId(Context context, String containerId) {
		try {
			DB db = App.getDB(context);
			Session session = db.getObject(containerId, Session.class);
			List<GateImage> gateImages = session.getGateImages();

			Logger.Log("gate images count in dataCenter: " + gateImages.size());
			db.close();

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
	public void uploadImage(Context context, String uri, String imageName, String containerId) throws SnappydbException {

		DB db = App.getDB(context);

		//Call network client to upload image
		networkClient.uploadImage(uri, imageName);

		// Change status image in db
		Session session = db.getObject(CJayConstant.PREFIX_UPLOADING + containerId, Session.class);
		if (session != null) {

			for (GateImage gateImage : session.getGateImages()) {
				if (gateImage.getName().equals(imageName)) {
					gateImage.setUploadStatus(UploadStatus.COMPLETE);
				}
			}

			db.put(CJayConstant.PREFIX_UPLOADING + containerId, session);
		}

		db.close();
		EventBus.getDefault().post(new UploadedEvent(containerId));
	}

	/**
	 * Upload container session and change status uploaded of session to true
	 *
	 * @param context
	 * @param session
	 * @throws SnappydbException
	 */
	public void uploadContainerSession(Context context, Session session) throws SnappydbException {

		DB db = App.getDB(context);
		networkClient.uploadContainerSession(context, session);

		String key = CJayConstant.PREFIX_UPLOADING + session.getContainerId();
		Session sessionUploaded = db.getObject(key, Session.class);

		if (sessionUploaded != null) {
			sessionUploaded.setUploadStatus(UploadStatus.COMPLETE);
			db.put(CJayConstant.PREFIX_UPLOADING + session.getContainerId(), sessionUploaded);
		}

		db.close();

		// TODO: sao không thấy post Event như upload hình?
	}
	//endregion

	@Background(serial = CACHE)
	public void getAuditImages(Context context, String containerId) {


		Session session = null;
		try {
			DB db = App.getDB(context);
			session = db.getObject(containerId, Session.class);
			db.close();
		} catch (SnappydbException e) {
			Logger.w(e.getMessage());
		}

		// Audit and Repaired Images
		List<AuditImage> auditImages = new ArrayList<AuditImage>();

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

			db.close();
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
			db.close();
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
	public List<LogUpload> getListLogUpload(Context context, String logUploadKey) {
		try {
			DB db = App.getDB(context);
			String[] keysResult = db.findKeys(logUploadKey);
			List<LogUpload> logUploads = new ArrayList<LogUpload>();

			for (String result : keysResult) {
				LogUpload logUpload = db.getObject(result, LogUpload.class);
				logUploads.add(logUpload);
			}
			db.close();
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
	public List<LogUpload> searchLogUpload(Context context, String searchKey) {
		try {
			DB db = App.getDB(context);
			String[] keysResult = db.findKeys(CJayConstant.PREFIX_LOGUPLOAD + searchKey);
			List<LogUpload> logUploads = new ArrayList<LogUpload>();

			for (String result : keysResult) {
				LogUpload logUpload = db.getObject(result, LogUpload.class);
				logUploads.add(logUpload);
			}
			db.close();
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
				auditItems = new ArrayList<AuditItem>();
			}

			// Add audit item to List session's audit items
			auditItems.add(auditItem);

			// Add audit item to Session
			session.setAuditItems(auditItems);

			db.put(containerId, session);

			Logger.Log("insert issue repaired successfully");
			db.close();

		} catch (SnappydbException e) {
			Logger.w(e.getMessage());
		}

	}

	// Merge hình vào lỗi đã giám định
	public void addAuditImageToAuditedIssue(Context context,
	                                        String containerId,
	                                        String auditItemUUID,
	                                        AuditItem auditItemRemove,
	                                        AuditImage auditImage) {

		try {
			DB db = App.getDB(context);
			Session session = db.getObject(containerId, Session.class);
			for (AuditItem auditItem : session.getAuditItems()) {

				if (auditItem.getAuditItemUUID().equals(auditItemUUID)) {

					List<AuditImage> auditImages = auditItem.getAuditImages();

					if (null == auditImages) {
						auditImages = new ArrayList<AuditImage>();
					}

					auditImages.add(auditImage);
					auditItem.setAuditImages(auditImages);

					break;
				}
			}

			for (AuditItem auditItem : session.getAuditItems()) {
				if (auditItem.getAuditItemUUID().equals(auditItemRemove.getAuditItemUUID())) {
					session.getAuditItems().remove(auditItem);
					break;
				}
			}

			db.put(containerId, session);
			Logger.Log("add AuditImage To AuditedIssue successfully");
			db.close();

		} catch (SnappydbException e) {
			Logger.w(e.getMessage());
		}

		EventBus.getDefault().post(new IssueDeletedEvent(containerId));
	}

	public void addLogUpload(Context context, LogUpload logUpload) {
		try {

			DB db = App.getDB(context);
			db.put(CJayConstant.PREFIX_LOGUPLOAD + logUpload.getContainerId() + logUpload.getMessage(), logUpload);
			db.close();

		} catch (SnappydbException e) {
			Logger.w(e.getMessage());
		}
	}
}