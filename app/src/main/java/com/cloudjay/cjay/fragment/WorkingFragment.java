package com.cloudjay.cjay.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.ListView;
import android.widget.TextView;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.activity.HomeActivity;
import com.cloudjay.cjay.activity.WizardActivity;
import com.cloudjay.cjay.activity.WizardActivity_;
import com.cloudjay.cjay.adapter.SessionAdapter;
import com.cloudjay.cjay.event.session.ContainersGotEvent;
import com.cloudjay.cjay.event.session.WorkingSessionCreatedEvent;
import com.cloudjay.cjay.event.upload.UploadStartedEvent;
import com.cloudjay.cjay.event.upload.UploadSucceededEvent;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.enums.UploadType;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ItemLongClick;
import org.androidannotations.annotations.Trace;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Danh sách các container đang thao tác.
 * <p/>
 * Tab Working sẽ update lại UI trong những trường hợp sau:
 * - on resume
 * - new working session
 * - container uploaded
 */
@EFragment(R.layout.fragment_working)
public class WorkingFragment extends Fragment {


	@ViewById(R.id.lv_working_container)
	ListView lvWorking;

	@ViewById(R.id.tv_empty_list_working)
	TextView tvEmpty;

	@Bean
	DataCenter dataCenter;

	SessionAdapter mAdapter;

	public WorkingFragment() {
	}

	@ItemClick(R.id.lv_working_container)
	void workingItemClicked(int position) {

		// navigation to Wizard Activity
		Session item = mAdapter.getItem(position);
		Logger.Log("Clicked on container: " + item.getContainerId());

		Intent intent = new Intent(getActivity(), WizardActivity_.class);
		intent.putExtra(WizardActivity.CONTAINER_ID_EXTRA, item.getContainerId());
		intent.putExtra(WizardActivity.STEP_EXTRA, item.getLocalStep());
		startActivity(intent);
	}

	@ItemLongClick(R.id.lv_working_container)
	void workingItemLongClicked(int position) {

		//Get session from position
		Session item = mAdapter.getItem(position);
		HomeActivity activity = (HomeActivity) getActivity();
		activity.exportSessionContainerId = item.getContainerId();
		activity.showMenuExportImmediately(true);

	}

	/**
	 * Initial loader and set adapter for list view
	 */
	@AfterViews
	void init() {
		mAdapter = new SessionAdapter(getActivity(), R.layout.item_container_working);
		lvWorking.setAdapter(mAdapter);
		lvWorking.setEmptyView(tvEmpty);

		List<Session> list = dataCenter.getListSessions(getActivity().getApplicationContext(),
				CJayConstant.PREFIX_WORKING);
		updatedData(list);
	}

	void refresh() {
		if (mAdapter != null) {
			dataCenter.getSessionsInBackground(getActivity().getApplicationContext(), CJayConstant.PREFIX_WORKING);
		}
	}

	@UiThread
	public void updatedData(List<Session> sessionList) {
		mAdapter.setData(sessionList);
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

	//region EVENT HANDLER

	public void onEvent(WorkingSessionCreatedEvent event) {
		refresh();
	}

	public void onEvent(UploadSucceededEvent event) {
		if (event.getUploadType() == UploadType.SESSION)
			refresh();
	}

	public void onEvent(UploadStartedEvent event) {
		if (event.getUploadType() == UploadType.SESSION)
			refresh();
	}

	@Trace
	public void onEvent(ContainersGotEvent event) {
		if (event.getPrefix().equals(CJayConstant.PREFIX_WORKING))
			updatedData(event.getSessions());
	}

	//endregion
}
