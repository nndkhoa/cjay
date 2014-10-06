package com.cloudjay.cjay.task.job;

import android.content.Context;

import com.cloudjay.cjay.event.SessionsFetchedEvent;
import com.cloudjay.cjay.util.DataCenter_;
import com.cloudjay.cjay.util.Logger;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import org.androidannotations.annotations.Trace;

import de.greenrobot.event.EventBus;

/**
 * Created by Thai on 10/2/2014.
 */

public class GetAllSessionsJob extends Job {
	public static final int PRIORITY = 1;
	public static int PAGE = 1;
	Context context;

	public GetAllSessionsJob(Context context) {
		super(new Params(500).requireNetwork().groupBy("get_session"));
		this.context = context;
	}

	@Override
	public void onAdded() {

	}

	@Override
	@Trace
	public void onRun() throws Throwable {

		// Call DataCenter#fetchSessions
		DataCenter_.getInstance_(context).fetchSession(context);

		// post Event
		EventBus.getDefault().post(new SessionsFetchedEvent());
	}

	@Override
	protected void onCancel() {

	}

	@Override
	protected boolean shouldReRunOnThrowable(Throwable throwable) {
		Logger.e(throwable.toString());
		return true;
	}
}
