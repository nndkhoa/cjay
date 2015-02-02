package com.cloudjay.cjay.task.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;

import org.androidannotations.annotations.EIntentService;

import java.io.File;

/**
 * Created by nambv on 2015/02/02.
 */
@EIntentService
public class LogService extends IntentService {

    public LogService() {
        super("LogService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        for (File f : downloadDir.listFiles()) {
            if (f.isFile() && f.getName().equals("CJay_Log.txt")) {
                // Upload log file to server

                break;
            }
        }
    }
}
