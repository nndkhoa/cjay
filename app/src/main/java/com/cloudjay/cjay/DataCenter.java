package com.cloudjay.cjay;

import android.content.Context;

import com.cloudjay.cjay.api.NetworkClient;
import com.cloudjay.cjay.event.BeginSearchOnServerEvent;
import com.cloudjay.cjay.event.ContainerSearchedEvent;
import com.cloudjay.cjay.event.GateImagesGotEvent;
import com.cloudjay.cjay.event.OperatorsGotEvent;
import com.cloudjay.cjay.event.WorkingSessionCreatedEvent;
import com.cloudjay.cjay.model.GateImage;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.model.WorkingSession;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.PreferencesUtil;
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

    // Inject the rest client
    @Bean
    NetworkClient networkClient;


    public static final String NETWORK = "NETWORK";
    public static final String CACHE = "CACHE";

    Context context;

    public DataCenter(Context context) {
        this.context = context;
    }

    public String getToken(String email, String password) {
        return networkClient.getToken(email, password);
    }

    public User getCurrentUser(Context context) {
        User user = networkClient.getCurrentUser(context);
        PreferencesUtil.storePrefsValue(context, PreferencesUtil.PREF_USER_ROLE, user.getRole() + "");
        PreferencesUtil.storePrefsValue(context, PreferencesUtil.PREF_USER_DEPOT, user.getDepotCode() + "");
        return user;
    }

    public void fetchOperators(Context context) throws SnappydbException {
        List<Operator> operators = networkClient.getOperators(context, null);
        for (Operator operator : operators) {
            App.getSnappyDB(context).put(CJayConstant.OPERATOR_KEY + operator.getOperatorCode(), operator);
        }
    }

    public void fetchIsoCodes(Context context) {
        //TODO: Add iso code to database @Thai
        networkClient.getDamageCodes(context, null);
        networkClient.getRepairCodes(context, null);
        networkClient.getComponentCodes(context, null);
    }

    @Trace
    public void fetchSession(Context context, String lastModifiedDate) throws SnappydbException {
        List<Session> sessions = networkClient.getAllSessions(context, lastModifiedDate);
        for (Session session : sessions) {
            Logger.e("Added: " + session.getContainerId());
            App.getSnappyDB(context).put(session.getContainerId(), session);
        }

    }

    @Trace
    @Background(serial = CACHE)
    public void search(Context context, String keyword) {

        Logger.Log("Begin search: " + keyword);

        // Search on local db

        String[] keysresult;
        try {
            keysresult = App.getSnappyDB(context).findKeys(keyword);
            List<Session> sessions = new ArrayList<Session>();
            for (String result : keysresult) {
                sessions.add(App.getSnappyDB(context).getObject(result, Session.class));
            }
            if (sessions.size() != 0) {
                EventBus.getDefault().post(new ContainerSearchedEvent(sessions));
            } else {
                // TODO: @thai need to alert to user about that no results was found in local

                // If there was not result in local, send search request to server
                EventBus.getDefault().post(new BeginSearchOnServerEvent(
                        context.getResources().getString(R.string.search_on_server)));
                searchAsync(context, keyword);
            }
        } catch (SnappydbException e) {
            e.printStackTrace();
        }

    }

    @Background(serial = NETWORK)
    public void searchAsync(Context context, String keyword) {

        Logger.Log("Begin to search container from server");
        List<Session> sessions = networkClient.searchSessions(context, keyword);

        if (sessions.size() != 0) {
            for (Session session : sessions) {
                try {
                    App.getSnappyDB(context).put(session.getContainerId(), session);
                } catch (SnappydbException e) {
                    e.printStackTrace();
                }
            }
            EventBus.getDefault().post(new ContainerSearchedEvent(sessions));
        } else {
            EventBus.getDefault().post(new ContainerSearchedEvent(sessions));
        }

    }

    @Background(serial = CACHE)
    public void getOperators() {

        // Search on local db
        List<Operator> operators = new ArrayList<Operator>();
        String[] keysresult;
        try {
            keysresult = App.getSnappyDB(context).findKeys(CJayConstant.OPERATOR_KEY);
            for (String result : keysresult) {
                operators.add(App.getSnappyDB(context).getObject(result, Operator.class));
            }
            EventBus.getDefault().post(new OperatorsGotEvent(operators));
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
            App.getSnappyDB(context).put(containerId, session);
            addWorkingId(context, containerId);
            Logger.Log("insert session successfully");
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    public void addWorkingId(Context context, String containerId) {

        WorkingSession workingSession = null;

        //Get working session list, if can't create one
        try {
            Session sessionWorking = App.getSnappyDB(context).getObject(containerId, Session.class);
            sessionWorking.setProcessing(true);
            try {
                workingSession = App.getSnappyDB(context).getObject(CJayConstant.WORKING_DB, WorkingSession.class);
                List<Session> current = workingSession.getWorkingSession();
                current.add(sessionWorking);
                workingSession.setWorkingSession(current);
                try {
                    App.getSnappyDB(context).put(CJayConstant.WORKING_DB, workingSession);
                } catch (SnappydbException e) {
                    e.printStackTrace();
                }
                EventBus.getDefault().post(new WorkingSessionCreatedEvent(current));
            } catch (SnappydbException e) {
                WorkingSession workingSessionCreate = new WorkingSession();

                List<Session> current = new ArrayList<Session>();
                current.add(sessionWorking);
                workingSessionCreate.setWorkingSession(current);
                App.getSnappyDB(context).put(CJayConstant.WORKING_DB, workingSessionCreate);
                EventBus.getDefault().post(new WorkingSessionCreatedEvent(current));
            }
        } catch (SnappydbException e) {
            e.printStackTrace();
        }


    }

    public void addGateImage(long type, String url, String containerId) throws SnappydbException {
        Logger.Log("url when insert in data center: " + url);

        Session session = App.getSnappyDB(context).getObject(containerId, Session.class);
        GateImage gateImage = new GateImage();
        gateImage.setId(0);
        gateImage.setType(type);
        gateImage.setUrl(url);

        List<GateImage> gateImages = session.getGateImages();
        if (gateImages == null){
           gateImages = new ArrayList<GateImage>();
        }
        gateImages.add(gateImage);
        session.setGateImages(gateImages);

        App.getSnappyDB(context).put(containerId, session);

        Logger.Log("insert gate image successfully");
    }

    public void getGateImages(long type, String containerId) throws SnappydbException {
        Logger.Log("type = " + type + ", containerId = " + containerId);
        Session session = App.getSnappyDB(context).getObject(containerId, Session.class);
        List<GateImage> gateImages = session.getGateImages();
        for (GateImage g : gateImages) {
            Logger.Log("url: " + g.getUrl());
        }
        Logger.Log("gate images count in dataCenter: " + gateImages.size());
        EventBus.getDefault().post(new GateImagesGotEvent(gateImages));
    }

    public void searchOperator(String keyword) throws SnappydbException {
        String[] keysresult = App.getSnappyDB(context).findKeys(CJayConstant.OPERATOR_KEY+keyword);
        List<Operator> operators = new ArrayList<Operator>();
        for (String result : keysresult) {
            operators.add(App.getSnappyDB(context).getObject(result, Operator.class));
        }

        EventBus.getDefault().post(new OperatorsGotEvent(operators));
    }

}