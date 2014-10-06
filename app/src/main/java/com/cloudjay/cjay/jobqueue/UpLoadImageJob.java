package com.cloudjay.cjay.jobqueue;

import android.content.Context;

import com.cloudjay.cjay.api.NetworkClient;
import com.cloudjay.cjay.util.Logger;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

/**
 * Created by thai on 04/10/2014.
 */

public class UpLoadImageJob extends Job {
    Context context;


    public UpLoadImageJob(Context context) {
        super(new Params(1).requireNetwork());
        this.context = context;
    }

    @Override
    public void onAdded() {
        Logger.d("Added Upload Image Job");

    }

    @Override
    public void onRun() throws Throwable {
        Logger.e("Running Upload Image");
        NetworkClient networkClient = new NetworkClient(context);
        networkClient.uploadImage(context);

    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }
}
