package com.cloudjay.cjay.task.jobqueue;

import android.content.Context;

import com.cloudjay.cjay.api.NetworkClient_;
import com.cloudjay.cjay.event.ResumeUpLoadEvent;
import com.cloudjay.cjay.event.StopUpLoadEvent;
import com.cloudjay.cjay.event.UploadedEvent;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.Logger;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import de.greenrobot.event.EventBus;

/**
 * Created by thai on 06/10/2014.
 */
public class UploadSessionJob extends Job {
    Session session;
    Context context;

    public UploadSessionJob(Context context, Session session) {
        super(new Params(1).requireNetwork().groupBy(session.getContainerId()));
        this.session = session;
        this.context = context;
    }

    @Override
    public void onAdded() {
    }

    @Override
    public void onRun() throws Throwable {
        EventBus.getDefault().post(new ResumeUpLoadEvent());

        Logger.e("Uploading container: " + session.getContainerId());
//        Session result = NetworkClient_.getInstance_(context).uploadContainerSession(context,session);

        EventBus.getDefault().post(new UploadedEvent());


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
