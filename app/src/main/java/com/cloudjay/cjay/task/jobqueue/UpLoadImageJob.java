package com.cloudjay.cjay.task.jobqueue;

import android.content.Context;

import com.cloudjay.cjay.api.NetworkClient;
import com.cloudjay.cjay.api.NetworkClient_;
import com.cloudjay.cjay.event.ResumeUpLoadEvent;
import com.cloudjay.cjay.event.StopUpLoadEvent;
import com.cloudjay.cjay.event.UploadedEvent;
import com.cloudjay.cjay.util.Logger;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import de.greenrobot.event.EventBus;

/**
 * Created by thai on 04/10/2014.
 */

public class UpLoadImageJob extends Job {
    Context context;
    String containerId;
    String uri;
    String imageName;


    public UpLoadImageJob(Context context, String uri, String imageName, String containerId) {
        super(new Params(2).requireNetwork().groupBy(containerId));

        this.context = context;
        this.containerId =containerId;
        this.uri = uri;
        this.imageName =imageName;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        EventBus.getDefault().post(new ResumeUpLoadEvent());

        NetworkClient networkClient = NetworkClient_.getInstance_(context);
        networkClient.uploadImage(context, uri,imageName);


    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        EventBus.getDefault().post(new StopUpLoadEvent());
        return true;
    }
}
