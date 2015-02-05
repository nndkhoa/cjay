package com.cloudjay.cjay.task.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;

import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by nambv on 2015/02/03.
 */
public class CreateLogService extends IntentService {

    public CreateLogService() {
        super("CreateLogService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Logger.w("Start create log service");

        //Random UUID
        String uuid = UUID.randomUUID().toString();

        // create today String
        String today = StringUtils.getCurrentTimestamp(CJayConstant.DAY_FORMAT);
        String fileName = "cjay-log-" + today + "-" + uuid + ".txt";
        File logFile =
                new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS), fileName);
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
