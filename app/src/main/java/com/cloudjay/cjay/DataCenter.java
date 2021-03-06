package com.cloudjay.cjay;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.cloudjay.cjay.activity.WizardActivity;
import com.cloudjay.cjay.activity.WizardActivity_;
import com.cloudjay.cjay.api.NetworkClient;
import com.cloudjay.cjay.event.image.RainyImagesDeletedEvent;
import com.cloudjay.cjay.event.image.RainyImagesGotEvent;
import com.cloudjay.cjay.event.issue.AuditItemChangedEvent;
import com.cloudjay.cjay.event.issue.IssueMergedEvent;
import com.cloudjay.cjay.event.session.ContainerGotEvent;
import com.cloudjay.cjay.event.session.ContainerSearchedEvent;
import com.cloudjay.cjay.event.session.SearchAsyncStartedEvent;
import com.cloudjay.cjay.event.upload.UploadStartedEvent;
import com.cloudjay.cjay.model.AuditImage;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.model.GateImage;
import com.cloudjay.cjay.model.IsoCode;
import com.cloudjay.cjay.model.LogItem;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.model.UploadObject;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.task.command.Command;
import com.cloudjay.cjay.task.command.CommandQueue;
import com.cloudjay.cjay.task.command.UploadQueue;
import com.cloudjay.cjay.task.command.session.update.AddListSessionsCommand;
import com.cloudjay.cjay.task.command.session.update.SaveSessionCommand;
import com.cloudjay.cjay.task.job.UploadAuditItemJob;
import com.cloudjay.cjay.task.job.UploadImageJob;
import com.cloudjay.cjay.task.job.UploadSessionJob;
import com.cloudjay.cjay.task.service.UploadIntentService_;
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
import com.esotericsoftware.kryo.KryoException;
import com.path.android.jobqueue.JobManager;
import com.snappydb.DB;
import com.snappydb.KeyIterator;
import com.snappydb.SnappydbException;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;
import retrofit.client.Response;

@EBean(scope = EBean.Scope.Singleton)
public class DataCenter {

    public void add(Command command) {
        queue.add(command);
    }

    public void addUploadItem(UploadObject object) {
        uploadQueue.add(object);
    }

    // region DECLARE

    // Inject the command queue
    @Bean
    UploadQueue uploadQueue;

    @Bean
    CommandQueue queue;

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
    // TODO: add to command
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
            e.printStackTrace();
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
            e.printStackTrace();
        }
        return null;
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
            IsoCode isoCode = null;
            if (keyResults.length > 0) {
                isoCode = db.getObject(keyResults[0], IsoCode.class);
            }
            return isoCode;
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateAuditItem(Context context, String containerId, AuditItem auditItem,
                                   String strComponentCode, String strDamageCode,
                                   String strRepairCode) {
        try {

            IsoCode componentCode = null;
            IsoCode damageCode = null;
            IsoCode repairCode = null;

            DB db = App.getDB(context);
            Session session = db.getObject(containerId, Session.class);
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

            auditItem.setSession(session.getId());
            auditItem.setComponentCodeId(componentCode.getId());
            auditItem.setComponentCode(componentCode.getCode());
            auditItem.setComponentName(componentCode.getFullName());

            auditItem.setDamageCodeId(damageCode.getId());
            auditItem.setDamageCode(damageCode.getCode());

            auditItem.setRepairCodeId(repairCode.getId());
            auditItem.setRepairCode(repairCode.getCode());

            updateAuditItem(context, containerId, auditItem);

            return true;
        } catch (SnappydbException e) {
            e.printStackTrace();
        }

        return false;
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
//	@Background(serial = NETWORK)
    public void getSessionAsyncById(Context context, long id, int type) {

        Session result = networkClient.getSessionById(id);
        add(new SaveSessionCommand(context, result, type));

//		try {
//
//		} catch (RetrofitError e) {
//			e.printStackTrace();
//		}
    }

    /**
     * @param context
     * @param containerId
     */
    public void forceExport(Context context, String containerId) {
        try {
            DB db = App.getDB(context);
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
     * <<<<<<< HEAD
     * Change container session local step in background
     *
     * @param context
     * @param containerId
     * @param step
     */
    @Background(serial = CACHE, delay = 50)
    public void changeSessionLocalStepInBackground(Context context, String containerId, Step step) {

        DB db;
        try {

            db = App.getDB(context);
            Session session = db.getObject(containerId, Session.class);
            session.setLocalStep(step.value);
            db.put(containerId, session);

            EventBus.getDefault().post(new ContainerGotEvent(session, containerId));

        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    /**
     * =======
     * >>>>>>> query
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
     * @param refetchWithFirstPageTime
     */
    public void fetchSession(Context context, String lastModifiedDate, boolean refetchWithFirstPageTime) throws RetrofitError {

        String newModifiedDay;
        do {
            int nextPage;
            String currentPage = PreferencesUtil.getPrefsValue(context, PreferencesUtil.PREF_MODIFIED_PAGE);

            if (currentPage.isEmpty()) {
                nextPage = 1;
            } else {
                nextPage = Integer.valueOf(currentPage) + 1;
            }

            Logger.Log("At page: " + nextPage);

            List<Session> sessions = networkClient.getSessionByPage(context, nextPage, lastModifiedDate);
//			processListSession(context, sessions);

            add(new AddListSessionsCommand(context, sessions));
            newModifiedDay = PreferencesUtil.getPrefsValue(context, PreferencesUtil.PREF_MODIFIED_DATE);

        } while (lastModifiedDate.equals(newModifiedDay));

        PreferencesUtil.storePrefsValue(context, PreferencesUtil.PREF_MODIFIED_PAGE, "");

        if (refetchWithFirstPageTime) {

            //Fetch again with modified day is first page request_time
            String firstPageTime = PreferencesUtil.getPrefsValue(context, PreferencesUtil.PREF_FIRST_PAGE_MODIFIED_DATE);
            fetchSession(context, firstPageTime, false);

        }
    }


    public void processListSession(Context context, List<Session> sessions) {
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
                        session.localize();
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
     * <<<<<<< HEAD
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
    @Background(serial = CACHE, delay = 30)
    public void search(Context context, String keyword, boolean searchInImportFragment) {

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

                EventBus.getDefault().post(new ContainerSearchedEvent(sessions, searchInImportFragment));
            } else {

                // If there was not result in local, send search request to server
                //  --> alert to user about that no results was found in local
                EventBus.getDefault().post(new SearchAsyncStartedEvent(context.getResources().getString(R.string.search_on_server)));
                searchAsync(context, keyword, searchInImportFragment);
            }
        } catch (SnappydbException e) {
            Logger.e(e.getMessage());
        }
    }

    /**
     * =======
     * >>>>>>> query
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
                    session.localize();
                    db.put(key, session);
                }
            }

            EventBus.getDefault().post(new ContainerSearchedEvent(sessions, searchInImportFragment));
        } catch (SnappydbException e) {
            e.printStackTrace();
        } catch (RetrofitError error) {
            EventBus.getDefault().post(new ContainerSearchedEvent(true));
        }
    }


    public Session getSession(Context context, String containerId) {
        Session session = null;

        if (TextUtils.isEmpty(containerId)) {
            Logger.Log("containerId is null");
            return null;
        }

        try {
            DB db = App.getDB(context);
            String key = containerId;
            session = db.getObject(key, Session.class);
        } catch (SnappydbException e) {
            e.printStackTrace();
        } catch (KryoException e) {
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
        } finally {
            return session;
        }
    }

    public boolean removeSession(Context context, String containerId, String prefix) {
        try {
            DB db = App.getDB(context);
            String key = prefix + containerId;
            Logger.Log("key: " + key);
            db.del(key);
            return true;
        } catch (SnappydbException e) {
            Logger.Log("failed");
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
    public boolean addOrUpdateSession(Context context, Session session) {

        DB db = null;
        String key = session.getContainerId();
        Session object;

        // Check if this container is existed in DB
        try {
            db = App.getDB(context);
            object = db.getObject(key, Session.class);
            object.mergeSession(session);
            object.setUploadStatus(UploadStatus.COMPLETE);
            Logger.Log("Local step: " + object.getLocalStep());
            db.put(key, object);

        } catch (SnappydbException e) {

            // This container is not exist in db, so we add it to db
            try {
                // Only localize container if it is from server
                if (session.getId() != 0) {
                    object = session.localize();
                    db.put(key, object);
                } else {
                    db.put(key, session);
                }
            } catch (SnappydbException e1) {
                Logger.w(e1.getMessage());
                return false;
            }
        }

        return true;
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
            e.printStackTrace();
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

            auditItem.setModifiedAt(StringUtils.getMinDate());

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

            EventBus.getDefault().post(new AuditItemChangedEvent(containerId));
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
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
            }
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    public void saveRainyImage(Context context, String uuid, String rainyImageUrl) {
        try {
            DB db = App.getDB(context);
            db.put(CJayConstant.PREFIX_RAINY_MODE_IMAGE + uuid, rainyImageUrl);
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> getRainyImages(Context context) {
        ArrayList<String> imageUrls = new ArrayList<>();
        try {
            DB db = App.getDB(context);

            String[] keys = db.findKeys(CJayConstant.PREFIX_RAINY_MODE_IMAGE);


            for (int i = 0; i < keys.length; i++) {
                imageUrls.add(db.get(keys[i]));
            }


            EventBus.getDefault().post(new RainyImagesGotEvent(imageUrls));

        } catch (SnappydbException e) {
            e.printStackTrace();
        }
        return imageUrls;
    }

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
    public Response uploadImage(String uri, String imageName) throws SnappydbException {
        Response response = networkClient.uploadImage(uri, imageName);
        return response;
    }

    /**
     * Upload audit item to server.
     * 1. Get audit item based on uuid
     * 2. Upload to server
     * 3. Assign uuid to response audit item
     * 4. Replace result with local audit item in container session
     *
     * @param context
     * @param sessionId
     * @throws SnappydbException
     */

    public AuditItem uploadAuditItem(Context context, long sessionId, AuditItem auditItem) throws SnappydbException {
        return networkClient.postAuditItem(context, sessionId, auditItem);
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
     * @param auditItem
     */
    public AuditItem uploadAddedAuditImage(Context context, AuditItem auditItem) {
        return networkClient.addAuditImage(context, auditItem);
    }

    /**
     * Upload container to server.
     *
     * @param context
     * @param session
     * @param uploadStep
     * @return
     * @throws SnappydbException
     */
    public Session uploadSession(Context context, Session session, Step uploadStep) throws SnappydbException, RetrofitError {

        Logger.Log("Begin to upload container: " + session.getContainerId() + " | Step: " + uploadStep.name());
        addLog(context, session.getContainerId(), uploadStep.name() + " | Bắt đầu quá trình upload", CJayConstant.PREFIX_LOG);

        EventBus.getDefault().post(new UploadStartedEvent(session, UploadType.SESSION));
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
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
    public boolean removeAuditItem(Context context, String containerId, String auditItemUUID) {
        try {
            // find session
            DB db = App.getDB(context);
            Session session = db.getObject(containerId, Session.class);

            // Remove audit item from session
            if (session != null) {
                session.removeAuditItem(auditItemUUID);
                db.put(containerId, session);
                return true;
            }
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateAuditItem(Context context, String containerId, AuditItem auditItem) {
        try {

            Logger.Log("upload status: " + auditItem.getUploadStatus());

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
    public boolean setWaterWashType(Context context, final AuditItem auditItem, String containerId) {

        try {
            // Find session
            DB db = App.getDB(context);
            Session session = db.getObject(containerId, Session.class);

            // --> Nếu nhiều image cùng được chọn là lỗi vệ sinh, thì gộp tất cả vào một lỗi
            boolean isExisted = false;

            List<AuditItem> list = session.getAuditItems();
            for (int i = 0; i < list.size(); i++) {

                // If water wash type item is existed, add image to it
                if (list.get(i).isWashTypeItem()) {
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
                auditItem.setModifiedAt(StringUtils.getMinDate());

                list.add(auditItem);
            }

            session.setAuditItems(list);
            db.put(containerId, session);

            return true;
        } catch (SnappydbException e) {
            e.printStackTrace();
        }

        return false;
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
    public void getAuditItemAsyncById(Context context, long id) throws SnappydbException {

        try {
            AuditItem auditItem = networkClient.getAuditItemById(id);

            if (auditItem != null) {
                long sessionId = auditItem.getSession();
                getSessionAsyncById(context, sessionId, 1);
            }
        } catch (RetrofitError e) {
            e.printStackTrace();
        }
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
    //endregion

    //region UPLOAD OBJECT

    public void enqueue(Context context, String containerId, UploadObject object) {

        try {
            DB db = App.getDB(context);

            Logger.Log("class in enqueue: " + object.getCls());

            // Reset index if there is no item left in the queue
            int leftCount = db.countKeys(CJayConstant.PREFIX_UPLOAD_QUEUE);
            if (leftCount == 0) {
                Logger.w("Reset index to 0");
                PreferencesUtil.storePrefsValue(context, PreferencesUtil.PREF_UPLOAD_QUEUE_INDEX, 0);
            }

            Logger.w("leftCount: " + leftCount);

            // Enqueue
            int currentIndex = PreferencesUtil.getPrefsValue(context, PreferencesUtil.PREF_UPLOAD_QUEUE_INDEX, 0);

            if (leftCount > 0) {
                currentIndex = currentIndex + 1;
                PreferencesUtil.storePrefsValue(context, PreferencesUtil.PREF_UPLOAD_QUEUE_INDEX, currentIndex);
            }

            String key = CJayConstant.PREFIX_UPLOAD_QUEUE + currentIndex + ":" + containerId;
            Logger.Log("key in enqueue: " + key);
            db.put(key, object);

        } catch (SnappydbException e) {
            Logger.e(e.getMessage());
        }
    }

    public void remove(Context context) throws SnappydbException {
        DB db = App.getDB(context);
        KeyIterator it = db.findKeysIterator(CJayConstant.PREFIX_UPLOAD_QUEUE);
        String[] keys = it.next(1);
        it.close();
        if (null != keys && keys.length > 0 && keys[0].contains(CJayConstant.PREFIX_UPLOAD_QUEUE)) {
            Logger.Log("Delete item: " + keys[0]);
            db.del(keys[0]);
        }
    }

    public UploadObject getNextItem(Context context) {

        UploadObject object = null;
        try {
            DB db = App.getDB(context);
            KeyIterator it = db.findKeysIterator(CJayConstant.PREFIX_UPLOAD_QUEUE);
            String[] keys = it.next(1);
            it.close();

            if (null != keys && keys.length > 0 && keys[0].contains(CJayConstant.PREFIX_UPLOAD_QUEUE)) {
                object = db.getObject(keys[0], UploadObject.class);

                Logger.Log("keys length: " + keys.length);
                Logger.Log("class in getNextItem: " + object.getCls());
            }

        } catch (SnappydbException e) {
            e.printStackTrace();
        }

        return object;
    }

    public UploadObject update(UploadObject object) throws SnappydbException {

        if (object.getCls() == Session.class) {

            Session session = getSession(context, object.getContainerId());
            UploadObject newObject = new UploadObject(session, Session.class, session.getContainerId());
            object = object.mergeCJayObject(newObject);
            return object;

        } else if (object.getCls() == AuditItem.class) {

            Session session = getSession(context, object.getContainerId());
            AuditItem auditItem = getAuditItem(context, object.getContainerId(), object.getAuditItem().getUuid());
            UploadObject newObject = new UploadObject(auditItem, AuditItem.class, object.getContainerId(), session.getId());
            object.mergeCJayObject(newObject);
            return object;

        } else {
            return object;
        }
    }

    //endregion

    /**
     * 1. Find next upload item
     * 2. Update object data
     * 3. Add object to JobManager
     *
     * @param context
     * @throws SnappydbException
     */
    public void startUploading(Context context) throws SnappydbException {

        Logger.Log("on startUploading");

        // Find next upload item
        JobManager jobManager = App.getJobManager();
        UploadObject object = getNextItem(context);

        if (object != null) {

            // update object before upload
            object = update(object);

            // Add Job in background
            Class cls = object.getCls();
            if (cls == Session.class) {
                Logger.Log("addJobInBackground Session");
                jobManager.addJobInBackground(new UploadSessionJob(object.getSession(), object));
            } else if (cls == AuditItem.class) {
                Logger.Log("addJobInBackground audit item");
                jobManager.addJobInBackground(new UploadAuditItemJob(object.getSessionId(), object.getAuditItem(), object.getContainerId(), object, false));

            } else if (cls == GateImage.class) {
                GateImage gateImage = object.getGateImage();
                ImageType type = ImageType.values()[((int) gateImage.getType())];
                jobManager.addJobInBackground(new UploadImageJob(gateImage.getUri(), gateImage.getName(), object.getContainerId(), type, object));

            } else if (cls == AuditImage.class) {
                AuditImage auditImage = object.getAuditImage();
                ImageType type = ImageType.values()[((int) auditImage.getType())];
                jobManager.addJobInBackground(new UploadImageJob(auditImage.getUri(), auditImage.getName(), object.getContainerId(), type, object));
            } else {
                Logger.wtf("Something goes wrong.");
            }

        } else {
            Logger.Log("No more item. Stop upload service.");
            context.stopService(new Intent(context, UploadIntentService_.class));
        }
    }

    //Back from available to audit step
    public void backToAudit(Context context, String containerId) {
        try {
            DB db = App.getDB(context);
            Session session = db.getObject(containerId, Session.class);
            session.setLocalStep(Step.AUDIT.value);
            db.put(containerId, session);

            Intent intent = new Intent(context, WizardActivity_.class);
            intent.putExtra(WizardActivity.CONTAINER_ID_EXTRA, containerId);
            intent.putExtra(WizardActivity.STEP_EXTRA, Step.AUDIT.value);
            context.startActivity(intent);

        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }
}
