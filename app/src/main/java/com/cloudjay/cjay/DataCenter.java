package com.cloudjay.cjay;

import android.content.Context;

import com.cloudjay.cjay.api.NetworkClient;
import com.cloudjay.cjay.event.BeginSearchOnServerEvent;
import com.cloudjay.cjay.event.ContainerSearchedEvent;
import com.cloudjay.cjay.event.GateImagesGotEvent;
import com.cloudjay.cjay.event.OperatorsGotEvent;
import com.cloudjay.cjay.event.UploadedEvent;
import com.cloudjay.cjay.event.WorkingSessionCreatedEvent;
import com.cloudjay.cjay.model.AuditImage;
import com.cloudjay.cjay.model.GateImage;
import com.cloudjay.cjay.model.IsoCode;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.PreferencesUtil;
import com.cloudjay.cjay.util.exception.NullCredentialException;
import com.snappydb.DB;
import com.snappydb.SnappydbException;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.Trace;

import java.util.ArrayList;
import java.util.List;

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
		User user = App.getDB(context).getObject(CJayConstant.USER_KEY, User.class);
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
		db.put(CJayConstant.USER_KEY, user);
		db.close();

		return user;
	}
	//endregion

	//region OPERATOR

	/**
	 * Fetch and save all operators to database
	 *
	 * @param context
	 * @throws SnappydbException
	 */
	public void fetchOperators(Context context) throws SnappydbException {
		DB db = App.getDB(context);
		List<Operator> operators = networkClient.getOperators(context, null);
		for (Operator operator : operators) {
			db.put(CJayConstant.OPERATOR_KEY + operator.getOperatorCode(), operator);
		}
		db.close();
	}


	/**
	 * Get all operators from client database
	 */
	@Background(serial = CACHE)
	public void getOperators() {

		try {
			// Search on local db
			List<Operator> operators = new ArrayList<Operator>();
			String[] keysResult = App.getDB(context).findKeys(CJayConstant.OPERATOR_KEY);
			for (String key : keysResult) {
				Operator foundOperator = App.getDB(context).getObject(key, Operator.class);
				operators.add(foundOperator);
			}
			EventBus.getDefault().post(new OperatorsGotEvent(operators));
		} catch (SnappydbException e) {
			e.printStackTrace();
		}
	}
	//endregion

	//region ISO CODE

	/**
	 * Fetch and save all iso codes to database
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
			String key = CJayConstant.DAMAGE_CODE_KEY + code.getCode();
			db.put(key, code);
		}
		db.close();
	}

	public void fetchRepairCodes(Context context) throws SnappydbException {
		DB db = App.getDB(context);
		List<IsoCode> repairCodes = networkClient.getRepairCodes(context, null);
		for (IsoCode code : repairCodes) {
			String key = CJayConstant.REPAIR_CODE_KEY + code.getCode();
			db.put(key, code);
		}
		db.close();
	}

	public void fetchComponentCodes(Context context) throws SnappydbException {
		DB db = App.getDB(context);
		List<IsoCode> componentCodes = networkClient.getComponentCodes(context, null);
		for (IsoCode code : componentCodes) {
			String key = CJayConstant.COMPONENT_CODE_KEY + code.getCode();
			db.put(key, code);
		}
		db.close();
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
		DB db = App.getDB(context);
		List<Session> sessions = networkClient.getAllSessions(context, lastModifiedDate);
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
	@Trace
	@Background(serial = CACHE)
	public void search(Context context, String keyword) {
		String[] keysResult;
		try {

			// try to search from client database
			keysResult = App.getDB(context).findKeys(keyword);
			List<Session> sessions = new ArrayList<Session>();
			for (String result : keysResult) {
				Session session = App.getDB(context).getObject(result, Session.class);
				sessions.add(session);
			}

			// Check if local search has results
			if (sessions.size() != 0) {
				EventBus.getDefault().post(new ContainerSearchedEvent(sessions));

			} else {

				// If there was not result in local, send search request to server
				//  --> alert to user about that no results was found in local
				EventBus.getDefault().post(new BeginSearchOnServerEvent(context.getResources().getString(R.string.search_on_server)));
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
				for (Session session : sessions) {
					String key = session.getContainerId();
					App.getDB(context).put(key, session);
				}
			}

			EventBus.getDefault().post(new ContainerSearchedEvent(sessions));
		} catch (SnappydbException e) {
			e.printStackTrace();
		}
	}

	@Background(serial = CACHE)
	public void addSession(String containerId, String operatorCode, long operatorId, String checkInTime, long preStatus) {
		Session session = new Session();
		session.setId(0);
		session.setContainerId(containerId);
		session.setOperatorId(operatorId);
		session.setOperatorCode(operatorCode);
		session.setCheckInTime(checkInTime);
		session.setPreStatus(preStatus);
		try {
			App.getDB(context).put(containerId, session);
			addWorkingId(context, containerId);
			Logger.Log("insert session successfully");
		} catch (SnappydbException e) {
			e.printStackTrace();
		}
	}

	public void addWorkingId(Context context, String containerId) {

		//Get working session list, if can't create one
		try {
			Session sessionWorking = App.getDB(context).getObject(containerId, Session.class);
			sessionWorking.setProcessing(true);
			App.getDB(context).put(CJayConstant.WORKING_DB + containerId, sessionWorking);
			EventBus.getDefault().post(new WorkingSessionCreatedEvent(sessionWorking));

		} catch (SnappydbException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Add gate image for both normal session and working session
	 *
	 * @param type
	 * @param url
	 * @param containerId
	 * @throws SnappydbException
	 */
	public void addGateImage(long type, String url, String containerId, String imageName) throws SnappydbException {
		addGateImageToNormalSession(type, url, containerId, imageName);
		addGateImageToWorkingSession(type, url, containerId, imageName);
	}

	private void addGateImageToWorkingSession(long type, String url, String containerId, String imageName) throws SnappydbException {
		containerId = CJayConstant.WORKING_DB + containerId;
		Session session = App.getDB(context).getObject(containerId, Session.class);
		GateImage gateImage = new GateImage();
		gateImage.setId(0);
		gateImage.setType(type);
		gateImage.setUrl(url);
		gateImage.setName(imageName);

		List<GateImage> gateImages = session.getGateImages();
		if (gateImages == null) {
			gateImages = new ArrayList<GateImage>();
		}
		gateImages.add(gateImage);
		session.setGateImages(gateImages);

		//Add update session with image to normal session in db
		App.getDB(context).put(containerId, session);
	}

	private void addGateImageToNormalSession(long type, String url, String containerId, String imageName) throws SnappydbException {

		Session session = App.getDB(context).getObject(containerId, Session.class);
		GateImage gateImage = new GateImage();
		gateImage.setId(0);
		gateImage.setType(type);
		gateImage.setUrl(url);
		gateImage.setName(imageName);

		List<GateImage> gateImages = session.getGateImages();
		if (gateImages == null) {
			gateImages = new ArrayList<GateImage>();
		}
		gateImages.add(gateImage);
		session.setGateImages(gateImages);

		//Add update session with image to normal session in db
		App.getDB(context).put(containerId, session);
	}

	public void getGateImages(long type, String containerId) throws SnappydbException {
		Logger.Log("type = " + type + ", containerId = " + containerId);
		Session session = App.getDB(context).getObject(containerId, Session.class);
		List<GateImage> gateImagesFiltered = new ArrayList<GateImage>();

		List<GateImage> gateImages = session.getGateImages();
		for (GateImage g : gateImages) {
			if (g.getType() == type) {
				gateImagesFiltered.add(g);
			}
		}
		EventBus.getDefault().post(new GateImagesGotEvent(gateImagesFiltered));
	}

	public void searchOperator(String keyword) throws SnappydbException {
		String[] keysresult = App.getDB(context).findKeys(CJayConstant.OPERATOR_KEY + keyword);
		List<Operator> operators = new ArrayList<Operator>();
		for (String result : keysresult) {
			operators.add(App.getDB(context).getObject(result, Operator.class));
		}
		EventBus.getDefault().post(new OperatorsGotEvent(operators));
	}

	@Background(serial = CACHE)
	public void getSessionByContainerId(String containerId) {

		String[] keysResult;
		try {
			keysResult = App.getDB(context).findKeys(containerId);
			List<Session> sessions = new ArrayList<Session>();
			for (String result : keysResult) {
				sessions.add(App.getDB(context).getObject(result, Session.class));
			}
			EventBus.getDefault().post(new ContainerSearchedEvent(sessions));
		} catch (SnappydbException e) {
			e.printStackTrace();
		}

	}

	@Background(serial = CACHE)
	public void getAllGateImagesByContainerId(String containerId) {
		try {
			Session session = App.getDB(context).getObject(containerId, Session.class);
			List<GateImage> gateImages = session.getGateImages();
			Logger.Log("gate images count in dataCenter: " + gateImages.size());
			EventBus.getDefault().post(new GateImagesGotEvent(gateImages));
		} catch (SnappydbException e) {
			e.printStackTrace();
		}
	}

	public void addUploadingSession(String containerId) throws SnappydbException {
		Session uploadingSession = App.getDB(context).getObject(containerId, Session.class);
		App.getDB(context).put(CJayConstant.UPLOADING_DB + containerId, uploadingSession);
	}

	//TODO: include upload Audit Image
	public void uploadImage(Context context, String uri, String imageName, String containerId) throws SnappydbException {

		//Call network client to upload image
		networkClient.uploadImage(uri, imageName);

		//Change status image in db
		Session uploadingSession = App.getDB(context).getObject(CJayConstant.UPLOADING_DB + containerId, Session.class);
		for (GateImage gateImage : uploadingSession.getGateImages()) {
			if (gateImage.getName().equals(imageName)) {
				gateImage.setUploaded(true);
			}
		}
		App.getDB(context).put(CJayConstant.UPLOADING_DB + containerId, uploadingSession);
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
		networkClient.uploadContainerSession(context, session);
		String key = CJayConstant.UPLOADING_DB + session.getContainerId();
		Session sessionUploaded = App.getDB(context).getObject(key, Session.class);
		sessionUploaded.setUploaded(true);
		App.getDB(context).put(CJayConstant.UPLOADING_DB + session + session.getContainerId(), sessionUploaded);
	}

	public void addAuditImages(String containerId, long type, String url) throws SnappydbException {
		Session session = App.getDB(context).getObject(containerId, Session.class);
		AuditImage auditImage = new AuditImage();
		auditImage.setId(0);
		auditImage.setType(type);
		auditImage.setUrl(url);
		auditImage.setUploaded(false);

		List<AuditImage> auditImages = session.getAuditImages();
		if (auditImages == null) {
			auditImages = new ArrayList<AuditImage>();
		}
		auditImages.add(auditImage);
		session.setAuditImages(auditImages);

		App.getDB(context).put(containerId, session);

		Logger.Log("insert audit image successfully");
		App.closeDB();
	}
}