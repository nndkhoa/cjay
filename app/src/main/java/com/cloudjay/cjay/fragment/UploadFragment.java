package com.cloudjay.cjay.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.ListView;
import android.widget.TextView;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.adapter.UploadSessionAdapter;
import com.cloudjay.cjay.event.upload.UploadStartedEvent;
import com.cloudjay.cjay.event.upload.UploadStoppedEvent;
import com.cloudjay.cjay.event.upload.UploadedEvent;
import com.cloudjay.cjay.event.upload.UploadingEvent;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.CJayConstant;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
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

	public void onEvent(UploadStartedEvent event) {
		refresh();
	}

	public void onEvent(UploadedEvent event) {
		refresh();
	}

	public void onEvent(UploadStoppedEvent event) {
		refresh();
	}

	public void onEvent(UploadingEvent event) {
		refresh();
	}

	@Background
	void refresh() {
		List<Session> list = dataCenter.getListSessions(getActivity().getApplicationContext(),
				CJayConstant.PREFIX_UPLOADING);
		updatedData(list);
	}

	@UiThread
	public void updatedData(List<Session> sessionList) {
		mAdapter.clear();
		if (sessionList != null) {
			for (Session object : sessionList) {
				mAdapter.insert(object, mAdapter.getCount());
			}
		}
		mAdapter.notifyDataSetChanged();
	}
}
