package com.cloudjay.cjay.task.jobqueue;

import android.content.Context;

import com.cloudjay.cjay.util.Logger;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

/**
 * Created by thai on 06/10/2014.
 */
public class UploadSessionJob extends Job {
    String session;

    public UploadSessionJob(Context context, String session) {
        super(new Params(1).requireNetwork().groupBy(session));
        this.session = session;
    }

    @Override
    public void onAdded() {
    }

    @Override
    public void onRun() throws Throwable {
        Logger.e("Uploading container: " + session);

    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }
}
