package com.cloudjay.cjay.task.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.cloudjay.cjay.adapter.IsoCodeSyncAdapter;

public class IsoCodeSyncService extends Service {

    private static final Object isoCodeSyncAdapterLock = new Object();
    private static IsoCodeSyncAdapter isoCodeSyncAdapter = null;

    @Override
    public void onCreate() {

//        Logger.i("Sync server start!");
//
//        synchronized (isoCodeSyncAdapterLock) {
//            if (isoCodeSyncAdapter == null) {
//                isoCodeSyncAdapter = new IsoCodeSyncAdapter(getApplicationContext(), true);
//            }
//        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return isoCodeSyncAdapter.getSyncAdapterBinder();
    }
}
