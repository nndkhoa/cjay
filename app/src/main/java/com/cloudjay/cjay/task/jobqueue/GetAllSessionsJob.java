package com.cloudjay.cjay.task.jobqueue;

import android.content.Context;

import com.cloudjay.cjay.api.NetworkClient;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.Logger;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import java.util.List;


/**
 * Created by thai on 06/10/2014.
 */

public class GetAllSessionsJob extends Job {

    Context context;


    public GetAllSessionsJob(Context context) {
        super(new Params(1).requireNetwork());
        this.context = context;
    }

    @Override
    public void onAdded() {
        Logger.Log("Add Fetch Job");
    }

    @Override
    public void onRun() throws Throwable {
        Logger.Log("Feching data");
        NetworkClient networkClient = new NetworkClient(context);
        List<Session> sessions = networkClient.getAllSessions(context, false);

    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }
}
