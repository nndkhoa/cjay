package com.cloudjay.cjay;

import android.content.Context;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.Session;

public class CJayActivity extends SherlockFragmentActivity implements
		ICJayActivity {

	private Session session;
	DataCenter dataCenter;

	public Session getSession() {
		return session;
	}

	public User getCurrentUser() {
		if (null == session)
			session = Session.restore(getApplicationContext());

		return session.getCurrentUser();
	}

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		session = Session.restore(getApplicationContext());
	}

	@Override
	public Context getContext() {
		return this;
	}

}
