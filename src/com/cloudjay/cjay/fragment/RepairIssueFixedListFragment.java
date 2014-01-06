package com.cloudjay.cjay.fragment;

import java.sql.SQLException;
import java.util.ArrayList;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ViewById;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.ami.fundapter.BindDictionary;
import com.ami.fundapter.FunDapter;
import com.ami.fundapter.extractors.StringExtractor;
import com.ami.fundapter.interfaces.DynamicImageLoader;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.RepairIssueReportActivity_;
import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.events.DataLoadedEvent;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.Issue;
import com.cloudjay.cjay.network.CJayClient;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;

import de.greenrobot.event.EventBus;

@EFragment(R.layout.fragment_repair_issue_fixed)
public class RepairIssueFixedListFragment extends SherlockFragment {

	private final static String LOG_TAG = "RepairIssueFixedListFragment";
	
	private ArrayList<Issue> mFeeds;
	private FunDapter<Issue> mFeedsAdapter;
	private ContainerSession mContainerSession;
	private String mContainerSessionUUID;
	private Issue mSelectedIssue;
	private ImageLoader imageLoader;

	@ViewById(R.id.feeds)
	ListView mFeedListView;

	@AfterViews
	void afterViews() {
		imageLoader = ImageLoader.getInstance();
		
		initIssueFeedAdapter(null);
	}

	@ItemClick(R.id.feeds)
	void imageItemClicked(int position) {
		mSelectedIssue = mFeedsAdapter.getItem(position);

		// show issue report activity
		Intent intent = new Intent(getActivity(),
				RepairIssueReportActivity_.class);
		intent.putExtra(RepairIssueReportActivity_.CJAY_ISSUE_EXTRA,
				mSelectedIssue.getUUID());
		startActivity(intent);
	}

	@Override
	public void onResume() {
		if (mFeedsAdapter != null) {
			refresh();
		}
		super.onResume();
	}

	@Override
	public void onDestroy() {
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		EventBus.getDefault().register(this);
		super.onCreate(savedInstanceState);
	}

	public void setContainerSessionUUID(String containerSessionUUID) {
		mContainerSessionUUID = containerSessionUUID;
	}

	public void refresh() {
		populateIssueList();
		mFeedsAdapter.updateData(mFeeds);
	}

	private void populateIssueList() {
		mFeeds = new ArrayList<Issue>();
		try {
			ContainerSessionDaoImpl containerSessionDaoImpl = CJayClient
					.getInstance().getDatabaseManager()
					.getHelper(getActivity()).getContainerSessionDaoImpl();
			mContainerSession = containerSessionDaoImpl
					.queryForId(mContainerSessionUUID);

			if (null != mContainerSession) {
				for (Issue issue : mContainerSession.getIssues()) {
					if (issue.isFixed()) {
						mFeeds.add(issue);
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void initIssueFeedAdapter(ArrayList<Issue> containers) {
		BindDictionary<Issue> feedsDict = new BindDictionary<Issue>();
		feedsDict.addStringField(R.id.issue_location_code,
				new StringExtractor<Issue>() {
					@Override
					public String getStringValue(Issue item, int position) {
						return Utils.stripNull(item.getLocationCode());
					}
				});
		feedsDict.addStringField(R.id.issue_damage_code,
				new StringExtractor<Issue>() {
					@Override
					public String getStringValue(Issue item, int position) {
						return Utils.stripNull(item.getDamageCodeString());
					}
				});
		feedsDict.addStringField(R.id.issue_repair_code,
				new StringExtractor<Issue>() {
					@Override
					public String getStringValue(Issue item, int position) {
						return Utils.stripNull(item.getRepairCodeString());
					}
				});
		feedsDict.addStringField(R.id.issue_component_code,
				new StringExtractor<Issue>() {
					@Override
					public String getStringValue(Issue item, int position) {
						return Utils.stripNull(item.getComponentCodeString());
					}
				});
		feedsDict.addStringField(R.id.issue_quantity,
				new StringExtractor<Issue>() {
					@Override
					public String getStringValue(Issue item, int position) {
						return Utils.stripNull(item.getQuantity());
					}
				});
		feedsDict.addStringField(R.id.issue_length,
				new StringExtractor<Issue>() {
					@Override
					public String getStringValue(Issue item, int position) {
						return Utils.stripNull(item.getLength());
					}
				});
		feedsDict.addStringField(R.id.issue_height,
				new StringExtractor<Issue>() {
					@Override
					public String getStringValue(Issue item, int position) {
						return Utils.stripNull(item.getHeight());
					}
				});
		feedsDict.addDynamicImageField(R.id.issue_picture,
				new StringExtractor<Issue>() {
					@Override
					public String getStringValue(Issue item, int position) {
						for (CJayImage cJayImage : item.getCJayImages()) {
							if (!TextUtils.isEmpty(cJayImage.getUri())) {
								return cJayImage.getUri();
							}
						}
						return null;
					}
				}, new DynamicImageLoader() {
					@Override
					public void loadImage(String url, ImageView view) {
						if (url != null && !TextUtils.isEmpty(url)) {
							imageLoader.displayImage(url, view);
						} else {
							view.setImageResource(R.drawable.ic_app);
						}
					}
				});
		mFeedsAdapter = new FunDapter<Issue>(getActivity(), containers,
				R.layout.list_item_issue, feedsDict);
		mFeedListView.setAdapter(mFeedsAdapter);
	}

	public void onEventMainThread(DataLoadedEvent event) {
		Logger.Log(LOG_TAG, "onEvent DataLoadedEvent");
		refresh();
	}
}
