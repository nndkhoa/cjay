package com.cloudjay.cjay.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.ListView;
import android.widget.TextView;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.adapter.SessionAdapter;
import com.cloudjay.cjay.event.WorkingSessionCreatedEvent;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.model.WorkingSession;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.snappydb.SnappydbException;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import de.greenrobot.event.EventBus;

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


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EventBus.getDefault().register(this);
	}

	@Override
	public void onDestroy() {
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}

	/**
	 * Initial loader and set adapter for list view
	 */
	@AfterViews
	void init() {

		List<Session> workingSession = null;

		try {
			workingSession = App.getSnappyDB(getActivity()).getObject(CJayConstant.WORKING_DB, WorkingSession.class).getWorkingSession();
			Logger.e(String.valueOf(workingSession.size()));
		} catch (SnappydbException e) {
			e.printStackTrace();
		}
		mAdapter = new SessionAdapter(getActivity(), R.layout.item_container_working);
		mAdapter.setData(workingSession);
		lvWorking.setAdapter(mAdapter);
		lvWorking.setEmptyView(tvEmpty);
	}

	@UiThread
	public void onEvent(WorkingSessionCreatedEvent event) {
		try {
			Logger.e("WorkingSessionCreatedEvent");
			List<Session> workingSession = App.getSnappyDB(getActivity()).getObject(CJayConstant.WORKING_DB, WorkingSession.class).getWorkingSession();
			mAdapter.clear();
			mAdapter.setData(workingSession);
			mAdapter.notifyDataSetChanged();
		} catch (SnappydbException e) {
			e.printStackTrace();
		}
	}
}
