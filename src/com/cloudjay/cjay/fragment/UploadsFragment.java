package com.cloudjay.cjay.fragment;

import java.sql.SQLException;
import java.util.List;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;

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
import com.cloudjay.cjay.events.ContainerSessionEnqueueEvent;
import com.cloudjay.cjay.events.UploadStateChangedEvent;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.network.CJayClient;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.Logger;
import com.example.android.swipedismiss.SwipeDismissListViewTouchListener;
import com.example.android.swipedismiss.SwipeDismissListViewTouchListener.OnDismissCallback;

import de.greenrobot.event.EventBus;

@EFragment
@OptionsMenu(R.menu.menu_upload)
public class UploadsFragment extends SherlockFragment implements
		OnDismissCallback, OnItemClickListener {

	UploadsListBaseAdapter mAdapter;
	ContainerSessionDaoImpl containerSessionDaoImpl = null;
	List<ContainerSession> listContainerSessions = null;

	public UploadsFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		EventBus.getDefault().register(this);
		super.onCreate(savedInstanceState);

		try {
			if (null == containerSessionDaoImpl)
				containerSessionDaoImpl = CJayClient.getInstance()
						.getDatabaseManager().getHelper(getActivity())
						.getContainerSessionDaoImpl();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		listContainerSessions = DataCenter.getInstance()
				.getListUploadContainerSessions(getActivity());

		mAdapter = new UploadsListBaseAdapter(getActivity(),
				listContainerSessions);
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
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}

	public void onItemClick(AdapterView<?> l, View view, int position, long id) {
		Logger.Log("onItemClick at index: " + Integer.toString(position));
	}

	public void onEvent(ContainerSessionEnqueueEvent event) {
		Logger.Log("onEvent ContainerSessionEnqueueEvent");
		updateUI();
	}

	public void onEvent(UploadStateChangedEvent event) {
		Logger.Log("onEvent UploadStateChangedEvent");
		Logger.e("Refrest UploadsFragment UI");
		updateUI();
	}

	@UiThread
	void updateUI() {

		listContainerSessions = DataCenter.getInstance()
				.getListUploadContainerSessions(getActivity());

		mAdapter.setContainerSessions(listContainerSessions);
		mAdapter.notifyDataSetChanged();
	}

	@OptionsItem(R.id.menu_clear_uploaded)
	void clearUploadsMenuItemSelected() {
		Logger.Log("Menu clear upload items clicked");

		for (ContainerSession containerSession : listContainerSessions) {
			try {

				// just clear items from UI
				containerSession.setCleared(true);
				containerSessionDaoImpl.update(containerSession);

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		DataCenter.getDatabaseHelper(getActivity()).addUsageLog(
				"Clear list #upload");

		updateUI();
	}

	public void onDismiss(AbsListView listView, int[] reverseSortedPositions) {
		Logger.Log("onSwipeDismiss");

		// set item Cleared = true then call updateUI()
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

		updateUI();
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
