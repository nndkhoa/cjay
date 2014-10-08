package com.cloudjay.cjay.task.jobqueue;

import android.content.Context;

import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.util.Logger;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

public class GetAllSessionsJob extends Job {

	Context context;
	String modifiedDate;

	public GetAllSessionsJob(Context context, String modifiedDate) {
		super(new Params(1).requireNetwork());
		this.context = context;
		this.modifiedDate = modifiedDate;
	}

	@Override
	public void onAdded() {
	}

	@Override
	public void onRun() throws Throwable {
		Logger.Log("Running fetch all sessions job");
		DataCenter_.getInstance_(context).fetchSession(context, modifiedDate);
	}

	@Override
	protected void onCancel() {

	}

	@Override
	protected boolean shouldReRunOnThrowable(Throwable throwable) {
		return false;
	}
}
