package com.cloudjay.cjay.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.ListView;
import android.widget.TextView;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.adapter.UploadSessionAdapter;
import com.cloudjay.cjay.event.session.ContainersGotEvent;
import com.cloudjay.cjay.event.upload.UploadStartedEvent;
import com.cloudjay.cjay.event.upload.UploadStoppedEvent;
import com.cloudjay.cjay.event.upload.UploadSucceededEvent;
import com.cloudjay.cjay.event.upload.UploadingEvent;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.enums.UploadStatus;
import com.cloudjay.cjay.util.enums.UploadType;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import de.greenrobot.event.EventBus;


@EFragment(R.layout.fragment_upload)
public class UploadFragment extends Fragment {

	@ViewById(R.id.lv_uploading_container)
	ListView lvUploading;

	@ViewById(R.id.tv_empty_list_uploading)
	TextView tvEmpty;

	@Bean
	DataCenter dataCenter;

	private UploadSessionAdapter mAdapter;

	public UploadFragment() {
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
	void initLoader() {
		mAdapter = new UploadSessionAdapter(getActivity(), R.layout.item_upload);
		lvUploading.setAdapter(mAdapter);
		lvUploading.setEmptyView(tvEmpty);
		refresh();
	}

	void refresh() {
		if (mAdapter != null) {
			dataCenter.getSessionsInBackground(getActivity().getApplicationContext(), CJayConstant.PREFIX_UPLOADING);
		}
	}

	@UiThread
	public void updatedData(List<Session> sessionList) {
		mAdapter.setData(sessionList);
	}

	//region EVENT HANDLER
	public void onEvent(UploadStartedEvent event) {
		refresh();
	}

	public void onEvent(UploadStoppedEvent event) {
		refresh();
	}

	public void onEvent(UploadingEvent event) {
		refresh();
	}

	public void onEvent(UploadSucceededEvent event) {
		refresh();
	}

	public void onEvent(ContainersGotEvent event) {
		if (event.getPrefix().equals(CJayConstant.PREFIX_UPLOADING))
			updatedData(event.getTargets());
	}
	//endregion

}
