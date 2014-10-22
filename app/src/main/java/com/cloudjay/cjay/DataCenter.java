package com.cloudjay.cjay;

import android.content.Context;

import com.cloudjay.cjay.api.NetworkClient;
import com.cloudjay.cjay.event.SearchAsyncStartedEvent;
import com.cloudjay.cjay.event.ContainerSearchedEvent;
import com.cloudjay.cjay.event.GateImagesGotEvent;
import com.cloudjay.cjay.event.OperatorsGotEvent;
import com.cloudjay.cjay.event.upload.UploadedEvent;
import com.cloudjay.cjay.event.WorkingSessionCreatedEvent;
import com.cloudjay.cjay.model.AuditImage;
import com.cloudjay.cjay.model.AuditItem;
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
        User user = App.getDB(context).getObject(CJayConstant.PREFIX_USER, User.class);
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
            List<Session> sessions = new ArrayList<Session>();

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

    /**
     * Get List audit item for normal session
     * @param context
     * @param containerId
     * @return
     */
    public List<AuditItem> getListAuditItems(Context context, String containerId) {
        try {
            DB db = App.getDB(context);
            Session session = db.getObject(containerId, Session.class);
            List<AuditItem> auditItems = new ArrayList<AuditItem>();
            for (AuditItem currentAuditItem : session.getAuditItems()) {
                if (currentAuditItem.getId() != 0) {
                    auditItems.add(currentAuditItem);
                }
            }
            db.close();
            return auditItems;
        } catch (SnappydbException e) {
            e.printStackTrace();
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

            // Add working session
            String workingKey = CJayConstant.PREFIX_WORKING + session.getContainerId();
            session.setProcessing(true);
            db.put(workingKey, session);

            // Close db
            db.close();

            // Notify to Working Fragment
            EventBus.getDefault().post(new WorkingSessionCreatedEvent(session));
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    public void addGateImage(GateImage image) throws SnappydbException {

    }

    public void addAuditImage(AuditImage image) throws SnappydbException {

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

        containerId = CJayConstant.PREFIX_WORKING + containerId;
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
        App.getDB(context).put(CJayConstant.PREFIX_UPLOADING + containerId, uploadingSession);
    }

    // TODO: include upload Audit Image
    public void uploadImage(Context context, String uri, String imageName, String containerId) throws SnappydbException {

        //Call network client to upload image
        networkClient.uploadImage(uri, imageName);

        //Change status image in db
        Session uploadingSession = App.getDB(context).getObject(CJayConstant.PREFIX_UPLOADING + containerId, Session.class);
        for (GateImage gateImage : uploadingSession.getGateImages()) {
            if (gateImage.getName().equals(imageName)) {
                gateImage.setUploaded(true);
            }
        }
        App.getDB(context).put(CJayConstant.PREFIX_UPLOADING + containerId, uploadingSession);
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
        String key = CJayConstant.PREFIX_UPLOADING + session.getContainerId();
        Session sessionUploaded = App.getDB(context).getObject(key, Session.class);
        sessionUploaded.setUploaded(true);
        App.getDB(context).put(CJayConstant.PREFIX_UPLOADING + session + session.getContainerId(), sessionUploaded);
    }

    public void addAuditImages(String containerId, AuditImage auditImage) throws SnappydbException {
        Session session = App.getDB(context).getObject(containerId, Session.class);

        // Generate random one UUID to save auditItem
        String uuid = UUID.randomUUID().toString();

        // Create new audit item to save
        AuditItem auditItem = new AuditItem();
        auditItem.setId(0);
        auditItem.setAuditItemUUID(uuid);

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

		App.getDB(context).put(containerId, session);

		Logger.Log("insert audit image successfully");
		App.closeDB();
    }

    public List<AuditItem> getAuditItems(String containerId) {
        Session session = null;
        try {
            session = App.getDB(context).getObject(containerId, Session.class);
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
        List<AuditItem> auditItems = session.getAuditItems();

        return auditItems;
    }
}