package com.cloudjay.cjay.task.jobqueue;

import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.event.upload.UploadStartedEvent;
import com.cloudjay.cjay.event.upload.UploadingEvent;
import com.cloudjay.cjay.model.Session;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import de.greenrobot.event.EventBus;

/**
 * Created by thai on 29/10/2014.
 */
public class UpLoadSessionHandCleaning extends Job {
    Session session;

    protected UpLoadSessionHandCleaning(Session session) {
        super(new Params(1).requireNetwork().persist().groupBy(session.getContainerId()));
        this.session = session;
    }

    @Override
    public void onAdded() {

        Context context = App.getInstance().getApplicationContext();
        DataCenter_.getInstance_(context).addUploadingSession(session.getContainerId());
        EventBus.getDefault().post(new UploadStartedEvent(session.getContainerId()));

    }

    @Override
    public void onRun() throws Throwable {

        EventBus.getDefault().post(new UploadingEvent());
        Context context = App.getInstance().getApplicationContext();
        DataCenter_.getInstance_(context).setHandCleaningSession(context, session.getContainerId());

    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }
}
