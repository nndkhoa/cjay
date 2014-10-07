package com.cloudjay.cjay.task.jobqueue;

import android.content.Context;

import com.cloudjay.cjay.api.NetworkClient;
import com.cloudjay.cjay.util.Logger;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

/**
 * Created by thai on 04/10/2014.
 */

public class UpLoadImageJob extends Job {
    Context context;
    String containerId;


    public UpLoadImageJob(Context context, String containerID) {
        super(new Params(2).requireNetwork().groupBy(containerID));
        this.context = context;
        this.containerId =containerID;
    }

    @Override
    public void onAdded() {
        Logger.d("Added Upload Image Job");

    }

    @Override
    public void onRun() throws Throwable {

        NetworkClient networkClient = new NetworkClient(context);
        networkClient.uploadImage(context);
        Logger.e("Upload Image " + containerId);

    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }
}
