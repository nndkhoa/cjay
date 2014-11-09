package com.cloudjay.cjay.task.job;

import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.event.session.ContainersFetchedEvent;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import de.greenrobot.event.EventBus;

public class FetchSessionsJob extends Job {

	String modifiedDate;

	/**
	 * Priority Higher is better.
	 * Do not need to pass context into Job. Because JobQueue cannot persist Context.
	 *
	 * @param modifiedDate
	 */

	public FetchSessionsJob(String modifiedDate) {
		super(new Params(1).requireNetwork().persist().setPersistent(true));
		this.modifiedDate = modifiedDate;
	}

	@Override
	public void onAdded() {
	}

	@Override
	public void onRun() throws Throwable {
		Context context = App.getInstance().getApplicationContext();
		DataCenter_.getInstance_(context).fetchSession(context, modifiedDate, true);

		// Notify UI that all data was fetched
		EventBus.getDefault().post(new ContainersFetchedEvent());
	}

	@Override
	protected void onCancel() {

	}

	@Override
	protected boolean shouldReRunOnThrowable(Throwable throwable) {
		return false;
	}
}
