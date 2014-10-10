package com.cloudjay.cjay.fragment;

import android.support.v4.app.Fragment;
import android.widget.ListView;
import android.widget.TextView;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.adapter.SessionAdapter;
import com.cloudjay.cjay.model.Session;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Danh sách các container đang thao tác
 */
@EFragment(R.layout.fragment_working)
public class WorkingFragment extends Fragment {

	private static final int LOADER_ID = 1;

	@ViewById(R.id.lv_working_container)
	ListView lvWorking;

	@ViewById(R.id.tv_emptylist_working)
	TextView tvEmpty;

	private SessionAdapter mAdapter;

	public WorkingFragment() {
	}

	/**
	 * Initial loader and set adapter for list view
	 */
	@AfterViews
	void init() {
		mAdapter = new SessionAdapter(getActivity(), R.layout.item_container_working);
		lvWorking.setAdapter(mAdapter);
		lvWorking.setEmptyView(tvEmpty);

		Realm realm = Realm.getInstance(getActivity());
		RealmResults<Session> sessions = realm.where(Session.class).equalTo("processing", true).findAll();

		if (sessions.size() != 0) {
			mAdapter.addAll(sessions);
			mAdapter.notifyDataSetChanged();
		}
	}
}
