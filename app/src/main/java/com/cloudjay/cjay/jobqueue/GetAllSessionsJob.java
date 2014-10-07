package com.cloudjay.cjay.jobqueue;

import android.content.Context;
import android.util.Log;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.api.NetworkClient;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.Logger;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.List;


/**
 * Created by thai on 06/10/2014.
 */

public class GetAllSessionsJob extends Job {

    Context context;
    boolean isFetchByTime;

    /**
     * Fetch data from server
     * @param context
     * @param isFetchByTime
     */
    public GetAllSessionsJob(Context context, boolean isFetchByTime) {
        super(new Params(1).requireNetwork());
        this.context = context;
        this.isFetchByTime = isFetchByTime;
    }

    @Override
    public void onAdded() {
    }

    @Override
    public void onRun() throws Throwable {
        Logger.e("Running Fetch Job");
        NetworkClient networkClient = new NetworkClient(context);
        List<Session> sessions = networkClient.getAllSessions(context, isFetchByTime);

    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }
}
