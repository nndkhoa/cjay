package com.cloudjay.cjay.task.jobqueue;

import android.content.Context;

import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.api.NetworkClient;
import com.cloudjay.cjay.api.NetworkClient_;
import com.cloudjay.cjay.event.UpLoadingEvent;
import com.cloudjay.cjay.event.StartUpLoadEvent;
import com.cloudjay.cjay.event.StopUpLoadEvent;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.snappydb.SnappydbException;

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
        try {
            DataCenter_.getInstance_(context).addUploadingSession(containerId);
            EventBus.getDefault().post(new StartUpLoadEvent(containerId));
        } catch (SnappydbException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onRun() throws Throwable {
        EventBus.getDefault().post(new UpLoadingEvent());
        DataCenter_.getInstance_(context).uploadImage(context, uri,imageName, containerId);


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
