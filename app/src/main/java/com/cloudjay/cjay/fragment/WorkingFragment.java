package com.cloudjay.cjay.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.Session;

import org.androidannotations.annotations.EFragment;

import io.realm.RealmResults;

@EFragment(R.layout.fragment_working)
public class WorkingFragment extends Fragment implements LoaderManager.LoaderCallbacks<RealmResults<Session>> {

	private static final int LOADER_ID = 1;

	public WorkingFragment() {
        // Required empty public constructor
    }

	@Override
	public Loader<RealmResults<Session>> onCreateLoader(int id, Bundle args) {
		return null;
	}

	@Override
	public void onLoadFinished(Loader<RealmResults<Session>> loader, RealmResults<Session> data) {

	}

	@Override
	public void onLoaderReset(Loader<RealmResults<Session>> loader) {

	}

}
