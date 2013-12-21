package com.cloudjay.cjay.fragment;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.ami.fundapter.BindDictionary;
import com.ami.fundapter.FunDapter;
import com.ami.fundapter.extractors.StringExtractor;
import com.ami.fundapter.interfaces.StaticImageLoader;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.Container;
import com.googlecode.androidannotations.annotations.EFragment;
import com.j256.ormlite.logger.Log;

@EFragment(R.layout.fragment_feeds)
public class FeedListFragment extends SherlockDialogFragment implements OnItemClickListener {
	
	private final static String TAG = "FeedListFragment";
	
	private ListView mFeedListView;
	private ArrayList<Container> mFeeds;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Hector: test only
		mFeeds = new ArrayList<Container>();
		for (int i = 0; i < 100; i++) {
			Container container = new Container();
			container.setContainerId("6280541");
			container.setOwnerName("CBHU");
			mFeeds.add(container);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_feeds, container, false);

		mFeedListView = (ListView) view.findViewById(R.id.feeds);

		mFeedListView.setOnItemClickListener(this);
		initFunDapter(mFeeds);

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
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		// Hector: go to details from here
		android.util.Log.d(TAG, "Show item at position: " + position);
	}

	private void initFunDapter(ArrayList<Container> containers) {

		BindDictionary<Container> feedsDict = new BindDictionary<Container>();
		feedsDict.addStringField(R.id.feed_item_container_id,
				new StringExtractor<Container>() {

					@Override
					public String getStringValue(Container item, int position) {
						return item.getContainerId();
					}
				});

		feedsDict.addStringField(R.id.feed_item_container_owner,
				new StringExtractor<Container>() {

					@Override
					public String getStringValue(Container item, int position) {
						// TODO Auto-generated method stub
						return item.getOwnerName();
					}
				});

		feedsDict.addStringField(R.id.feed_item_container_import_date, 
				new StringExtractor<Container>() {
			@Override
			public String getStringValue(Container item, int position) {
				// TODO Auto-generated method stub
				return java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
			}
		});
		
		feedsDict.addStaticImageField(R.id.feed_item_picture, new StaticImageLoader<Container>() {

			@Override
			public void loadImage(Container item, ImageView imageView,
					int position) {
				// TODO Auto-generated method stub
				
			}
		});

		FunDapter<Container> adapter = new FunDapter<Container>(getActivity(),
				containers, R.layout.feed_item, feedsDict);

		mFeedListView.setAdapter(adapter);

	}

}
