package com.cloudjay.cjay.fragment;

import java.util.ArrayList;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.Container;

public class FeedListFragment extends SherlockDialogFragment implements
		OnItemClickListener {
	
	private ListView mFeedListView;
	private ArrayAdapter<Container> mAdapter;
	
	private ArrayList<Container> mFeeds;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setStyle(STYLE_NO_TITLE, 0);
		
		// Hector: test only
		mFeeds = new ArrayList<Container>();
		for (int i = 0; i < 100; i++) {
			Container container = new Container();
			mFeeds.add(container);
		}

		mAdapter = new ArrayAdapter<Container>(getActivity(),
				android.R.layout.simple_list_item_1, mFeeds);

	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_feeds, container,
				false);

		mFeedListView = (ListView) view.findViewById(R.id.feeds);
		mFeedListView.setOnItemClickListener(this);
		mFeedListView.setAdapter(mAdapter);

		return view;
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

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		// Hector: go to details from here
	}

}
