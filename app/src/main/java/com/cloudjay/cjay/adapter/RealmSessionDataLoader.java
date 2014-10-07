package com.cloudjay.cjay.adapter;

import android.content.Context;

import com.cloudjay.cjay.model.Session;

import io.realm.Realm;
import io.realm.RealmResults;

public class RealmSessionDataLoader extends AbstractDataLoader<RealmResults<Session>> {

	Context context;
	Realm realm;

	public RealmSessionDataLoader(Context context) {
		super(context);
		realm = Realm.getInstance(context);
	}

	@Override
	protected RealmResults<Session> buildList() {
		RealmResults<Session> sessions = realm.where(Session.class).findAll();
		return sessions;
	}
}