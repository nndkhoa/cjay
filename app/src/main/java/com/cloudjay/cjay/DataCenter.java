package com.cloudjay.cjay;

import android.content.Context;

import com.cloudjay.cjay.api.NetworkClient;
import com.cloudjay.cjay.event.ContainerSearchedEvent;
import com.cloudjay.cjay.event.GateImagesGotEvent;
import com.cloudjay.cjay.event.OperatorsGotEvent;
import com.cloudjay.cjay.model.GateImage;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.PreferencesUtil;
import com.cloudjay.cjay.util.Utils;
import com.google.gson.JsonObject;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.Trace;

import java.util.List;

import de.greenrobot.event.EventBus;
import io.realm.Realm;
import io.realm.RealmResults;

@EBean(scope = EBean.Scope.Singleton)
public class DataCenter {

    // Inject the rest client
    @Bean
    NetworkClient networkClient;

    public static final String NETWORK = "NETWORK";
    public static final String CACHE = "CACHE";
    public static final String CALLBACK = "CALLBACK";
    public static final String GET_CALLBACK = "GET_CALLBACK";

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

    public void fetchOperators(Context context) {
        networkClient.getOperators(context, null);
    }

    public void fetchIsoCodes(Context context) {
        networkClient.getDamageCodes(context, null);
        networkClient.getRepairCodes(context, null);
        networkClient.getComponentCodes(context, null);
    }

    @Trace
    public void fetchSession(Context context, String lastModifiedDate) {
        networkClient.getAllSessions(context, lastModifiedDate);
    }

    @Trace
    @Background(serial = CACHE)
    public void search(Context context, String keyword) {

        Logger.Log("Begin search: " + keyword);
        // Search on local db
        Realm realm = Realm.getInstance(context);
        RealmResults<Session> sessions = realm.where(Session.class)
                .contains("containerId", keyword)
                .findAll();
        Logger.e(String.valueOf(sessions.size()));
        if (sessions.size() != 0) {
            EventBus.getDefault().post(new ContainerSearchedEvent(sessions));
        } else {
            // If there was not result in local, send search request to server
            searchAsync(context, keyword);
        }
    }

    @Background(serial = NETWORK)
    public void searchAsync(Context context, String keyword) {

        Logger.Log("Begin to search container from server");
        Realm realm = Realm.getInstance(context);
        List<Session> sessions = networkClient.searchSessions(context, keyword);
        if (sessions.size() != 0) {
            RealmResults<Session> results = realm.where(Session.class)
                    .contains("containerId", keyword)
                    .findAll();
            EventBus.getDefault().post(new ContainerSearchedEvent(results));
        } else {
            RealmResults<Session> results = realm.where(Session.class)
                    .contains("containerId", keyword)
                    .findAll();
            EventBus.getDefault().post(new ContainerSearchedEvent(results));
        }

    }

    @Background(serial = CACHE)
    public void getOperators() {

        // Search on local db
        Realm realm = Realm.getInstance(context);
        RealmResults<Operator> operators = realm.where(Operator.class).findAll();
        EventBus.getDefault().post(new OperatorsGotEvent(operators));
    }

    @Background(serial = CACHE)
    public void addSession(String containerId, String operatorCode, long operatorId) {
        Realm realm = Realm.getInstance(context);

        // Open a transaction to store session into the realm
        realm.beginTransaction();

        Session session = realm.createObject(Session.class);
        session.setId(0);
        session.setContainerId(containerId);
        session.setOperatorId(operatorId);
        session.setOperatorCode(operatorCode);

        // Commit transaction
        realm.commitTransaction();

        Logger.Log("insert session successfully");
    }

    public void addGateImage(long type, String url) {
        Logger.Log("url when insert in data center: " + url);
        Realm realm = Realm.getInstance(context);

        // Open a transaction to store session into the realm
        realm.beginTransaction();

        GateImage gateImage = realm.createObject(GateImage.class);
        gateImage.setId(0);
        gateImage.setType(type);
        gateImage.setUrl(url);
        realm.commitTransaction();

        Logger.Log("insert gate image successfully");
    }

    public void getGateImages(long type, String containerId) {
        Logger.Log("type = " + type + ", containerId = " + containerId);
        Realm realm = Realm.getInstance(context);
        RealmResults<GateImage> gateImages = realm.where(GateImage.class).contains("url", containerId).findAll();
        // RealmResults<GateImage> gateImages = realm.where(GateImage.class).findAll();
        for (GateImage g : gateImages) {
            Logger.Log("url: " + g.getUrl());
        }
        Logger.Log("gate images count in dataCenter: " + gateImages.size());
        EventBus.getDefault().post(new GateImagesGotEvent(gateImages));
    }

    public void searchOperator(String keyword) {
        Realm realm = Realm.getInstance(context);
        RealmResults<Operator> operators = realm.where(Operator.class)
                .contains("operatorName", keyword).or()
                .contains("operatorCode", keyword)
                .findAll();

        EventBus.getDefault().post(new OperatorsGotEvent(operators));
    }

}