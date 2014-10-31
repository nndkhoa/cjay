package com.cloudjay.cjay;

import android.content.Context;

import com.cloudjay.cjay.api.NetworkClient;
import com.cloudjay.cjay.event.AuditImagesGotEvent;
import com.cloudjay.cjay.event.ContainerSearchedEvent;
import com.cloudjay.cjay.event.GateImagesGotEvent;
import com.cloudjay.cjay.event.IssueDeletedEvent;
import com.cloudjay.cjay.event.IssueMergedEvent;
import com.cloudjay.cjay.event.OperatorsGotEvent;
import com.cloudjay.cjay.event.SearchAsyncStartedEvent;
import com.cloudjay.cjay.event.WorkingSessionCreatedEvent;
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
		// db.close();

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
		// db.close();

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
		// db.close();
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
			// db.close();
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

			// db.close();
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
		// db.close();
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
		// db.close();
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
		// db.close();
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
			IsoCode isoCode = db.getObject(keyResults[new Random().nextInt(keyResults.length)], IsoCode.class);
			return isoCode;

		} catch (SnappydbException e) {
			Logger.e(e.getMessage());
			return null;
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

			// db.close();
			EventBus.getDefault().post(new ContainerSearchedEvent(sessions));
		} catch (SnappydbException e) {
			e.printStackTrace();
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

			// db.close();

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

				// db.close();
			} else {
				Logger.w("List session is empty");
			}

			EventBus.getDefault().post(new ContainerSearchedEvent(sessions));
		} catch (SnappydbException e) {
			Logger.w(e.getMessage());
		}
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
			// db.close();

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
				// db.close();
			}
		}

		return sessions;
	}

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
			db.put(key, session);
		}
		// db.close();
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
			// db.close();
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
			db.put(key, session);
			// db.close();

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
	public void addUploadSession(String containerId) {

		try {
			DB db = App.getDB(context);

			Session session = db.getObject(containerId, Session.class);
			session.checkRetry();

			db.put(containerId, Session.class);
			String key = CJayConstant.PREFIX_UPLOADING + containerId;
			db.put(key, session);

			// db.close();
		} catch (SnappydbException e) {
			e.printStackTrace();
		}
	}

	public void setSessionStatus(Context context, String containerId, UploadStatus status) {

		try {
			DB db = App.getDB(context);
			String key = containerId;

			Session session = db.getObject(key, Session.class);
			session.setUploadStatus(status);

			db.put(key, session);

		} catch (SnappydbException e) {
			Logger.e(e.getMessage());
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
			// db.close();
			return newSession;
		} catch (SnappydbException e) {
			e.printStackTrace();
			return null;
		}

	}

	//endregion

	//region IMAGE

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

		// db.close();
	}

	public void getGateImages(Context context, String containerId) throws SnappydbException {
		DB db = App.getDB(context);
		Session session = db.getObject(containerId, Session.class);
		// db.close();

		List<GateImage> gateImages = session.getGateImages();
		EventBus.getDefault().post(new GateImagesGotEvent(gateImages));
	}

	@Background(serial = CACHE)
	public void getAllGateImagesByContainerId(Context context, String containerId) {
		try {
			DB db = App.getDB(context);
			Session session = db.getObject(containerId, Session.class);
			List<GateImage> gateImages = session.getGateImages();

			Logger.Log("gate images count in dataCenter: " + gateImages.size());
			// db.close();

			EventBus.getDefault().post(new GateImagesGotEvent(gateImages));
		} catch (SnappydbException e) {
			e.printStackTrace();
		}
	}


	/**
	 * Add audit image to container session
	 *
	 * @param context
	 * @param auditImage
	 * @param containerId
	 * @throws SnappydbException
	 */
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
		// db.close();
	}

	@Background(serial = CACHE)
	public void getAuditImages(Context context, String containerId) {
		Session session = null;
		try {
			DB db = App.getDB(context);
			session = db.getObject(containerId, Session.class);
			// db.close();
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

	public void setUploadStatus(Context context, String containerId, String imageName, ImageType type, UploadStatus status) throws SnappydbException {

		DB db = App.getDB(context);

		// Change status image in db
		String key = containerId;
		Session session = db.getObject(key, Session.class);

		if (session != null) {
			switch (type) {

				case AUDIT:
				case REPAIRED:
					for (AuditItem auditItem : session.getAuditItems()) {
						for (AuditImage auditImage : auditItem.getAuditImages()) {
							if (auditImage.getName().equals(imageName) && auditImage.getType() == type.value) {
								auditImage.setUploadStatus(status);
							}
						}
					}
					break;

				case IMPORT:
				case EXPORT:
				default:
					for (GateImage gateImage : session.getGateImages()) {
						if (gateImage.getName().equals(imageName) && gateImage.getType() == type.value) {
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

	//endregion

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
			setUploadStatus(context, containerId, imageName, imageType, UploadStatus.COMPLETE);

			return true;
		} catch (RetrofitError e) {
			e.printStackTrace();
			return false;
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
	 * @param session
	 * @throws SnappydbException
	 */
	public void uploadSession(Context context, Session session) throws SnappydbException {
		DB db = App.getDB(context);

		// Change local step to import
		session.setLocalStep(Step.AUDIT.value);
		db.put(session.getContainerId(), session);

		// Check for make sure all gate image have uploaded
		for (GateImage gateImage : session.getGateImages()) {
			if (gateImage.getUploadStatus() != UploadStatus.COMPLETE.value && gateImage.getType() == ImageType.IMPORT.value) {

				//TODO Note to Khoa this upload import session have to retry upload Image @Han
				uploadImage(context, Utils.parseUrltoUri(gateImage.getUrl()), gateImage.getName(), session.getContainerId(), ImageType.IMPORT);
			}
		}

		// Upload container session to server
		Session result = networkClient.uploadSession(context, session);
		Logger.Log("Uploaded Session Id: " + result.getId());

		if (result != null) {

			// Update container back to database
			String key = result.getContainerId();
			result.setUploadStatus(UploadStatus.COMPLETE);
			db.put(key, result);

			// Then remove them from WORKING
			String workingKey = CJayConstant.PREFIX_WORKING + key;
			db.del(workingKey);
		}
		// db.close();
	}

	public void uploadAuditItem(Context context, String containerId, AuditItem auditItem) throws SnappydbException {

		DB db = App.getDB(context);
		Session oldSession = db.getObject(containerId, Session.class);

		//Check for make sure all image of this audit item had uploaded
		for (AuditImage auditImage : auditItem.getAuditImages()) {
			if (auditImage.getUploadStatus() != UploadStatus.COMPLETE.value && auditImage.getType() == ImageType.AUDIT.value) {
				//TODO Note to Khoa this upload audit Item have to retry upload Image @Han
				uploadImage(context, Utils.parseUrltoUri(auditImage.getUrl()), auditImage.getName(), containerId, ImageType.AUDIT);
			}
		}

		// Check for make sure this container had uploaded to be import step
		if (oldSession.getStep() != Step.AUDIT.value) {
			//TODO Note to Khoa this upload audit item have to retry upload import session @Han
			uploadSession(context, oldSession);
		}

		// Upload audit item session to server
		Session result = networkClient.postAuditItem(context, oldSession, auditItem);
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

	public void uploadExportSession(Context context, Session session) throws SnappydbException {

		DB db = App.getDB(context);
		Session oldSession = db.getObject(session.getContainerId(), Session.class);

		//Check for make sure all image of this audit item had uploaded
		for (GateImage gateImage : session.getGateImages()) {
			if (gateImage.getUploadStatus() != UploadStatus.COMPLETE.value && gateImage.getType() == ImageType.EXPORT.value) {
				//TODO Note to Khoa this upload export session have to retry upload Image @Han
				uploadImage(context, Utils.parseUrltoUri(gateImage.getUrl()), gateImage.getName(), session.getContainerId(), ImageType.EXPORT);
			}
		}

		// Check for make sure this container had uploaded to be import step
		if (oldSession.getStep() != Step.AVAILABLE.value) {
			//TODO Note to Khoa this upload export session have to retry upload complete repair @Han
			uploadCompleteRepairSession(context, oldSession.getContainerId());
		}

		// Upload audit item session to server
		Session result = networkClient.checkOutContainerSession(context, oldSession);
		Logger.Log("Add AuditItem to Session Id: " + result.getId());

		if (result != null) {
			db.close();
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

	public void uploadCompleteAuditSession(Context context, String containerId) throws SnappydbException {

		DB db = App.getDB(context);
		Session oldSession = db.getObject(containerId, Session.class);

		// Change local step to audit
		oldSession.setLocalStep(Step.REPAIR.value);
		db.put(oldSession.getContainerId(), oldSession);

		// Check for make sure all audit item had uploaded
		for (AuditItem auditItem : oldSession.getAuditItems()) {
			if (auditItem.getUploadStatus() != UploadStatus.COMPLETE.value) {
				//TODO Note to Khoa this upload complete audit session have to retry upload audit item @Han
				Logger.Log("containerId: " + containerId);
				uploadAuditItem(context, containerId, auditItem);
			}
		}

		// Upload complete audit session to server
		Session result = networkClient.completeAudit(context, oldSession);
		Logger.Log("Add AuditItem to Session Id: " + result.getId());

		if (result != null) {
			db.close();
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

	public void uploadCompleteRepairSession(Context context, String containerId) throws SnappydbException {

		DB db = App.getDB(context);
		Session oldSession = db.getObject(containerId, Session.class);

		// Change local step to repair
		oldSession.setLocalStep(Step.AVAILABLE.value);
		db.put(oldSession.getContainerId(), oldSession);

		// Check for sure all repaired image had uploaded
		for (AuditItem auditItem : oldSession.getAuditItems()) {
			for (AuditImage auditImage : auditItem.getAuditImages()) {
				if (auditImage.getUploadStatus() != UploadStatus.COMPLETE.value && auditImage.getType() == ImageType.REPAIRED.value) {
					//TODO Note to Khoa this upload complete repair session have to retry upload repaired item @Han
					uploadImage(context, Utils.parseUrltoUri(auditImage.getUrl()), auditImage.getName(), containerId, ImageType.REPAIRED);
				}
			}
		}

		// Check for sure session is complete audit step had uploaded to server
		if (oldSession.getStep() != Step.AUDIT.value) {
			//TODO Note to Khoa this upload complete repair session have to retry upload complete audit @Han
			uploadCompleteAuditSession(context, containerId);
		}

		// Upload complete repair session to server
		Session result = networkClient.completeRepairSession(context, oldSession);
		Logger.Log("Add AuditItem to Session Id: " + result.getId());

		if (result != null) {
			db.close();
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


	//endregion

	//region AUDIT

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

	/**
	 * Merge hình vào lỗi đã giám định.
	 *
	 * @param context
	 * @param containerId
	 * @param auditItemUUID
	 * @param auditItemRemove
	 * @param auditImage
	 */
	public void addAuditImageToAuditedIssue(Context context,
	                                        String containerId,
	                                        String auditItemUUID,
	                                        String auditItemRemove,
	                                        AuditImage auditImage) {

		try {
			DB db = App.getDB(context);
			Session session = db.getObject(containerId, Session.class);
			for (AuditItem auditItem : session.getAuditItems()) {

				if (auditItem.getAuditItemUUID().equals(auditItemUUID)) {
					List<AuditImage> auditImages = auditItem.getAuditImages();
					if (null == auditImages) {
						auditImages = new ArrayList<>();
					}

					auditImages.add(auditImage);
					auditItem.setAuditImages(auditImages);

					break;
				}
			}

			db.put(containerId, session);
			Logger.Log("add AuditImage To AuditedIssue successfully");
			// db.close();

		} catch (SnappydbException e) {
			Logger.w(e.getMessage());
		}

		EventBus.getDefault().post(new IssueMergedEvent(containerId, auditItemRemove));
	}


	/**
	 * Thêm lỗi đã giám định
	 *
	 * @param context
	 * @param auditItem
	 * @param containerId
	 */
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

			Logger.Log("insert issue repaired successfully");
			// db.close();

		} catch (SnappydbException e) {
			Logger.w(e.getMessage());
		}

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

	/**
	 * Change audit item's upload status
	 *
	 * @param context
	 * @param containerId
	 * @param auditItem
	 */
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

	//endregion

	//region LOG

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
			// db.close();

		} catch (SnappydbException e) {
			Logger.w(e.getMessage());
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
			// db.close();
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

			// db.close();
			return logUploads;
		} catch (SnappydbException e) {
			Logger.w(e.getMessage());
			return null;
		}
	}
	//endregion

	//region NOTIFICATION

	/**
	 * Gửi request lên server thông báo đã nhận được notification.
	 */
	public void gotMessage(Context context, String channel, long messageId) {
		networkClient.gotMessageFromPubNub(channel, messageId);
	}
	// endregion
}
