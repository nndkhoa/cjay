package com.cloudjay.cjay.task.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.util.PreferencesUtil;
import com.snappydb.SnappydbException;

import org.androidannotations.annotations.EIntentService;

/**
 * Created by thai on 22/10/2014.
 */
@EIntentService
public class QueueIntentService extends IntentService {
    public QueueIntentService() {
        super("QueueIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            DataCenter_.getInstance_(getApplicationContext()).fetchSession(getApplicationContext(), PreferencesUtil.getPrefsValue(getApplicationContext(),PreferencesUtil.PREF_MODIFIED_DATE));
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }
}
