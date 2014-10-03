package com.cloudjay.cjay.jobqueue;

import android.content.Context;

import com.cloudjay.cjay.event.GotAllSessionEvent;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.network.NetworkClient;
import com.cloudjay.cjay.util.Logger;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import de.greenrobot.event.EventBus;

/**
 * Created by Thai on 10/2/2014.
 */
public class GetAllSessionsJob extends Job {
	public static final int PRIORITY = 1;
	public static  int PAGE =1;
	Context context;

	public GetAllSessionsJob(Context context) {
		super(new Params(500).requireNetwork().groupBy("get_session"));
		this.context = context;

	}

	@Override
	public void onAdded() {
		Logger.e("Added job to run");
		Logger.e(String.valueOf(getRetryLimit()));

	}

	@Override
	public void onRun() throws Throwable {
		Logger.e("Job is running");
		Logger.e(String.valueOf(getCurrentRunCount()));
		String token = NetworkClient.getInstance().getToken(context, "giamdinhcong1.icd1@pip.com.vn", "123456");
		String mToken = "Token " + token;
		User user = NetworkClient.getInstance().getCurrentUser(context, mToken);
		NetworkClient.getInstance().getAllSession(context, mToken, user.getFullName(),null);
		Logger.e("Job is done");

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
