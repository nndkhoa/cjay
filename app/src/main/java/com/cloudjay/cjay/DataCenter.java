package com.cloudjay.cjay;

import android.content.Context;

import com.cloudjay.cjay.api.NetworkClient;
import com.cloudjay.cjay.event.ContainerSearchedEvent;
import com.cloudjay.cjay.event.OperatorsGotEvent;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.PreferencesUtil;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.List;

import de.greenrobot.event.EventBus;
import io.realm.Realm;
import io.realm.RealmResults;

@EBean(scope = EBean.Scope.Singleton)
public class DataCenter {

    // Inject the rest client
    @Bean
    NetworkClient networkClient;

    // Inject the Cache

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

    public void fetchOperators(Context context) {
        List<Operator> operators = networkClient.getOperators(context, null);
    }

    public void fetchIsoCodes(Context context) {
        networkClient.getDamageCodes(context, null);
        networkClient.getRepairCodes(context, null);
        networkClient.getComponentCodes(context, null);
    }

    public void fetchSession(Context context, String lastModifiedDate) {
        // TODO: get all container sessions

        networkClient.getAllSessions(context, false);
    }

    @Background(serial = CACHE)
    public void search(Context context, String keyword) {

        // Search on local db
        Realm realm = Realm.getInstance(context);
        RealmResults<Session> sessions = realm.where(Session.class)
                .contains(Session.FIELD_CONTAINER_ID, keyword)
                .findAll();

        if (sessions != null) {
            EventBus.getDefault().post(new ContainerSearchedEvent(sessions));
        } else {
            // If there was not result in local, send search request to server
            // TODO: ask server for search feature
            searchAsync(context, keyword);
        }
    }

    @Background(serial = NETWORK)
    public void searchAsync(Context context, String keyword) {
        List<Session> sessions = networkClient.searchSessions(context, keyword);
        EventBus.getDefault().post(new ContainerSearchedEvent((RealmResults<Session>) sessions));
    }

    @Background(serial = CACHE)
    public void getOperators() {
        // Search on local db
        Realm realm = Realm.getInstance(context);
        RealmResults<Operator> operators = realm.where(Operator.class).findAll();
        Logger.Log("operators count in dataCenter: " + operators.size());
        EventBus.getDefault().post(new OperatorsGotEvent(operators));

    }

    @Background(serial = CACHE)
    public void addSession(String containerId, String operatorCode, long operatorId) {
        Realm realm = Realm.getInstance(context);

        // Open a transaction to store session into the realm
        realm.beginTransaction();

        Session session = realm.createObject(Session.class);
        session.setContainerId(containerId);
        session.setOperatorId(operatorId);
        session.setOperatorCode(operatorCode);

        realm.commitTransaction();

        Logger.Log("insert session successfully");
    }
}