package com.cloudjay.cjay.network;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.cloudjay.cjay.adapter.IsoCodeSyncAdapter;
import com.cloudjay.cjay.util.Logger;

import java.util.Objects;

/**
 * Created by nambv on 16/09/2014.
 */
public class IsoCodeSyncService extends Service {

    private static final Object isoCodeSyncAdapterLock = new Object();
    private static IsoCodeSyncAdapter isoCodeSyncAdapter = null;

    @Override
    public void onCreate() {

        Logger.i("Sync server start!");

        synchronized (isoCodeSyncAdapterLock) {
            if (isoCodeSyncAdapter == null) {
                isoCodeSyncAdapter = new IsoCodeSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return isoCodeSyncAdapter.getSyncAdapterBinder();
    }
}
