package com.cloudjay.cjay.task.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;

import com.cloudjay.cjay.api.NetworkClient;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.StringUtils;
import com.cloudjay.cjay.util.Utils;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EIntentService;

import java.io.File;

/**
 * Created by nambv on 2015/02/02.
 */
@EIntentService
public class UploadLogService extends IntentService {

    public UploadLogService() {
        super("LogService");
    }

    // Inject the rest client
    @Bean
    NetworkClient networkClient;

    @Override
    protected void onHandleIntent(Intent intent) {

        Logger.w("Start upload log service");

        // create today String
        String today = StringUtils.getCurrentTimestamp(CJayConstant.DAY_FORMAT);
        String prefix = "cjay-log-" + today;

        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        for (File f : downloadDir.listFiles()) {
            if (f.isFile() && f.getName().contains(prefix)) {
                networkClient.uploadLogFile(f.getAbsolutePath(), f.getName());
            } else {
                Utils.writeErrorsToLogFile("can not find file to upload");
            }
        }
    }
}
