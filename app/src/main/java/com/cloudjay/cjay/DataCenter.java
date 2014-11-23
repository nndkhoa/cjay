package com.cloudjay.cjay;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.cloudjay.cjay.activity.WizardActivity;
import com.cloudjay.cjay.activity.WizardActivity_;
import com.cloudjay.cjay.api.NetworkClient;
import com.cloudjay.cjay.event.image.RainyImagesDeletedEvent;
import com.cloudjay.cjay.event.image.RainyImagesGotEvent;
import com.cloudjay.cjay.event.isocode.IsoCodeGotEvent;
import com.cloudjay.cjay.event.isocode.IsoCodesGotEvent;
import com.cloudjay.cjay.event.isocode.IsoCodesGotToUpdateEvent;
import com.cloudjay.cjay.event.issue.AuditItemChangedEvent;
import com.cloudjay.cjay.event.issue.AuditItemGotEvent;
import com.cloudjay.cjay.event.issue.AuditItemsGotEvent;
import com.cloudjay.cjay.event.issue.IssueMergedEvent;
import com.cloudjay.cjay.event.operator.OperatorsGotEvent;
import com.cloudjay.cjay.event.session.ContainerGotEvent;
import com.cloudjay.cjay.event.session.ContainerSearchedEvent;
import com.cloudjay.cjay.event.upload.UploadStartedEvent;
import com.cloudjay.cjay.event.upload.UploadSucceededEvent;
import com.cloudjay.cjay.model.AuditImage;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.model.CJayObject;
import com.cloudjay.cjay.model.GateImage;
import com.cloudjay.cjay.model.IsoCode;
import com.cloudjay.cjay.model.LogItem;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.task.command.Command;
import com.cloudjay.cjay.task.command.CommandQueue;
import com.cloudjay.cjay.task.command.session.update.AddUploadingSessionCommand;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.PreferencesUtil;
import com.cloudjay.cjay.util.StringUtils;
import com.cloudjay.cjay.util.Utils;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;

@EBean(scope = EBean.Scope.Singleton)
public class DataCenter {

	@Bean
	CommandQueue queue;

	public void add(Command command) {
		queue.add(command);
	}

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

	public Session getSession(Context context, String containerId) {
		Session session = null;
		try {
			DB db = App.getDB(context);
			String key = containerId;
			session = db.getObject(key, Session.class);
		} catch (SnappydbException e) {
			Logger.w(e.getMessage());
		} finally {
			return session;
		}
	}

	public List<Session> getListSessions(Context context, String prefix) {

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
				addLog(context, newKey, prefix + " | Cannot retrieve this container", CJayConstant.PREFIX_LOG);
			}
		}

		return sessions;
	}

	public List<Session> getListSessions(Context context, String keyword, String prefix) {

		int len = prefix.length();
		DB db = null;
		String[] keysResult = new String[0];
		List<Session> sessions = new ArrayList<>();

		try {
			db = App.getDB(context);
			keysResult = db.findKeys(prefix + keyword);
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
				addLog(context, newKey, prefix + " | Cannot retrieve this container", CJayConstant.PREFIX_LOG);
			}
		}

		return sessions;
	}

	/**
	 * Get session with key without prefix
	 *
	 * @param context
	 * @param containerId
	 * @param prefix
	 * @return
	 */
	public Session getSession(Context context, String containerId, String prefix) {
		Session session = null;
		try {

			DB db = App.getDB(context);
			String key = containerId.substring(prefix.length());
			session = db.getObject(key, Session.class);

		} catch (SnappydbException e) {
			Logger.w(e.getMessage());
		} finally {
			return session;
		}
	}

	public boolean removeSession(Context context, String containerId, String prefix) {
		try {
			DB db = App.getDB(context);
			String key = prefix + containerId;
			db.del(key);
			return true;
		} catch (SnappydbException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Use it to add container session that received from server
	 *
	 * @param context
	 * @param session
	 */
	public void addOrUpdateSession(Context context, Session session) {

		DB db = null;
		String key = session.getContainerId();
		Session object = null;

		// Check if this container is existed in DB
		try {
			db = App.getDB(context);
			object = db.getObject(key, Session.class);

			Logger.Log("Container " + session.getContainerId() + " is existed in db");
			object.mergeSession(session);
			object.setUploadStatus(UploadStatus.COMPLETE);
			db.put(key, object);

		} catch (SnappydbException e) {

			// This container is not exist in db, so we add it to db
			try {
				// Only localize container if it is from server
				if (session.getId() != 0)
					object = session.changeToLocalFormat();

				db.put(key, object);
			} catch (SnappydbException e1) {
				e1.printStackTrace();
			}
		} finally {
			EventBus.getDefault().post(new UploadSucceededEvent(object, UploadType.SESSION));
		}
	}

	/**
	 * Use it to add existed container from Collection Normal to another collection. e.g. UPLOAD, WORKING
	 *
	 * @param context
	 * @param session
	 * @param prefix
	 */
	public void addSession(Context context, Session session, String prefix) {
		try {
			DB db = App.getDB(context);
			String key = prefix + session.getContainerId();
			db.put(key, session);

		} catch (SnappydbException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Use it to add existed container from Collection Normal to another collection. e.g. UPLOAD, WORKING
	 *
	 * @param context
	 * @param containerId
	 * @param prefix
	 */
	public void addSession(Context context, String containerId, String prefix) {
		try {

			DB db = App.getDB(context);
			Session session = db.getObject(containerId, Session.class);
			String key = prefix + containerId;
			db.put(key, session);

		} catch (SnappydbException e) {
			e.printStackTrace();
		}
	}

	public void prepareForUploading(Context context, Session session, Step step) {

		addLog(context, session.getContainerId(), step.name() + " | Add container vào Queue", CJayConstant.PREFIX_LOG);

		// Change local step
		switch (step) {
			case IMPORT:
				session.setLocalStep(Step.AUDIT.value);
				break;

			case AUDIT:
				session.setLocalStep(Step.REPAIR.value);
				break;

			case AVAILABLE:
				session.setLocalStep(Step.EXPORTED.value);
				break;

			case REPAIR:
			default:
				session.setLocalStep(Step.AVAILABLE.value);
				break;
		}

		//Change upload status
		session.setUploadStatus(UploadStatus.UPLOADING);
		DataCenter_.getInstance_(context).add(new AddUploadingSessionCommand(context, session));
		EventBus.getDefault().post(new UploadStartedEvent(session, UploadType.SESSION));
	}

	public Session uploadSession(Context context, Session session, Step uploadStep) throws SnappydbException {

		Logger.Log("Begin to upload container: " + session.getContainerId() + " | Step: " + uploadStep.name());
		addLog(context, session.getContainerId(), uploadStep.name() + " | Bắt đầu quá trình upload", CJayConstant.PREFIX_LOG);
		switch (uploadStep) {
			case IMPORT:
				return networkClient.uploadSession(context, session);

			case AUDIT:
				return networkClient.completeAudit(context, session);

			case REPAIR:
				return networkClient.completeRepairSession(context, session);

			case AVAILABLE:
				return networkClient.checkOutContainerSession(context, session);

			case HAND_CLEAN:
			default:
				return networkClient.setHandCleaningSession(context, session);
		}
	}

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
	public List<Operator> searchOperator(Context context, String keyword) {
		List<Operator> operators = new ArrayList<>();
		try {
			DB db = App.getDB(context);
			String[] keysResult = db.findKeys(CJayConstant.PREFIX_OPERATOR + keyword);
			for (String result : keysResult) {
				Operator operator = db.getObject(result, Operator.class);
				operators.add(operator);
			}
		} catch (SnappydbException e) {
			e.printStackTrace();
		}
		return operators;
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
	@Background(serial = CACHE, delay = 50)
	public void getListIsoCodes(Context context, String prefix) {
		try {
			DB db = App.getDB(context);
			String[] keyResults = db.findKeys(prefix);
			List<IsoCode> isoCodes = new ArrayList<>();

			for (String result : keyResults) {
				IsoCode isoCode = db.getObject(result, IsoCode.class);
				isoCodes.add(isoCode);
			}

			EventBus.getDefault().post(new IsoCodesGotEvent(isoCodes, prefix));

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
	@Background(serial = CACHE, delay = 50)
	public void getIsoCode(Context context, String prefix, String code) {
		try {
			DB db = App.getDB(context);
			String[] keyResults = db.findKeys(prefix + code);

			if (keyResults.length > 0) {
				IsoCode isoCode = db.getObject(keyResults[0], IsoCode.class);
				EventBus.getDefault().post(new IsoCodeGotEvent(isoCode, prefix));
			}
		} catch (SnappydbException e) {
			Logger.e(e.getMessage());
		}
	}

	@Background(serial = CACHE, delay = 50)
	public void getIsoCodesToUpdate(Context context, String strComponentCode,
	                                String strDamageCode, String strRepairCode) {
		try {

			IsoCode componentCode = null;
			IsoCode damageCode = null;
			IsoCode repairCode = null;

			DB db = App.getDB(context);
			String[] keyComponent = db.findKeys(CJayConstant.PREFIX_COMPONENT_CODE + strComponentCode);
			String[] keyDamage = db.findKeys(CJayConstant.PREFIX_DAMAGE_CODE + strDamageCode);
			String[] keyRepair = db.findKeys(CJayConstant.PREFIX_REPAIR_CODE + strRepairCode);

			if (keyComponent.length > 0) {
				componentCode = db.getObject(keyComponent[0], IsoCode.class);
			}
			if (keyDamage.length > 0) {
				damageCode = db.getObject(keyDamage[0], IsoCode.class);
			}
			if (keyRepair.length > 0) {
				repairCode = db.getObject(keyRepair[0], IsoCode.class);
			}

			EventBus.getDefault().post(new IsoCodesGotToUpdateEvent(componentCode, damageCode, repairCode));

		} catch (SnappydbException e) {
			Logger.e(e.getMessage());
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

				// merge result from server to local session
				session.mergeSession(result);
				db.put(key, session);
				return session;

			}
		} catch (SnappydbException e) {

			//Merge Session from server to local type

			Logger.w(e.getMessage());
			Logger.w("Received new container session from server: " + result.getContainerId());
			result.changeToLocalFormat();
//			addOrUpdateSession(result);
			return result;

		}
		return null;
	}

	/**
	 * @param context
	 * @param containerId
	 */
	@Background(serial = CACHE, delay = 50)
	public void changeLocalStepAndForceExport(Context context, String containerId) {

		DB db;
		try {

			db = App.getDB(context);
			Session session = db.getObject(containerId, Session.class);
			session.setLocalStep(Step.AVAILABLE.value);
			db.put(containerId, session);

			Intent intent = new Intent(context, WizardActivity_.class);
			intent.putExtra(WizardActivity.CONTAINER_ID_EXTRA, containerId);
			intent.putExtra(WizardActivity.STEP_EXTRA, Step.AVAILABLE.value);
			context.startActivity(intent);

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
			processListSession(context, sessions);
			newModifiedDay = PreferencesUtil.getPrefsValue(context, PreferencesUtil.PREF_MODIFIED_DATE);

		} while (lastModifiedDate.equals(newModifiedDay));

		PreferencesUtil.storePrefsValue(context, PreferencesUtil.PREF_MODIFIED_PAGE, "");

		if (refetchWithFistPageTime) {
			//Fetch again with modified day is first page request_time
			String firstPageTime = PreferencesUtil.getPrefsValue(context, PreferencesUtil.PREF_FIRST_PAGE_MODIFIED_DATE);
			fetchSession(context, firstPageTime, false);
		}
	}

	@Background(serial = CACHE, delay = 50)
	void processListSession(Context context, List<Session> sessions) {
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
						addOrUpdateSession(context, session);
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
	public void searchAsync(Context context, String keyword, boolean searchInImportFragment) {

		try {
			Logger.Log("Begin to search container from server");
			List<Session> sessions = networkClient.searchSessions(context, keyword);

			if (sessions.size() != 0) {
				DB db = App.getDB(context);

				for (Session session : sessions) {
					String key = session.getContainerId();
					session.changeToLocalFormat();
					db.put(key, session);
				}
			}

			EventBus.getDefault().post(new ContainerSearchedEvent(sessions, searchInImportFragment));
		} catch (SnappydbException e) {
			Logger.w(e.getMessage());
		} catch (RetrofitError error) {
			EventBus.getDefault().post(new ContainerSearchedEvent(true));
		}
	}

	public void updateImportSession(Context context, Session session) {
		DB db = null;
		String key = session.getContainerId();
		try {

			db = App.getDB(context);
			Session oldSession = db.getObject(session.getContainerId(), Session.class);
			oldSession.setOperatorId(session.getOperatorId());
			oldSession.setOperatorCode(session.getOperatorCode());
			oldSession.setPreStatus(session.getPreStatus());
			db.put(key, oldSession);

		} catch (SnappydbException e) {
			Logger.w(e.getMessage());
			try {
				db.put(key, session);
			} catch (SnappydbException e1) {
				e1.printStackTrace();
			}
		} finally {

			// Notify to Import Fragment
			getSession(context, key);
			EventBus.getDefault().post(new ContainerGotEvent(session, key));
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
	@Background(serial = CACHE, delay = 50)
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
	public void addGateImage(Context context, GateImage image, String containerId) {
		try {
			DB db = App.getDB(context);
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
	public void addAuditImage(Context context, AuditImage auditImage, String containerId) {

		try {
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

			EventBus.getDefault().post(new AuditItemChangedEvent(containerId));
		} catch (SnappydbException e) {
			e.printStackTrace();
		}
	}

	public void addOrUpdateAuditImage() {

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
	@Background(serial = CACHE, delay = 50)
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

	@Background(serial = CACHE, delay = 50)
	public void saveRainyImage(Context context, String uuid, String rainyImageUrl) {
		try {
			DB db = App.getDB(context);
			db.put(CJayConstant.PREFIX_RAINY_MODE_IMAGE + uuid, rainyImageUrl);
		} catch (SnappydbException e) {
			e.printStackTrace();
		}
	}

	@Background(serial = CACHE, delay = 50)
	public void getRainyImages(Context context) {
		try {
			DB db = App.getDB(context);

			String[] keys = db.findKeys(CJayConstant.PREFIX_RAINY_MODE_IMAGE);
			ArrayList<String> imageUrls = new ArrayList<>();

			for (int i = 0; i < keys.length; i++) {
				imageUrls.add(db.get(keys[i]));
			}

			EventBus.getDefault().post(new RainyImagesGotEvent(imageUrls));

		} catch (SnappydbException e) {
			e.printStackTrace();
		}
	}

	@Background(serial = CACHE, delay = 50)
	public void deleteRainyImage(Context context, ArrayList<String> imageUrls) {
		try {
			DB db = App.getDB(context);

			for (String imageUrl : imageUrls) {
				String uuid = Utils.getUuidFromImageName(Utils.getImageNameFromUrl(imageUrl));
				Logger.Log("uuid: " + uuid);
				if (db.exists(CJayConstant.PREFIX_RAINY_MODE_IMAGE + uuid)) {
					Logger.Log("del: " + CJayConstant.PREFIX_RAINY_MODE_IMAGE + uuid);
					db.del(CJayConstant.PREFIX_RAINY_MODE_IMAGE + uuid);
				}
			}

			EventBus.getDefault().post(new RainyImagesDeletedEvent());

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
	 * @param containerId
	 * @param sessionId
	 * @throws SnappydbException
	 */
	public void uploadAuditItem(Context context, String containerId, long sessionId, AuditItem auditItem) throws SnappydbException {

		AuditItem result = networkClient.postAuditItem(context, sessionId, auditItem);
//		saveUploadAuditItemSession(context, result, UploadType.AUDIT_ITEM, containerId);

	}

	/**
	 * Upload audit images of uploaded audit item to server
	 * 1. Get audit item based on uuid
	 * 2. Get id of audit item and list images to upload
	 * 3. Upload to server
	 * 4. Assign uuid to response audit item
	 * 5. Replace result with local audit item in container session
	 *
	 * @param context
	 * @param containerId
	 * @param auditItem
	 */
	public void uploadAddedAuditImage(Context context, String containerId, AuditItem auditItem) {
		AuditItem result = networkClient.addAuditImage(context, auditItem);
//		saveUploadAuditItemSession(context, result, UploadType.AUDIT_ITEM, containerId);
	}

//	@Background(serial = CACHE, delay = 50)
//	public void saveUploadAuditItemSession(Context context, AuditItem result, UploadType type, String containerId) {
//		DB db = null;
//		String key = containerId;
//		Session object = null;
//		try {
//			db = App.getDB(context);
//
//			object = db.getObject(key, Session.class);
//			object.updateAuditItem(result);
//
////			saveSession(context, object, type);
//
//		} catch (SnappydbException e) {
//			Logger.wtf(e.getMessage());
//		} finally {
//			EventBus.getDefault().post(new UploadSucceededEvent(object, UploadType.SESSION));
//		}
//	}

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
			String[] keysResult = db.findKeys(searchKey);
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
	public void addLog(Context context, String title, String message, String typeLog) {
		String currentTime = StringUtils.getCurrentTimestamp(CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE);

		LogItem log = new LogItem();
		log.setContainerId(title);
		log.setMessage(message);
		log.setTime(currentTime);

		addLog(context, log, typeLog);
	}

	public void addLog(Context context, LogItem log, String typeLog) {
		try {

			DB db = App.getDB(context);
			String key = typeLog + log.getContainerId() + log.getTime();
			db.put(key, log);

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
	@Background(serial = CACHE, delay = 50)
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

	public boolean updateAuditItem(Context context, String containerId, AuditItem auditItem) {
		try {
			// find session
			DB db = App.getDB(context);
			Session session = db.getObject(containerId, Session.class);
			session.updateAuditItem(auditItem);
			db.put(containerId, session);
			return true;
		} catch (SnappydbException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Background(serial = CACHE, delay = 50)
	public void updateAuditItemInBackground(Context context, String containerId, AuditItem auditItem) {
		Logger.Log("updateAuditItemInBackground");
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
	@Background(serial = CACHE, delay = 50)
	public void setWaterWashType(Context context, final AuditItem auditItem, String containerId) {

		try {
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
				auditItem.setQuantity(1);

				list.add(auditItem);
			}

			session.setAuditItems(list);
			db.put(containerId, session);

			EventBus.getDefault().post(new AuditItemChangedEvent(containerId));
		} catch (SnappydbException e) {
			e.printStackTrace();
		}
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
	@Background(serial = CACHE, delay = 50)
	public void getAuditItemsInBackground(Context context, String containerId) {
		try {
			DB db = App.getDB(context);
			Session session = db.getObject(containerId, Session.class);
			EventBus.getDefault().post(new AuditItemsGotEvent(session.getAuditItems()));
		} catch (SnappydbException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get audit item in background and post event back
	 *
	 * @param context
	 * @param containerId
	 * @param itemUuid
	 */
	@Background(serial = CACHE, delay = 50)
	public void getAuditItemInBackground(Context context, String containerId, String itemUuid) {
		Logger.Log("getAuditItemInBackground");
		try {
			DB db = App.getDB(context);
			Session session = db.getObject(containerId, Session.class);
			if (session != null) {
				AuditItem auditItem = session.getAuditItem(itemUuid);
				EventBus.getDefault().post(new AuditItemGotEvent(auditItem));
			}

		} catch (SnappydbException e) {
			e.printStackTrace();
		}
	}

	//endregion

	/**
	 * Add Queue to line
	 * <p/>
	 * 1. Find Session queue
	 * - if found, get max session priority => add new session queue with priority = current max priority +1
	 * - if can't find, get max queue priority => add new container priority then add new session queue
	 *
	 * @param containerId
	 * @param object
	 * @throws SnappydbException
	 */
	public void addQueue(String containerId, CJayObject object) throws SnappydbException {
		DB db = App.getDB(context);
		//Find if this container is in line
		//If found => get max priority number and add object with key have priority is
		// current max priority + 1
		// If can't find, => search for Queue Priority of this all container,
		String keytoFind = CJayConstant.SESSION_PRIORITY + containerId + ":";
		String[] sessionPriority = db.findKeys(keytoFind);
		//Progress if found
		if (sessionPriority.length != 0) {
			int maxPriority = getPriority(sessionPriority, keytoFind, true);
			int priorityToAdd = maxPriority + 1;

			CJayObject beforeObject = db.getObject(keytoFind + maxPriority, CJayObject.class);

			object.setSessionPriority(priorityToAdd);
			object.setQueuePriority(beforeObject.getQueuePriority());

			db.put(keytoFind + priorityToAdd, object);
		} else {
			//Search for current max queue priority
			String[] queuePriority = db.findKeys(CJayConstant.QUEUE_PRIORITY);
			if (queuePriority.length != 0) {
				int maxPriority = getPriority(queuePriority, CJayConstant.QUEUE_PRIORITY, true);
				int priorityToAdd = maxPriority + 1;

				object.setQueuePriority(priorityToAdd);
				object.setSessionPriority(1);

				db.put(keytoFind + "1", object);
				db.put(CJayConstant.QUEUE_PRIORITY + priorityToAdd, containerId);
			} else {

				object.setQueuePriority(1);
				object.setSessionPriority(1);

				db.put(keytoFind + "1", object);
				db.put(CJayConstant.QUEUE_PRIORITY + 1, containerId);
			}
		}
	}

	/**
	 * Get next Queue by old CjayObject
	 * 1. Search for next session priority,
	 * - if exit => return Object
	 * - if isn't exit => search for next queue priority
	 * - if exit => return first object (object with session priority = 1 )
	 * - if isn't exit => return null
	 *
	 * @param containerId
	 * @param oldObject
	 * @return
	 * @throws SnappydbException
	 */
	public CJayObject getNextQueue(String containerId, CJayObject oldObject) throws SnappydbException {
		DB db = App.getDB(context);

		int nextSessionPriority = oldObject.getSessionPriority() + 1;
		int nexQueuePriority = oldObject.getQueuePriority() + 1;

		String keytoFind = CJayConstant.SESSION_PRIORITY + containerId + ":" + nextSessionPriority;
		String[] sessionPriority = db.findKeys(keytoFind);
		if (sessionPriority.length != 0) {
			CJayObject nextJob = db.getObject(keytoFind, CJayObject.class);
			return nextJob;
		} else {
			String keyQueryNextQueue = CJayConstant.QUEUE_PRIORITY + nexQueuePriority;
			String[] queuePriority = db.findKeys(keyQueryNextQueue);
			if (queuePriority.length != 0) {
				String nextContainer = db.get(keyQueryNextQueue);
				CJayObject nextJob = db.getObject(CJayConstant.SESSION_PRIORITY + ":" + 1, CJayObject.class);
				return nextJob;
			} else {
				return null;
			}
		}
	}

	/**
	 * Remove done CjObject after done job
	 * 1. Remove object in db with key is current object session priority
	 * 2 Find for next session priority
	 * => if didn't find , remove queue priority with key is current object queue priority
	 *
	 * @param containerId
	 * @param object
	 * @throws SnappydbException
	 */
	public void removeDoneQueue(String containerId, CJayObject object) throws SnappydbException {
		DB db = App.getDB(context);

		int sessionPriority = object.getSessionPriority();
		int queuePriority = object.getQueuePriority();

		String keytoDelete = CJayConstant.SESSION_PRIORITY + containerId + ":" + sessionPriority;
		db.del(keytoDelete);

		int nextSessionPririty = object.getSessionPriority() + 1;
		String keySearchNextSessionPriority = CJayConstant.SESSION_PRIORITY + containerId + ":" + nextSessionPririty;
		String[] nextSessionPrioritys = db.findKeys(keySearchNextSessionPriority);
		if (nextSessionPrioritys.length == 0) {
			String keyDeleteQueuePriority = CJayConstant.QUEUE_PRIORITY + queuePriority;
			db.del(keyDeleteQueuePriority);
		}


	}

	/**
	 * Get current priority base on String[] reuslt search and key to search
	 * False for min priority
	 * True for max priority
	 *
	 * @param priorityString
	 * @param keyToGetInt
	 * @return
	 */
	private int getPriority(String[] priorityString, String keyToGetInt, boolean max) {
		ArrayList<Integer> priorityList = new ArrayList<Integer>();
		for (String key : priorityString) {
			int lenghtString = (keyToGetInt).length();
			String priority = key.substring(lenghtString);
			int priorityNumer = Integer.valueOf(priority);
			priorityList.add(priorityNumer);
		}
		int maxPriority = Collections.max(priorityList);
		int minPriority = Collections.min(priorityList);
		if (max) {
			return maxPriority;
		} else {
			return minPriority;
		}
	}
}
