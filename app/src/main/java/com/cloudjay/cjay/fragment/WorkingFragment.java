package com.cloudjay.cjay.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.ListView;
import android.widget.TextView;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.adapter.SessionAdapter;
import com.cloudjay.cjay.event.ImageCapturedEvent;
import com.cloudjay.cjay.event.WorkingSessionCreatedEvent;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.CJayConstant;
import com.snappydb.DB;
import com.snappydb.SnappydbException;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Danh sách các container đang thao tác
 */
@EFragment(R.layout.fragment_working)
public class WorkingFragment extends Fragment {


	@ViewById(R.id.lv_working_container)
	ListView lvWorking;

	@ViewById(R.id.tv_empty_list_working)
	TextView tvEmpty;

	SessionAdapter mAdapter;
	List<Session> workingSessions;

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
		workingSessions = new ArrayList<Session>();

		try {
			DB db = App.getDB(getActivity().getApplicationContext());

			String[] listWorkingId = db.findKeys(CJayConstant.PREFIX_WORKING);
			for (String workingId : listWorkingId) {
				Session session = db.getObject(workingId, Session.class);
				workingSessions.add(session);
			}

			db.close();
		} catch (SnappydbException e) {
			e.printStackTrace();
		}

		mAdapter = new SessionAdapter(getActivity(), R.layout.item_container_working);
		mAdapter.setData(workingSessions);

		lvWorking.setAdapter(mAdapter);
		lvWorking.setEmptyView(tvEmpty);
	}

	/**
	 * Sự kiện được kích hoạt khi một WorkingSession được tạo ra
	 *
	 * @param event
	 */
	@UiThread
	public void onEvent(WorkingSessionCreatedEvent event) {
		try {
			Session session = App.getDB(getActivity()).getObject(CJayConstant.PREFIX_WORKING + event.getSession().getContainerId(), Session.class);
			workingSessions.add(session);
			mAdapter.setData(workingSessions);
			mAdapter.notifyDataSetChanged();
		} catch (SnappydbException e) {
			e.printStackTrace();
		}
	}

	@UiThread
	public void onEvent(ImageCapturedEvent event) {
		Session session;
		try {
			Session oldSession = null;
			session = App.getDB(getActivity()).getObject(CJayConstant.PREFIX_WORKING + event.getContainerId(), Session.class);
			for (Session session1 : workingSessions) {
				if (session1.getContainerId().equals(event.getContainerId())) {
					oldSession = session1;
				}
			}
			workingSessions.remove(oldSession);
			workingSessions.add(session);
			mAdapter.setData(workingSessions);
			mAdapter.notifyDataSetChanged();
		} catch (SnappydbException e) {
			e.printStackTrace();
		}

	}

}
