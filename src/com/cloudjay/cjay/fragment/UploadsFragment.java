/*
 * Copyright 2013 Chris Banes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloudjay.cjay.fragment;

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
import com.cloudjay.cjay.events.ContainerSessionAddedEvent;
import com.cloudjay.cjay.model.ContainerSession;
import com.example.android.swipedismiss.SwipeDismissListViewTouchListener;
import com.example.android.swipedismiss.SwipeDismissListViewTouchListener.OnDismissCallback;
import de.greenrobot.event.EventBus;

public class UploadsFragment extends SherlockFragment implements
		OnDismissCallback, OnItemClickListener {

	private UploadsListBaseAdapter mAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EventBus.getDefault().register(this);

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

	}

	public void onEvent(ContainerSessionAddedEvent event) {
		mAdapter.notifyDataSetChanged();
	}

	public void onDismiss(AbsListView listView, int[] reverseSortedPositions) {
		try {
			for (int i = 0, z = reverseSortedPositions.length; i < z; i++) {
				ContainerSession item = (ContainerSession) listView
						.getItemAtPosition(reverseSortedPositions[i]);

				// remove from Upload Fragment
				item.setCleared(true);
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
