package com.cloudjay.cjay;

import java.util.ArrayList;
import java.util.Calendar;

import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.ami.fundapter.BindDictionary;
import com.ami.fundapter.FunDapter;
import com.ami.fundapter.extractors.StringExtractor;
import com.ami.fundapter.interfaces.DynamicImageLoader;
import com.ami.fundapter.interfaces.ItemClickListener;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.util.DataCenter;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.ItemClick;
import com.googlecode.androidannotations.annotations.ViewById;

//slide 15

@EActivity(R.layout.activity_auditor_container)
public class AuditorContainerActivity extends SherlockFragmentActivity {

	private final static String TAG = "AuditorContainerActivity";
	private ArrayList<ContainerSession> mFeeds;
	
	@ViewById(R.id.btn_add_new) Button mAddButton;
	@ViewById(R.id.feeds) ListView mFeedListView;
	
	@AfterViews
	void AfterViews() {
		mFeeds = (ArrayList<ContainerSession>) DataCenter.getInstance().getListContainerSessions(this);
		initFunDapter(mFeeds);
	}
	
	@ItemClick(R.id.feeds)
	void onItemClick(int position) {
		// Hector: go to details from here
		android.util.Log.d(TAG, "Show item at position: " + position);
	}
	
	private void initFunDapter(ArrayList<ContainerSession> containers) {
		BindDictionary<ContainerSession> feedsDict = new BindDictionary<ContainerSession>();
		feedsDict.addStringField(R.id.feed_item_container_id,
				new StringExtractor<ContainerSession>() {
					@Override
					public String getStringValue(ContainerSession item,	int position) {	return item.getContainerId(); }
				});
		feedsDict.addStringField(R.id.feed_item_container_owner,
				new StringExtractor<ContainerSession>() {
					@Override
					public String getStringValue(ContainerSession item, int position) {	return item.getOperatorName(); }
				});
		feedsDict.addStringField(R.id.feed_item_container_import_date,
				new StringExtractor<ContainerSession>() {
					@Override
					public String getStringValue(ContainerSession item, int position) {
						return java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
					}
				});
		feedsDict.addDynamicImageField(R.id.feed_item_picture,
				new StringExtractor<ContainerSession>() {
					@Override
					public String getStringValue(ContainerSession item, int position) {	return item.getContainerId(); }
				}, new DynamicImageLoader() {
					@Override
					public void loadImage(String stringColor, ImageView view) {	view.setImageResource(R.drawable.ic_logo); }
				}).onClick(new ItemClickListener<ContainerSession>() {
						@Override
						public void onClick(ContainerSession item, int position, View view) {
							// TODO Auto-generated method stub
						}
		});
		FunDapter<ContainerSession> adapter = new FunDapter<ContainerSession>(this, containers, R.layout.list_item_damage, feedsDict);
		mFeedListView.setAdapter(adapter);
	}
}
