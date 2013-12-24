package com.cloudjay.cjay.fragment;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.ami.fundapter.BindDictionary;
import com.ami.fundapter.FunDapter;
import com.ami.fundapter.extractors.StringExtractor;
import com.ami.fundapter.interfaces.StaticImageLoader;
import com.cloudjay.cjay.CameraActivity_;
import com.cloudjay.cjay.R;

import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.util.DataCenter;
import com.googlecode.androidannotations.annotations.EFragment;

@EFragment(R.layout.fragment_feeds)
public class FeedListFragment extends SherlockDialogFragment implements
		OnClickListener, OnItemClickListener {

	private final static String TAG = "FeedListFragment";

	private Button mAddNewBtn;

	private ListView mFeedListView;

	private ArrayList<ContainerSession> mFeeds;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Hector: test only
		mFeeds = (ArrayList<ContainerSession>) DataCenter.getInstance()
				.getListContainerSessions(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_feeds, container, false);

		mAddNewBtn = (Button) view.findViewById(R.id.btn_add_new);
		mAddNewBtn.setOnClickListener(this);

		mFeedListView = (ListView) view.findViewById(R.id.feeds);
		mFeedListView.setOnItemClickListener(this);
		initFunDapter(mFeeds);

		return view;
	}

	@Override
	public void onClick(View view) {
		// Show Dialog
		LayoutInflater factory = LayoutInflater.from(getActivity());
		final View newContainerView = factory.inflate(
				R.layout.dialog_new_container, null);
		
		EditText newContainerIdEditText = (EditText) view.findViewById(R.id.dialog_new_container_id);
		Spinner newContainerOwnerSpinner = (Spinner) view.findViewById(R.id.dialog_new_container_owner);
		
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
		        R.array.dialog_container_owner_list, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		newContainerOwnerSpinner.setAdapter(adapter);

		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(
				getActivity())
				.setTitle(getString(R.string.dialog_new_container))
				.setView(newContainerView)
				.setPositiveButton(R.string.dialog_container_ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								Intent intent = new Intent(getActivity(),
										CameraActivity_.class);
								startActivity(intent);
							}
						})
				.setNegativeButton(R.string.dialog_container_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {

							}
						});
		dialogBuilder.create().show();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// Hector: go to details from here
		android.util.Log.d(TAG, "Show item at position: " + position);
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
						// TODO Auto-generated method stub
						return item.getOperatorName();
					}
				});

		feedsDict.addStringField(R.id.feed_item_container_import_date,
				new StringExtractor<ContainerSession>() {
					@Override
					public String getStringValue(ContainerSession item,
							int position) {
						// TODO Auto-generated method stub
						return java.text.DateFormat.getDateTimeInstance()
								.format(Calendar.getInstance().getTime());
					}
				});

		feedsDict.addStaticImageField(R.id.feed_item_picture,
				new StaticImageLoader<ContainerSession>() {

					@Override
					public void loadImage(ContainerSession item,
							ImageView imageView, int position) {
						// TODO Auto-generated method stub

					}
				});

		FunDapter<ContainerSession> adapter = new FunDapter<ContainerSession>(
				getActivity(), containers, R.layout.list_item_container,
				feedsDict);

		mFeedListView.setAdapter(adapter);

	}
}
