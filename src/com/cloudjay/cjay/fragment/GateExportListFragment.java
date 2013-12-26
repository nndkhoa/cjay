package com.cloudjay.cjay.fragment;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.ami.fundapter.BindDictionary;
import com.ami.fundapter.FunDapter;
import com.ami.fundapter.extractors.StringExtractor;
import com.ami.fundapter.interfaces.DynamicImageLoader;
import com.ami.fundapter.interfaces.ItemClickListener;
import com.cloudjay.cjay.CameraActivity_;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.TmpContainerSession;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.Mapper;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ItemClick;
import com.googlecode.androidannotations.annotations.ViewById;

@EFragment(R.layout.fragment_gate_export)
public class GateExportListFragment extends SherlockDialogFragment {

	private ArrayList<ContainerSession> mFeeds;
	private FunDapter<ContainerSession> mAdapter;

	@ViewById(R.id.container_list)
	ListView mFeedListView;
	@ViewById(R.id.search_textfield)
	EditText mSearchEditText;

	@AfterViews
	void afterViews() {
		mSearchEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable arg0) {
				search(arg0.toString());
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		});
		mFeeds = (ArrayList<ContainerSession>) DataCenter.getInstance().getListContainerSessions(getActivity());
		initFunDapter(mFeeds);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		// Set Soft Input mode so it's always visible
		dialog.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		return dialog;
	}

	@Override
	public void onResume() {
		super.onResume();
		// Hector: will update UI here
	}

	@ItemClick(R.id.container_list)
	void containerItemClick(int position) {
		ContainerSession containerSession = mAdapter.getItem(position);

		TmpContainerSession tmpContainerSession = Mapper.toTmpContainerSession(
				containerSession, getActivity());

		// Pass tmpContainerSession away
		// Then start showing the Camera
		Intent intent = new Intent(getActivity(), CameraActivity_.class);
		intent.putExtra(CameraActivity_.CJAY_CONTAINER_SESSION_EXTRA,
				tmpContainerSession);
		intent.putExtra("type", 1); // in
		startActivity(intent);
	}

	private void search(String searchText) {
		if (searchText.equals("")) {
			mAdapter.updateData(mFeeds);
		} else {
			ArrayList<ContainerSession> searchFeeds = new ArrayList<ContainerSession>();
			for (ContainerSession containerSession : mFeeds) {
				if (containerSession.getContainerId().contains(searchText)) {
					searchFeeds.add(containerSession);
				}
			}
			// refresh list
			mAdapter.updateData(searchFeeds);
		}
	}

	private void initFunDapter(ArrayList<ContainerSession> containers) {
		BindDictionary<ContainerSession> feedsDict = new BindDictionary<ContainerSession>();
		feedsDict.addStringField(R.id.feed_item_container_id,
				new StringExtractor<ContainerSession>() {
					@Override
					public String getStringValue(ContainerSession item,
							int position) {
						return item.getContainerId();
					}
				});
		feedsDict.addStringField(R.id.feed_item_container_owner,
				new StringExtractor<ContainerSession>() {
					@Override
					public String getStringValue(ContainerSession item,
							int position) {
						return item.getOperatorName();
					}
				});
		feedsDict.addStringField(R.id.feed_item_container_import_date,
				new StringExtractor<ContainerSession>() {
					@Override
					public String getStringValue(ContainerSession item,
							int position) {
						return item.getCheckInTime();
					}
				});
		feedsDict.addDynamicImageField(R.id.feed_item_picture,
				new StringExtractor<ContainerSession>() {
					@Override
					public String getStringValue(ContainerSession item,
							int position) {
						return item.getContainerId();
					}
				}, new DynamicImageLoader() {
					@Override
					public void loadImage(String stringColor, ImageView view) {
						view.setImageResource(R.drawable.ic_logo);
					}
				}).onClick(new ItemClickListener<ContainerSession>() {
			@Override
			public void onClick(ContainerSession item, int position, View view) {
				// TODO Auto-generated method stub
			}
		});
		mAdapter = new FunDapter<ContainerSession>(getActivity(), containers,
				R.layout.list_item_container, feedsDict);
		mFeedListView.setAdapter(mAdapter);
	}
}
