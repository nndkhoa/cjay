package com.cloudjay.cjay.fragment;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.Trace;
import org.androidannotations.annotations.ViewById;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.ami.fundapter.BindDictionary;
import com.ami.fundapter.FunDapter;
import com.ami.fundapter.extractors.StringExtractor;
import com.ami.fundapter.interfaces.DynamicImageLoader;
import com.cloudjay.cjay.CameraActivity;
import com.cloudjay.cjay.CameraActivity_;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.dao.CJayImageDaoImpl;
import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.dao.IssueDaoImpl;
import com.cloudjay.cjay.events.CJayImageAddedEvent;
import com.cloudjay.cjay.events.ContainerSessionChangedEvent;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.Issue;
import com.cloudjay.cjay.network.CJayClient;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;

import de.greenrobot.event.EventBus;

@EFragment(R.layout.fragment_repair_issue_pending)
public class RepairIssuePendingListFragment extends SherlockFragment {

	private final String LOG_TAG = "RepairPendingIssueListFragment";
	private ArrayList<Issue> mFeeds;
	private FunDapter<Issue> mFeedsAdapter;

	private ContainerSession mContainerSession;
	private String mContainerSessionUUID;
	private Issue mSelectedIssue;
	private ImageLoader imageLoader;
	private ArrayList<CJayImage> mTakenImages;

	IssueDaoImpl issueDaoImpl;
	CJayImageDaoImpl cJayImageDaoImpl;

	@ViewById(R.id.feeds)
	ListView mFeedListView;

	@ViewById(android.R.id.empty)
	FrameLayout emptyElement;

	@AfterViews
	void afterViews() {
		try {
			issueDaoImpl = CJayClient.getInstance().getDatabaseManager().getHelper(getActivity()).getIssueDaoImpl();
			cJayImageDaoImpl = CJayClient.getInstance().getDatabaseManager().getHelper(getActivity())
											.getCJayImageDaoImpl();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		mFeedListView.setEmptyView(emptyElement);
		imageLoader = ImageLoader.getInstance();
		initIssueFeedAdapter(null);
		mSelectedIssue = null;
	}

	@Deprecated
	@Click(R.id.btn_add_new)
	void cameraClicked() {
		Logger.Log("cameraClicked()");

		if (mTakenImages == null) {
			Logger.Log("mTakenImages is NULL. Init mTakenImages");
			mTakenImages = new ArrayList<CJayImage>();
		}

		Intent intent = new Intent(getActivity(), CameraActivity_.class);
		intent.putExtra(CameraActivity.CJAY_CONTAINER_SESSION_EXTRA, mContainerSession.getUuid());
		intent.putExtra("type", CJayImage.TYPE_REPORT);
		intent.putExtra("tag", LOG_TAG);
		startActivity(intent);
	}

	/**
	 * Click item --> chụp hình issue sau sửa chữa
	 * 
	 * @param position
	 */
	@ItemClick(R.id.feeds)
	@Trace(level = Log.WARN)
	void imageItemClicked(int position) {
		mSelectedIssue = mFeedsAdapter.getItem(position);
		mFeedListView.setItemChecked(-1, true);

		// Open Camera
		if (mTakenImages == null) {
			Logger.Log("mTakenImages is NULL. Init mTakenImages");
			mTakenImages = new ArrayList<CJayImage>();
		}

		Intent intent = new Intent(getActivity(), CameraActivity_.class);
		intent.putExtra(CameraActivity.CJAY_CONTAINER_SESSION_EXTRA, mContainerSession.getUuid());
		intent.putExtra("type", CJayImage.TYPE_REPAIRED);
		intent.putExtra("tag", LOG_TAG);
		startActivity(intent);
	}

	private void initIssueFeedAdapter(ArrayList<Issue> containers) {
		BindDictionary<Issue> feedsDict = new BindDictionary<Issue>();
		feedsDict.addStringField(R.id.issue_location_code, new StringExtractor<Issue>() {
			@Override
			public String getStringValue(Issue item, int position) {
				return Utils.replaceNullBySpace(item.getLocationCode());
			}
		});
		feedsDict.addStringField(R.id.issue_damage_code, new StringExtractor<Issue>() {
			@Override
			public String getStringValue(Issue item, int position) {
				return Utils.replaceNullBySpace(item.getDamageCodeString());
			}
		});
		feedsDict.addStringField(R.id.issue_repair_code, new StringExtractor<Issue>() {
			@Override
			public String getStringValue(Issue item, int position) {
				return Utils.replaceNullBySpace(item.getRepairCodeString());
			}
		});
		feedsDict.addStringField(R.id.issue_component_code, new StringExtractor<Issue>() {
			@Override
			public String getStringValue(Issue item, int position) {
				return Utils.replaceNullBySpace(item.getComponentCodeString());
			}
		});
		feedsDict.addStringField(R.id.issue_quantity, new StringExtractor<Issue>() {
			@Override
			public String getStringValue(Issue item, int position) {
				return Utils.replaceNullBySpace(item.getQuantity());
			}
		});
		feedsDict.addStringField(R.id.issue_length, new StringExtractor<Issue>() {
			@Override
			public String getStringValue(Issue item, int position) {
				return Utils.replaceNullBySpace(item.getLength());
			}
		});
		feedsDict.addStringField(R.id.issue_height, new StringExtractor<Issue>() {
			@Override
			public String getStringValue(Issue item, int position) {
				return Utils.replaceNullBySpace(item.getHeight());
			}
		});
		feedsDict.addDynamicImageField(R.id.issue_picture, new StringExtractor<Issue>() {
			@Override
			public String getStringValue(Issue item, int position) {
				Collection<CJayImage> cJayImages = item.getCJayImages();
				if (null != cJayImages) {
					for (CJayImage cJayImage : cJayImages) {
						if (!TextUtils.isEmpty(cJayImage.getUri())) return Utils.stripNull(cJayImage.getUri());
					}
				}
				return Utils.stripNull(null);
			}
		}, new DynamicImageLoader() {
			@Override
			public void loadImage(String url, ImageView view) {
				if (!TextUtils.isEmpty(url)) {
					imageLoader.displayImage(url, view);
				} else {
					view.setImageResource(R.drawable.ic_app);
				}
			}
		});
		mFeedsAdapter = new FunDapter<Issue>(getActivity(), containers, R.layout.list_item_issue, feedsDict);
		mFeedListView.setAdapter(mFeedsAdapter);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		EventBus.getDefault().register(this);
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onDestroy() {
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}

	public void onEvent(CJayImageAddedEvent event) {

		if (event == null) {
			Logger.Log("Event is null");
		} else {

			if (mTakenImages == null) {
				Logger.Log("mTakenImages is NULL");
				mTakenImages = new ArrayList<CJayImage>();
			}

			try {
				// retrieve image
				Logger.Log("onEvent CJayImageAddedEvent");
				if (event.getTag().equals(LOG_TAG)) {
					mTakenImages.add(event.getCJayImage());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public void onEventMainThread(ContainerSessionChangedEvent event) {
		Logger.Log("onEvent ContainerSessionChangedEvent");
		refresh();
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mTakenImages != null && mTakenImages.size() > 0) {

			// Update list cjay images of selected Issue
			try {
				for (CJayImage cJayImage : mTakenImages) {
					cJayImage.setIssue(mSelectedIssue);
					cJayImage.setContainerSession(mContainerSession);
					cJayImageDaoImpl.createOrUpdate(cJayImage);
				}

				mSelectedIssue.setFixed(true);
				issueDaoImpl.createOrUpdate(mSelectedIssue);

				refresh();

			} catch (SQLException e) {
				e.printStackTrace();
			}

			mTakenImages.clear();
			mTakenImages = null;

		} else {
			refresh();
		}
	}

	private void populateIssueList() {
		mFeeds = new ArrayList<Issue>();
		try {
			ContainerSessionDaoImpl containerSessionDaoImpl = CJayClient.getInstance().getDatabaseManager()
																		.getHelper(getActivity())
																		.getContainerSessionDaoImpl();
			mContainerSession = containerSessionDaoImpl.queryForId(mContainerSessionUUID);

			if (null != mContainerSession) {
				for (Issue issue : mContainerSession.getIssues()) {
					if (!issue.isFixed()) {
						mFeeds.add(issue);
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void refresh() {
		populateIssueList();
		mFeedsAdapter.updateData(mFeeds);
	}

	public void setContainerSessionUUID(String containerSessionUUID) {
		mContainerSessionUUID = containerSessionUUID;
	}
}
