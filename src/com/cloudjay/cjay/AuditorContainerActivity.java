package com.cloudjay.cjay;

import java.util.ArrayList;

import android.widget.Button;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.ami.fundapter.BindDictionary;
import com.ami.fundapter.FunDapter;
import com.ami.fundapter.extractors.StringExtractor;
import com.cloudjay.cjay.model.CJayImage;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.ItemClick;
import com.googlecode.androidannotations.annotations.ViewById;

//slide 15

@EActivity(R.layout.activity_auditor_container)
public class AuditorContainerActivity extends SherlockFragmentActivity {

	private final static String TAG = "AuditorContainerActivity";
	private ArrayList<CJayImage> mFeeds;

	@ViewById(R.id.btn_add_new)
	Button mAddButton;
	@ViewById(R.id.feeds)
	ListView mFeedListView;

	@AfterViews
	void afterViews() {
//		mFeeds = (ArrayList<ContainerSession>) DataCenter.getInstance()
//				.getListContainerSessions(this);
		initImageFeedAdapter(mFeeds);
	}

	@ItemClick(R.id.feeds)
	void containerItemClicked(int position) {
		// Hector: go to details from here
		android.util.Log.d(TAG, "Show item at position: " + position);
	}

	private void initImageFeedAdapter(ArrayList<CJayImage> containers) {
		BindDictionary<CJayImage> feedsDict = new BindDictionary<CJayImage>();
		feedsDict.addStringField(R.id.issue_location_code,
				new StringExtractor<CJayImage>() {
					@Override
					public String getStringValue(CJayImage item,
							int position) {
						return item.getIssue().getLocationCode();
					}
				});
		feedsDict.addStringField(R.id.issue_damage_code,
				new StringExtractor<CJayImage>() {
					@Override
					public String getStringValue(CJayImage item,
							int position) {
						return item.getIssue().getDamageCode().getCode();
					}
				});
		feedsDict.addStringField(R.id.issue_repair_code,
				new StringExtractor<CJayImage>() {
					@Override
					public String getStringValue(CJayImage item,
							int position) {
						return item.getIssue().getRepairCode().getCode();
					}
				});
		feedsDict.addStringField(R.id.issue_component_code,
				new StringExtractor<CJayImage>() {
					@Override
					public String getStringValue(CJayImage item,
							int position) {
//						return item.getIssue().getDamageCode().getCode();
						// TODO
						return "";
					}
				});
		feedsDict.addStringField(R.id.issue_quantity,
				new StringExtractor<CJayImage>() {
					@Override
					public String getStringValue(CJayImage item,
							int position) {
						return String.valueOf(item.getIssue().getQuantity());
					}
				});
		feedsDict.addStringField(R.id.issue_dimension,
				new StringExtractor<CJayImage>() {
					@Override
					public String getStringValue(CJayImage item,
							int position) {
						return new StringBuilder()
							.append(getResources().getString(R.string.label_issue_length))
							.append(item.getIssue().getLength())
							.append(getResources().getString(R.string.label_issue_height))
							.append(item.getIssue().getHeight())							
							.toString();
					}
				});
//		feedsDict.addDynamicImageField(R.id.feed_item_picture,
//				new StringExtractor<ContainerSession>() {
//					@Override
//					public String getStringValue(ContainerSession item,
//							int position) {
//						return item.getFullContainerId();
//					}
//				}, new DynamicImageLoader() {
//					@Override
//					public void loadImage(String stringColor, ImageView view) {
//						view.setImageResource(R.drawable.ic_logo);
//					}
//				}).onClick(new ItemClickListener<ContainerSession>() {
//			@Override
//			public void onClick(ContainerSession item, int position, View view) {
//
//			}
//		});
		FunDapter<CJayImage> adapter = new FunDapter<CJayImage>(
				this, containers, R.layout.list_item_issue, feedsDict);
		mFeedListView.setAdapter(adapter);
	}
}
