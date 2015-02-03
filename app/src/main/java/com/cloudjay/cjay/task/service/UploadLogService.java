package com.cloudjay.cjay.task.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;

import com.cloudjay.cjay.api.ApiEndpoint;
import com.cloudjay.cjay.api.NetworkService;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.StringUtils;

import org.androidannotations.annotations.EIntentService;

import java.io.File;

import retrofit.RestAdapter;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

/**
 * Created by nambv on 2015/02/02.
 */
@EIntentService
public class UploadLogService extends IntentService {

    public UploadLogService() {
        super("LogService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Logger.w("Start upload log service");

        // create today String
        String today = StringUtils.getCurrentTimestamp(CJayConstant.DAY_FORMAT);
        String fileName ="cjay-log-" + today + ".txt";

        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        for (File f : downloadDir.listFiles()) {

            if (f.isFile() && f.getName().equals(fileName)) {

                // Init explicit rest adapter for upload to Google Cloud Storage
                RestAdapter restAdapter = new RestAdapter.Builder()
                        .setEndpoint(ApiEndpoint.CJAY_TMP_STORAGE)
                        .setLogLevel(RestAdapter.LogLevel.HEADERS)
                        .build();

                TypedFile typedFile = new TypedFile("text/plain", f);

                // Upload log file to server
                Response response = restAdapter.create(NetworkService.class).postLogFile("image/jpeg", "media", fileName, typedFile);

                break;
            }
        }
    }
}
