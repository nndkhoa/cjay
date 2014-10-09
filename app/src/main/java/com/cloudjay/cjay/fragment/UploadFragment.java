package com.cloudjay.cjay.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.widget.ListView;
import android.widget.TextView;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.adapter.SessionAdapter;
import com.cloudjay.cjay.adapter.UploadSessionAdapter;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.loader.AbstractDataLoader;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

@EFragment(R.layout.fragment_upload)
public class UploadFragment extends Fragment implements LoaderManager.LoaderCallbacks<RealmResults<Session>> {

    private static final int LOADER_ID = 1;

    @ViewById(R.id.lv_uploading_container)
    ListView lvUploading;

    @ViewById(R.id.tv_emptylist_uploading)
    TextView tvEmpty;

    private UploadSessionAdapter mAdapter;


	public UploadFragment() {
		// Required empty public constructor
	}

    /**
     * Initial loader and set adapter for list view
     */
    @AfterViews
    void initLoader() {
        getLoaderManager().initLoader(LOADER_ID, null, this);
        mAdapter = new UploadSessionAdapter(getActivity(), R.layout.item_container_working);
        lvUploading.setAdapter(mAdapter);
        lvUploading.setEmptyView(tvEmpty);
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
    public android.support.v4.content.Loader<RealmResults<Session>> onCreateLoader(int id, Bundle args) {

        return new AbstractDataLoader<RealmResults<Session>>(getActivity()) {
            @Override
            protected RealmResults<Session> buildList() {
                Realm realm = Realm.getInstance(context);
                return realm.where(Session.class).equalTo("processing",false).findAll();
            }
        };

    }

    @Override
    public void onLoadFinished(Loader<RealmResults<Session>> loader, RealmResults<Session> data) {
        mAdapter.clear();
        for (Session session : data) {
            mAdapter.add(session);
        }
    }


    @Override
    public void onLoaderReset(Loader<RealmResults<Session>> realmResultsLoader) {
        mAdapter.clear();
    }


}
