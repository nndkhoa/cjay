package com.cloudjay.cjay.fragment;

import android.support.v4.app.Fragment;
import android.widget.ListView;
import android.widget.TextView;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.adapter.UploadSessionAdapter;
import com.cloudjay.cjay.event.ResumeUpLoadEvent;
import com.cloudjay.cjay.event.StopUpLoadEvent;
import com.cloudjay.cjay.event.UploadedEvent;
import com.cloudjay.cjay.model.Session;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;


@EFragment(R.layout.fragment_upload)
public class UploadFragment extends Fragment {

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
		mAdapter = new UploadSessionAdapter(getActivity(), R.layout.item_upload);
		lvUploading.setAdapter(mAdapter);
		lvUploading.setEmptyView(tvEmpty);
	}


	public void onEvent(UploadedEvent event) {

	}

	public void onEvent(StopUpLoadEvent event) {
	}

	public void onEvent(ResumeUpLoadEvent event) {

	}
}
