package com.cloudjay.cjay.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.widget.ListView;
import android.widget.TextView;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.event.ParsedSessionEvent;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.loader.AbstractDataLoader;
import com.cloudjay.cjay.adapter.SessionAdapter;
import com.cloudjay.cjay.model.Session;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Danh sách các container đang thao tác
 */
@EFragment(R.layout.fragment_working)
public class WorkingFragment extends Fragment implements LoaderManager.LoaderCallbacks<RealmResults<Session>> {

	private static final int LOADER_ID = 1;

	@ViewById(R.id.lv_working_container)
	ListView listView;

	@ViewById(R.id.tv_emptylist_working)
	TextView tvEmpty;

	private SessionAdapter mAdapter;

	public WorkingFragment() {
		// Required empty public constructor
	}

	/**
	 * Initial loader and set adapter for list view
	 */
	@AfterViews
	void initLoader() {
		getLoaderManager().initLoader(LOADER_ID, null, this);
		mAdapter = new SessionAdapter(getActivity(), R.layout.item_container_working);
		listView.setAdapter(mAdapter);
		listView.setEmptyView(tvEmpty);
        Realm realm = Realm.getInstance(getActivity());
        realm.addChangeListener(new RealmChangeListener() {

            @Override
            public void onChange() {
                refreshListView();
            }
        });
	}

    private void refreshListView() {
        getLoaderManager().restartLoader(LOADER_ID,null,this);
    }

    @Override
	public Loader<RealmResults<Session>> onCreateLoader(int id, Bundle args) {

		return new AbstractDataLoader<RealmResults<Session>>(getActivity()) {
			@Override
			protected RealmResults<Session> buildList() {
				Realm realm = Realm.getInstance(context);
				return realm.where(Session.class).equalTo("processing",true).findAll();
			}
		};

	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
	public void onLoadFinished(Loader<RealmResults<Session>> loader, RealmResults<Session> data) {
		mAdapter.clear();
		for (Session session : data) {
			mAdapter.add(session);
		}
	}

	@Override
	public void onLoaderReset(Loader<RealmResults<Session>> loader) {
		mAdapter.clear();
	}

}
