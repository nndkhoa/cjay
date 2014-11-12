package com.cloudjay.cjay;

import android.content.Context;
import android.text.TextUtils;

import com.cloudjay.cjay.api.NetworkClient;
import com.cloudjay.cjay.event.ContainerGotEvent;
import com.cloudjay.cjay.event.image.AuditImagesGotEvent;
import com.cloudjay.cjay.event.issue.AuditItemChangedEvent;
import com.cloudjay.cjay.event.issue.AuditItemsGotEvent;
import com.cloudjay.cjay.event.issue.IssueMergedEvent;
import com.cloudjay.cjay.event.operator.OperatorsGotEvent;
import com.cloudjay.cjay.event.session.ContainerForUploadGotEvent;
import com.cloudjay.cjay.event.session.ContainerSearchedEvent;
import com.cloudjay.cjay.event.session.ContainersGotEvent;
import com.cloudjay.cjay.event.session.SearchAsyncStartedEvent;
import com.cloudjay.cjay.event.session.WorkingSessionCreatedEvent;
import com.cloudjay.cjay.event.upload.UploadSucceededEvent;
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
import com.cloudjay.cjay.util.enums.ImageType;
import com.cloudjay.cjay.util.enums.Step;
import com.cloudjay.cjay.util.enums.UploadStatus;
import com.cloudjay.cjay.util.enums.UploadType;
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

	/**
	 * Request token from server. Throw exception if no connectivity
	 *
	 * @param email
	 * @param password
	 * @return
	 */
	public String getToken(String email, String password) throws RetrofitError {
		return networkClient.getToken(email, password);
	}

	/**
	 * Get current logged in user information from database
	 *
	 * @param context
	 * @return
	 * @throws SnappydbException
	 * @throws NullCredentialException
	 */
	public User getUser(Context context) throws SnappydbException, NullCredentialException {

		DB db = App.getDB(context);
		User user = db.getObject(CJayConstant.PREFIX_USER, User.class);


		if (null == user) {
			return getCurrentUserAsync(context);
		} else {
			return user;
		}
	}

	/**
	 * Request server for current user information
	 *
	 * @param context
	 * @return
	 * @throws SnappydbException
	 * @throws NullCredentialException
	 */
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
	@Background(serial = CACHE)
	public void searchOperator(String keyword) {
		try {
			List<Operator> operators = new ArrayList<>();
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
	 * Get operator from server by id
	 *
	 * @param context
	 * @param id
	 */
	@Background(serial = NETWORK)
	public void getOperatorAsyncById(Context context, long id) {

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
	 * Get list of Iso Code based on given prefix (code type)
	 *
	 * @param context
	 * @param prefix
	 * @return
	 */
	public List<IsoCode> getListIsoCodes(Context context, String prefix) {
		try {
			DB db = App.getDB(context);
			String[] keyResults = db.findKeys(prefix);
			List<IsoCode> isoCodes = new ArrayList<>();

			for (String result : keyResults) {
				IsoCode isoCode = db.getObject(result, IsoCode.class);
				isoCodes.add(isoCode);
			}

			return isoCodes;

		} catch (SnappydbException e) {
			Logger.e(e.getMessage());
			return null;
		}
	}

	/**
	 * get single damage code by Id
	 *
	 * @param context
	 * @param id
	 */
	@Background(serial = NETWORK)
	public void getDamageCodeAsyncById(Context context, long id) {
		IsoCode isoCode = networkClient.getDamageCodeById(id);
		addIsoCode(context, isoCode, IsoCode.Type.DAMAGE);
	}

	/**
	 * get single repair code by Id
	 *
	 * @param context
	 * @param id
	 */
	@Background(serial = NETWORK)
	public void getRepairCodeAsyncById(Context context, long id) {
		IsoCode isoCode = networkClient.getRepairCodeById(id);
		addIsoCode(context, isoCode, IsoCode.Type.REPAIR);
	}

	/**
	 * get single component code by Id
	 *
	 * @param context
	 * @param id
	 */
	@Background(serial = NETWORK)
	public void getComponentCodeAsyncById(Context context, long id) {
		IsoCode isoCode = networkClient.getComponentCodeById(id);
		addIsoCode(context, isoCode, IsoCode.Type.COMPONENT);
	}

	/**
	 * @param context
	 * @param prefix
	 * @param code
	 * @return
	 */
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

	//endregion

	//region SESSION

	/**
	 * get Session from server
	 *
	 * @param context
	 * @param id
	 * @return
	 * @throws SnappydbException
	 */
	public Session getSessionAsyncById(Context context, long id) {

		Session result = networkClient.getSessionById(id);
		try {
			// Get session from server
			DB db = App.getDB(context);
			if (result != null) {

				// Find local session
				String key = result.getContainerId();
				Session session = db.get(key, Session.class);

//				SimpleDateFormat format = new SimpleDateFormat(CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE);
//				try {
//
//					Date server = format.parse(result.getModifiedAt());
//					Date local = format.parse(session.getModifiedAt());
//
//				} catch (ParseException e) {
//					e.printStackTrace();
//				}

				// merge result from server to local session
				Logger.w("From get session async");
				session.mergeSession(result);
				db.put(key, session);
				return session;

			}
		} catch (SnappydbException e) {

			//Merge Session from server to local type

			Logger.w(e.getMessage());
			Logger.w("Received new container session from server: " + result.getContainerId());
			result.changeToLocalFormat();
			addSession(result);
			return result;

		}
		return null;
	}

	@Background(serial = CACHE)
	public void getSessionForUpload(Context context, String containerId) {
		try {
			DB db = App.getDB(context);
			String key = containerId;
			Session session = db.getObject(key, Session.class);

			List<Session> list = new ArrayList<>();
			list.add(session);
			EventBus.getDefault().post(new ContainerForUploadGotEvent(list, containerId));

		} catch (SnappydbException e) {
			Logger.w(e.getMessage());
		}
	}

	/**
	 * Get list container sessions based on param `prefix`
	 *
	 * @param context
	 * @param prefix
	 * @return
	 */
	@Background(serial = CACHE)
	public void getSessionsInBackground(Context context, String prefix) {
//		Logger.Log("Getting list session: " + prefix);

		int len = prefix.length();

		DB db = null;
		String[] keysResult = new String[0];
		List<Session> sessions = new ArrayList<>();

		try {
			db = App.getDB(context);
			keysResult = db.findKeys(prefix);
		} catch (SnappydbException e) {
			Logger.e(e.getMessage());
		}

		for (String result : keysResult) {

			String newKey = result.substring(len);
			Session session;
			try {
				session = db.getObject(newKey, Session.class);
				sessions.add(session);
			} catch (SnappydbException e) {
				e.printStackTrace();
				addLog(context, newKey, prefix + " | Cannot retrieve this container");
			}
		}

		EventBus.getDefault().post(new ContainersGotEvent(sessions, prefix));
	}

	@Background(serial = CACHE)
	public void getSessionInBackground(Context context, String containerId) {

		try {
			DB db = App.getDB(context);
			String key = containerId;
			Session session = db.getObject(key, Session.class);
			EventBus.getDefault().post(new ContainerGotEvent(session, containerId));

		} catch (SnappydbException e) {
			Logger.w(e.getMessage());
		}
	}

	@Background(serial = CACHE)
	public void changeSessionLocalStepInBackground(Context context, String containerId, Step step) {

		DB db;
		try {
			db = App.getDB(context);
			Session session = db.getObject(containerId, Session.class);
			session.setLocalStep(step.value);
			db.put(containerId, session);
		} catch (SnappydbException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Fetch all container session with last modified datetime
	 * If refetchWithFistPageTime == true => after fetch all page, fetch again with modified_since is the time of fetched page 1
	 * <p/>
	 * 1.Get current page from preferences
	 * 2.If current page is null =>> next page use to get session = 1
	 * Else next page to get session = current page +1
	 * 3. Refresh page after fetched
	 * 5. Fetch again with modified day is first page request_time for make sure all data have update
	 *
	 * @param context
	 * @param lastModifiedDate
	 * @param refetchWithFistPageTime
	 */
	public void fetchSession(Context context, String lastModifiedDate, boolean refetchWithFistPageTime) {

		String newModifiedDay;
		do {

			int nextPage;
			String currentPage = PreferencesUtil.getPrefsValue(context, PreferencesUtil.PREF_MODIFIED_PAGE);

			if (currentPage.isEmpty()) {
				nextPage = 1;
			} else {
				nextPage = Integer.valueOf(currentPage) + 1;
			}

			List<Session> sessions = networkClient.getSessionByPage(context, nextPage, lastModifiedDate);
			processListSession(sessions);

			newModifiedDay = PreferencesUtil.getPrefsValue(context, PreferencesUtil.PREF_MODIFIED_DATE);
			Logger.Log("Fetched page: " + nextPage);
			Logger.Log("Current Modified day: " + newModifiedDay);

		} while (lastModifiedDate.equals(newModifiedDay));

		PreferencesUtil.storePrefsValue(context, PreferencesUtil.PREF_MODIFIED_PAGE, "");

		if (refetchWithFistPageTime) {
			//Fetch again with modified day is first page request_time
			String firstPageTime = PreferencesUtil.getPrefsValue(context, PreferencesUtil.PREF_FIRST_PAGE_MODIFIED_DATE);
			fetchSession(context, firstPageTime, false);
		}
	}

	@Background(serial = CACHE)
	void processListSession(List<Session> sessions) {

		Logger.w("From process list session");
		DB db;
		try {
			db = App.getDB(context);
			for (Session session : sessions) {

				String key = session.getContainerId();
				String[] searchResult = db.findKeys(key);
				if (session.getStep() == Step.EXPORTED.value) {
					if (searchResult.length != 0) {
						db.del(key);
					}
				} else {
					if (searchResult.length == 0) {
						session.changeToLocalFormat();
						addSession(session);
					} else {
						Session local = db.getObject(key, Session.class);
						local.mergeSession(session);
						db.put(key, local);
					}
				}
			}
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
			EventBus.getDefault().post(new ContainerSearchedEvent(true));
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

		} catch (SnappydbException e) {
			e.printStackTrace();
		}
	}

	@Background(serial = CACHE)
	void saveSession(Context context, Session session, UploadType type) {

		DB db = null;
		String key = session.getContainerId();
		Session object = null;
		try {
			db = App.getDB(context);

			object = db.getObject(key, Session.class);
			Logger.w("From save session: " + session.getModifiedAt());

			object.mergeSession(session);
			object.setUploadStatus(UploadStatus.COMPLETE);
			db.put(key, object);

		} catch (SnappydbException e) {

			// change session to local
			Logger.wtf(e.getMessage());
			try {
				object = session.changeToLocalFormat();
				db.put(key, object);
			} catch (SnappydbException e1) {
				e1.printStackTrace();
			}
		} finally {
			EventBus.getDefault().post(new UploadSucceededEvent(object, UploadType.SESSION));
		}
	}


	@Background(serial = CACHE)
	public void updateImportSession(Session session) {
		DB db = null;
		try {
			db = App.getDB(context);

			Session oldSession = db.getObject(session.getContainerId(), Session.class);


			oldSession.setOperatorId(session.getOperatorId());
			oldSession.setOperatorCode(session.getOperatorCode());
			oldSession.setPreStatus(session.getPreStatus());

			// Add normal session
			String key = session.getContainerId();
			db.put(key, oldSession);

		} catch (SnappydbException e) {
			e.printStackTrace();
			String key = session.getContainerId();
			try {
				db.put(key, session);
			} catch (SnappydbException e1) {
				e1.printStackTrace();
			}
		} finally {
			getSessionInBackground(context, session.getContainerId());
		}
	}

	/**
	 * Add container session vào list working session in database
	 *
	 * @param session
	 */
	@Trace
	@Background(serial = CACHE)
	public void addWorkingSession(Session session) {

		try {
			DB db = App.getDB(context);

			String key = CJayConstant.PREFIX_WORKING + session.getContainerId();
			db.put(key, session);

			// Log
			Step step = Step.values()[session.getLocalStep()];
			Logger.Log("Add container " + session.getContainerId() + " | " + step.name() + " to Working collection");

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
			String key = CJayConstant.PREFIX_UPLOADING + containerId;
			db.put(key, session);

			Step step = Step.values()[session.getLocalStep()];
			Logger.Log("Add container " + session.getContainerId() + " | " + step.name() + " to Upload collection");

		} catch (SnappydbException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param context
	 * @param containerId
	 */
	@Background(serial = CACHE)
	@Trace
	public void removeWorkingSession(Context context, String containerId) {

		try {
			DB db = App.getDB(context);

			String workingKey = CJayConstant.PREFIX_WORKING + containerId;
			db.del(workingKey);

			Logger.Log("REMOVE container " + containerId + " from Working collection");

		} catch (SnappydbException e) {
			e.printStackTrace();
		}
	}


	@Background(serial = CACHE)
	public void changeStatusWhenUpload(Context context, Session session, UploadType uploadType, UploadStatus uploadStatus) {

		DB db = null;
		try {
			db = App.getDB(context);
			if (uploadStatus == UploadStatus.UPLOADING) {
				//Change local step
				Step step = Step.values()[session.getLocalStep()];
				switch (step) {
					case IMPORT:
						session.setLocalStep(Step.AUDIT.value);
						break;
					case AUDIT:
						session.setLocalStep(Step.REPAIR.value);
						break;
					case REPAIR:
						session.setLocalStep(Step.AVAILABLE.value);
						break;
					case AVAILABLE:
					default:
						session.setLocalStep(Step.EXPORTED.value);
						break;
				}

				//Add to uploading
				addUploadSession(session.getContainerId());

				//Change upload status
				session.setUploadStatus(uploadStatus);
			} else if (uploadStatus == UploadStatus.COMPLETE) {
				session.setUploadStatus(uploadStatus.value);
			}

			//Update new session to db
			db.put(session.getContainerId(), session);
		} catch (SnappydbException e) {
			e.printStackTrace();
		}


	}

	//endregion

	//region IMAGE

	/**
	 * Merge hình vào lỗi đã giám định.
	 *
	 * @param context
	 * @param containerId
	 * @param auditItemUUID
	 * @param auditItemRemove
	 * @param auditImageUUID
	 */
	public void addAuditImageToAuditedItem(Context context,
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
			AuditItem removeItem = getAuditItem(context, containerId, auditItemRemove);
			if (removeItem != null) {
				Logger.Log("Begin to remove");
				session.getAuditItems().remove(removeItem);
			}

			AuditItem itemMerged = getAuditItem(context, containerId, auditItemUUID);
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
			Session session = db.getObject(containerId, Session.class);

			if (session != null) {

				// Find audit item
				for (AuditItem auditItem : session.getAuditItems()) {
					if (auditItem.getUuid().equals(auditItemUUID)) {

						// Find audit image
						for (AuditImage auditImage : auditItem.getAuditImages()) {
							if (auditImage.getUuid().equals(auditImageUUID))
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

	/**
	 * Add image to container Session
	 *
	 * @param image
	 * @param containerId
	 * @throws SnappydbException
	 */
	@Background(serial = CACHE)
	public void addGateImage(Context context, GateImage image, String containerId) {

		try {
			DB db = App.getDB(context);

			// Add gate image to normal container session
			Session session = db.getObject(containerId, Session.class);
			session.getGateImages().add(image);

			String key = containerId;
			db.put(key, session);
		} catch (SnappydbException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Add audit image to tmp audit item in given container session
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
		auditItem.setUuid(uuid);
		auditItem.setAudited(false);
		auditItem.setRepaired(false);
		auditItem.setAllowed(null);
		auditItem.setUploadStatus(UploadStatus.NONE);

		// Get list session's audit items
		List<AuditItem> auditItems = session.getAuditItems();
		if (auditItems == null) {
			auditItems = new ArrayList<>();
		}

		List<AuditImage> auditImages = auditItem.getAuditImages();
		if (auditImages == null) {
			auditImages = new ArrayList<>();
		}
		// Add audit image to list audit images
		auditImages.add(auditImage);
		auditItem.setAuditImages(auditImages);

		// Add audit item to List session's audit items
		auditItems.add(auditItem);

		// Add audit item to Session
		session.setAuditItems(auditItems);

		db.put(containerId, session);

		Logger.Log("Insert audit image successfully");
	}

	/**
	 * Change upload status of given image
	 *
	 * @param context
	 * @param containerId
	 * @param imageName
	 * @param imageType
	 * @param status
	 * @throws SnappydbException
	 */
	@Background(serial = CACHE)
	public void changeImageUploadStatus(Context context, String containerId, String imageName, ImageType imageType, UploadStatus status) {
		try {
			DB db = App.getDB(context);

			// Change status image in db
			String key = containerId;
			Session session = db.getObject(key, Session.class);

			if (session != null) {
				switch (imageType) {

					case AUDIT:
						for (AuditItem auditItem : session.getAuditItems()) {
							for (AuditImage auditImage : auditItem.getListAuditedImages()) {
								if (!TextUtils.isEmpty(auditImage.getName())) {
									if (auditImage.getName().contains(imageName) && auditImage.getType() == imageType.value) {
										auditImage.setUploadStatus(status);
									}
								} else {
									Logger.wtf(auditImage.getUrl() + " does not have image name");
								}
							}
						}

					case REPAIRED:
						for (AuditItem auditItem : session.getAuditItems()) {
							for (AuditImage auditImage : auditItem.getListRepairedImages()) {
								if (!TextUtils.isEmpty(auditImage.getName())) {
									if (auditImage.getName().contains(imageName) && auditImage.getType() == imageType.value) {
										auditImage.setUploadStatus(status);
									}
								} else {
									Logger.wtf(auditImage.getUrl() + " does not have image name");
								}
							}
						}
						break;

					case IMPORT:
					case EXPORT:
					default:
						for (GateImage gateImage : session.getGateImages()) {
							if (!TextUtils.isEmpty(gateImage.getName())) {
								if (gateImage.getName().contains(imageName) && gateImage.getType() == imageType.value) {
									gateImage.setUploadStatus(status);
									break;
								}
							} else {
								Logger.wtf(gateImage.getUrl() + " does not have image name");
							}
						}
						break;
				}

				db.put(key, session);
				if (status == UploadStatus.COMPLETE) {
					EventBus.getDefault().post(new UploadSucceededEvent(containerId, UploadType.IMAGE));
				}
			}
		} catch (SnappydbException e) {
			e.printStackTrace();
		}
	}

	//endregion

	//region UPLOAD

	/**
	 * 1. Tìm session với containerId trong list uploading.
	 * 2. Upload hình và gán field uploaded ngược vào list uploading
	 *
	 * @param uri
	 * @param imageName
	 * @throws SnappydbException
	 */
	public void uploadImage(String uri, String imageName) throws SnappydbException {

		//Call network client to upload image
		networkClient.uploadImage(uri, imageName);

	}

	/**
	 * Upload audit item to server.
	 * 1. Get audit item based on uuid
	 * 2. Upload to server
	 * 3. Assign uuid to response audit item
	 * 4. Replace result with local audit item in container session
	 *
	 * @param context
	 * @param session
	 * @param itemUuid
	 * @throws SnappydbException
	 */
	public void uploadAuditItem(Context context, Session session, String itemUuid) throws SnappydbException {

		AuditItem auditItem = session.getAuditItem(itemUuid);
		AuditItem result = networkClient.postAuditItem(context, session, auditItem);
		session.updateAuditItem(result);
		saveSession(context, session, UploadType.AUDIT_ITEM);

	}

	/**
	 * Upload import container session
	 *
	 * @param context
	 * @param session
	 * @throws SnappydbException
	 */
	public void uploadImportSession(Context context, Session session) {
		Session result = networkClient.uploadSession(context, session);
		saveSession(context, result, UploadType.SESSION);
	}

	/**
	 * Upload audited container session
	 *
	 * @param context
	 * @param session
	 * @throws SnappydbException
	 */
	public void uploadAuditSession(Context context, Session session) throws SnappydbException {

		Session result = networkClient.completeAudit(context, session);
		saveSession(context, result, UploadType.SESSION);
	}

	/**
	 * Upload repaired container session
	 *
	 * @param context
	 * @param session
	 * @throws SnappydbException
	 */
	public void uploadRepairSession(Context context, Session session) throws SnappydbException {

		Session result = networkClient.completeRepairSession(context, session);
		saveSession(context, result, UploadType.SESSION);
	}

	/**
	 * @param context
	 * @param session
	 * @throws SnappydbException
	 */
	public void uploadExportSession(Context context, Session session) throws SnappydbException {

		Session result = networkClient.checkOutContainerSession(context, session);
		saveSession(context, result, UploadType.SESSION);
	}

	/**
	 * Set session have containerId is hand cleaning session, upload this to server then add new session return from server to database
	 *
	 * @param context
	 * @param session
	 * @return
	 */
	public void setHandCleaningSession(Context context, Session session) {

		Session result = networkClient.setHandCleaningSession(context, session);
		saveSession(context, result, UploadType.SESSION);

	}

	//endregion

	//region LOG

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
	 * @param title
	 * @param message
	 */
	public void addLog(Context context, String title, String message) {
		String currentTime = StringUtils.getCurrentTimestamp(CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE);

		LogItem log = new LogItem();
		log.setContainerId(title);
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
	//endregion

	//region NOTIFICATION

	/**
	 * Gửi request lên server thông báo đã nhận được notification.
	 */
	@Background(serial = NETWORK)
	public void gotMessage(Context context, String channel, String messageId) {
		try {
			networkClient.gotMessageFromPubNub(channel, messageId);
		} catch (RetrofitError e) {
			Logger.e(e.getMessage());
		}
	}

	// endregion

	//region AUDIT ITEM

	/**
	 * Xóa lỗi sau khi merge
	 *
	 * @param context
	 * @param containerId
	 * @param auditItemUUID
	 */
	public void removeAuditItem(Context context, String containerId, String auditItemUUID) {
		try {
			// find session
			DB db = App.getDB(context);
			Session session = db.getObject(containerId, Session.class);

			// Remove audit item from session
			if (session != null) {
				session.removeAuditItem(auditItemUUID);
				db.put(containerId, session);
			}

		} catch (SnappydbException e) {
			e.printStackTrace();
		}

		EventBus.getDefault().post(new AuditItemChangedEvent(containerId));
	}

	/**
	 * Update given audit item
	 *
	 * @param context
	 * @param containerId
	 * @param auditItem
	 */
	public void updateAuditItem(Context context, String containerId, AuditItem auditItem) {
		try {

			// find session
			DB db = App.getDB(context);
			Session session = db.getObject(containerId, Session.class);
			session.updateAuditItem(auditItem);
			db.put(containerId, session);

			// Notify that an audit item is updated
			EventBus.getDefault().post(new AuditItemChangedEvent(containerId));

		} catch (SnappydbException e) {
			e.printStackTrace();
		}
	}

	@Background(serial = CACHE)
	public void updateAuditItemInBackground(Context context, String containerId, AuditItem auditItem) {
		try {
			// find session
			DB db = App.getDB(context);
			Session session = db.getObject(containerId, Session.class);
			session.updateAuditItem(auditItem);
			db.put(containerId, session);

			// Notify that an audit item is updated
			EventBus.getDefault().post(new AuditItemChangedEvent(containerId));

		} catch (SnappydbException e) {
			e.printStackTrace();
		}
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
	 * @param auditItem
	 * @throws SnappydbException
	 */
	public void setWaterWashType(Context context, final AuditItem auditItem, String containerId) throws SnappydbException {

		Logger.Log("Selected audit item: " + auditItem.getUuid());

		// Find session
		DB db = App.getDB(context);
		Session session = db.getObject(containerId, Session.class);

		// --> Nếu nhiều image cùng được chọn là lỗi vệ sinh, thì gộp tất cả vào một lỗi
		boolean isExisted = false;

		List<AuditItem> list = session.getAuditItems();
		for (int i = 0; i < list.size(); i++) {

			// neu da ton tai loi ve sinh va chua duoc upload thi them hinh vao loi ve sinh
			if (list.get(i).isWashTypeItem() && list.get(i).getId() == 0) {
				list.get(i).getAuditImages().add(auditItem.getAuditImages().get(0));
				isExisted = true;
				Logger.Log("Images size: " + list.get(i).getAuditImages().size());
				Logger.Log("existed");
				break;
			}
		}

		// Remove temporary audit item
		for (int i = 0; i < list.size(); i++) {
			AuditItem tmp = list.get(i);
			if (tmp.equals(auditItem)) {
				list.remove(i);
			}
		}

		if (!isExisted) {

			Logger.Log("Create new audit item");

			// Add Isso Code
			String damageKey = CJayConstant.PREFIX_DAMAGE_CODE + "DB";
			String repairKey = CJayConstant.PREFIX_REPAIR_CODE + "WW";
			String componentKey = CJayConstant.PREFIX_COMPONENT_CODE + "FWA";

			IsoCode damageCode = db.getObject(damageKey, IsoCode.class);
			IsoCode repairCode = db.getObject(repairKey, IsoCode.class);
			IsoCode componentCode = db.getObject(componentKey, IsoCode.class);

			auditItem.setDamageCodeId(damageCode.getId());
			auditItem.setDamageCode(damageCode.getCode());

			auditItem.setRepairCodeId(repairCode.getId());
			auditItem.setRepairCode(repairCode.getCode());

			auditItem.setComponentCodeId(componentCode.getId());
			auditItem.setComponentCode(componentCode.getCode());

			auditItem.setLocationCode("BXXX");
			auditItem.setAudited(true);
			auditItem.setAllowed(true);

			list.add(auditItem);
		}

		session.setAuditItems(list);
		db.put(containerId, session);

		EventBus.getDefault().post(new AuditItemChangedEvent(containerId));
	}

	/**
	 * Get audit item from server by id.
	 * 1. Đầu tiên sẽ lấy session từ server.
	 * 2. Kiểm tra trong db có tồn tại session này chưa
	 * 2.1 Nếu có thì update
	 * 2.2 Không có thì tạo mới
	 *
	 * @param context
	 * @param id
	 */
	public Session getAuditItemAsyncById(Context context, long id) throws SnappydbException {

		AuditItem auditItem = networkClient.getAuditItemById(id);
		if (auditItem != null) {

			long sessionId = auditItem.getSession();
			Session session = getSessionAsyncById(context, sessionId);
			return session;
		}

		return null;
	}

	/**
	 * Change upload status of an audit item
	 *
	 * @param context
	 * @param containerId
	 * @param auditItem
	 */
	public void changeUploadStatus(Context context, String containerId, AuditItem auditItem, UploadStatus status) throws SnappydbException {
		changeUploadStatus(context, containerId, auditItem.getUuid(), status);
	}

	/**
	 * Change upload status of audit item
	 *
	 * @param context
	 * @param containerId
	 * @param itemUuid
	 * @param status
	 */
	public void changeUploadStatus(Context context, String containerId, String itemUuid, UploadStatus status) throws SnappydbException {
		DB db = App.getDB(context);
		Session session = db.getObject(containerId, Session.class);
		session.changeAuditItemUploadStatus(containerId, itemUuid, status);
		db.put(containerId, session);
	}

	/**
	 * Return Audit item of given container
	 *
	 * @param context
	 * @param containerId
	 * @param itemUuid
	 * @return
	 */
	public AuditItem getAuditItem(Context context, String containerId, String itemUuid) {

		try {
			DB db = App.getDB(context);
			Session session = db.getObject(containerId, Session.class);
			if (session != null) {
				return session.getAuditItem(itemUuid);
			} else {
				return null;
			}

		} catch (SnappydbException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Get list container sessions based on param `prefix`
	 *
	 * @param context
	 * @param containerId
	 * @return
	 */
	@Background(serial = CACHE)
	public void getAuditItemsInBackground(Context context, String containerId) {
		try {
			DB db = App.getDB(context);
			Session session = db.getObject(containerId, Session.class);
			EventBus.getDefault().post(new AuditItemsGotEvent(session.getAuditItems()));
		} catch (SnappydbException e) {
			e.printStackTrace();
		}
	}

	//endregion
}
