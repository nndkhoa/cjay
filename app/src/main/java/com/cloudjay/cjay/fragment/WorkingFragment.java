package com.cloudjay.cjay.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.adapter.AbstractDataLoader;
import com.cloudjay.cjay.model.Session;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import io.realm.Realm;
import io.realm.RealmResults;

@EFragment(R.layout.fragment_working)
public class WorkingFragment extends Fragment implements LoaderManager.LoaderCallbacks<RealmResults<Session>> {

	private static final int LOADER_ID = 1;

	@ViewById(R.id.lv_search_container)
	ListView mFeedListView;

	private ArrayAdapter mAdapter;

	public WorkingFragment() {
		// Required empty public constructor
	}

	@AfterViews
	void initLoader() {
		getLoaderManager().initLoader(LOADER_ID, null, this);
		mAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1);
		mFeedListView.setAdapter(mAdapter);
	}

	@Override
	public Loader<RealmResults<Session>> onCreateLoader(int id, Bundle args) {

		return new AbstractDataLoader<RealmResults<Session>>(getActivity()) {
			@Override
			protected RealmResults<Session> buildList() {
				Realm realm = Realm.getInstance(context);
				return realm.where(Session.class).findAll();
			}
		};
	}

	@Override
	public void onLoadFinished(Loader<RealmResults<Session>> loader, RealmResults<Session> data) {
		mAdapter.clear();

		for(Session session : data){
			mAdapter.add(session);
		}

//		if (isResumed()) {
//			setListShown(true);
//		} else {
//			setListShownNoAnimation(true);
//		}
	}

	@Override
	public void onLoaderReset(Loader<RealmResults<Session>> loader) {
		mAdapter.clear();
	}

}
