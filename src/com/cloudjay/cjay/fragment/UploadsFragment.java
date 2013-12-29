package com.cloudjay.cjay.fragment;

import java.sql.SQLException;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.adapter.UploadsListBaseAdapter;
import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.events.ContainerSessionAddedEvent;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.network.CJayClient;
import com.cloudjay.cjay.util.Logger;
import com.example.android.swipedismiss.SwipeDismissListViewTouchListener;
import com.example.android.swipedismiss.SwipeDismissListViewTouchListener.OnDismissCallback;
import de.greenrobot.event.EventBus;

public class UploadsFragment extends SherlockFragment implements
		OnDismissCallback, OnItemClickListener {

	private static final String LOG_TAG = "UploadsFragment";

	private UploadsListBaseAdapter mAdapter;
	ContainerSessionDaoImpl containerSessionDaoImpl = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EventBus.getDefault().register(this);

		try {
			if (null == containerSessionDaoImpl)
				containerSessionDaoImpl = CJayClient.getInstance()
						.getDatabaseManager().getHelper(getActivity())
						.getContainerSessionDaoImpl();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		mAdapter = new UploadsListBaseAdapter(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_uploads, container,
				false);

		ListView listView = (ListView) view.findViewById(android.R.id.list);
		SwipeDismissListViewTouchListener swipeListener = new SwipeDismissListViewTouchListener(
				listView, this);

		listView.setOnItemClickListener(this);
		listView.setOnTouchListener(swipeListener);
		listView.setOnScrollListener(swipeListener.makeScrollListener());
		listView.setSelector(R.drawable.selectable_background_photup);
		listView.setAdapter(mAdapter);
		listView.setEmptyView(view.findViewById(android.R.id.empty));

		return view;
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}

	public void onItemClick(AdapterView<?> l, View view, int position, long id) {
		Logger.Log(LOG_TAG,
				"onItemClick at index: " + Integer.toString(position));
	}

	public void onEvent(ContainerSessionAddedEvent event) {
		Logger.Log(LOG_TAG, "onEvent ContainerSessionAddedEvent");
		mAdapter.notifyDataSetChanged();
	}

	public void onDismiss(AbsListView listView, int[] reverseSortedPositions) {
		Logger.Log(LOG_TAG, "onSwipeDismiss");

		// set item Cleared = true then notifyDataSetChanged
		try {
			for (int i = 0, z = reverseSortedPositions.length; i < z; i++) {
				ContainerSession item = (ContainerSession) listView
						.getItemAtPosition(reverseSortedPositions[i]);

				// remove from Upload Fragment
				item.setCleared(true);
				containerSessionDaoImpl.update(item);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		mAdapter.notifyDataSetChanged();
	}

	public boolean canDismiss(AbsListView listView, int position) {
		try {
			ContainerSession upload = (ContainerSession) listView
					.getItemAtPosition(position);
			return upload.getUploadState() != ContainerSession.STATE_UPLOAD_IN_PROGRESS;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
