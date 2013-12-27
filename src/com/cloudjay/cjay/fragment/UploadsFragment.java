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

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.cloudjay.cjay.PhotoUploadController;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.adapter.UploadsListBaseAdapter;
import com.cloudjay.cjay.events.PhotoSelectionRemovedEvent;
import com.cloudjay.cjay.model.GateReportImage;
import com.cloudjay.cjay.model.TmpContainerSession;
import com.example.android.swipedismiss.SwipeDismissListViewTouchListener;
import com.example.android.swipedismiss.SwipeDismissListViewTouchListener.OnDismissCallback;

import de.greenrobot.event.EventBus;

public class UploadsFragment extends SherlockFragment implements
		OnDismissCallback, OnItemClickListener {

	private PhotoUploadController mPhotoSelectionController;
	private UploadsListBaseAdapter mAdapter;

	private ProgressDialog mOpeningFacebookDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EventBus.getDefault().register(this);

		mAdapter = new UploadsListBaseAdapter(getActivity());
		mPhotoSelectionController = PhotoUploadController
				.getFromContext(getActivity());
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
		closeFacebookProgressDialog();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}

	private void closeFacebookProgressDialog() {
		if (null != mOpeningFacebookDialog) {
			mOpeningFacebookDialog.dismiss();
			mOpeningFacebookDialog = null;
		}
	}

	// private void openFacebookProgressDialog() {
	// closeFacebookProgressDialog();
	//
	// mOpeningFacebookDialog = new ProgressDialog(getActivity());
	// mOpeningFacebookDialog.setMessage(getString(R.string.opening_app));
	// mOpeningFacebookDialog.show();
	// }

	public void onItemClick(AdapterView<?> l, View view, int position, long id) {
		TmpContainerSession upload = (TmpContainerSession) l
				.getItemAtPosition(position);
		if (null != upload
				&& upload.getUploadState() == TmpContainerSession.STATE_UPLOAD_COMPLETED) {

			// String postId = upload.getResultPostId();
			// if (null != postId) {
			// final Intent intent = new Intent(Intent.ACTION_VIEW);
			//
			// try {
			// intent.setData(Uri.parse("fb://post/" + postId));
			// startActivity(intent);
			// // openFacebookProgressDialog();
			// return;
			// } catch (Exception e) {
			// // Facebook not installed
			// }
			//
			// try {
			// intent.setData(Uri.parse("fplusfree://post?id=" + postId));
			// startActivity(intent);
			// return;
			// } catch (Exception e) {
			// // Friendcaster Free not installed
			// }
			//
			// try {
			// intent.setData(Uri.parse("fplus://post?id=" + postId));
			// startActivity(intent);
			// return;
			// } catch (Exception e) {
			// // Friendcaster Pro not installed
			// }
			// }
		}
	}

	public void onEvent(PhotoSelectionRemovedEvent event) {
		mAdapter.notifyDataSetChanged();
	}

	public void onDismiss(AbsListView listView, int[] reverseSortedPositions) {
		try {
			for (int i = 0, z = reverseSortedPositions.length; i < z; i++) {
				GateReportImage upload = (GateReportImage) listView
						.getItemAtPosition(reverseSortedPositions[i]);
				mPhotoSelectionController.removeUpload(upload);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		mAdapter.notifyDataSetChanged();
	}

	public boolean canDismiss(AbsListView listView, int position) {
		try {
			// TODO:
			// GateReportImage upload = (GateReportImage) listView
			// .getItemAtPosition(position);
			// return upload.getUploadState() !=
			// GateReportImage.STATE_UPLOAD_IN_PROGRESS;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

}
