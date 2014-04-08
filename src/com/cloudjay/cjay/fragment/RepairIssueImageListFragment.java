package com.cloudjay.cjay.fragment;

import java.sql.SQLException;
import java.util.ArrayList;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.actionbarsherlock.app.SherlockFragment;
import com.ami.fundapter.BindDictionary;
import com.ami.fundapter.FunDapter;
import com.ami.fundapter.extractors.StringExtractor;
import com.ami.fundapter.interfaces.DynamicImageLoader;
import com.cloudjay.cjay.CameraActivity;
import com.cloudjay.cjay.CameraActivity_;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.dao.CJayImageDaoImpl;
import com.cloudjay.cjay.dao.IssueDaoImpl;
import com.cloudjay.cjay.events.CJayImageAddedEvent;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.Issue;
import com.cloudjay.cjay.network.CJayClient;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;

import de.greenrobot.event.EventBus;

@EFragment(R.layout.fragment_repair_issue_image_list)
public class RepairIssueImageListFragment extends SherlockFragment {

	private final String LOG_TAG = "RepairIssueImageListFragment";

	private ArrayList<CJayImage> mFeeds;
	private ArrayList<CJayImage> mTakenImages;
	private FunDapter<CJayImage> mFeedsAdapter;
	private Issue mIssue;
	private String mIssueUUID;
	private int mType;
	private ImageLoader imageLoader;

	@ViewById(R.id.feeds)
	ListView mFeedListView;
	@ViewById(R.id.btn_add_new)
	ImageButton mCameraButton;

	@AfterViews
	void afterViews() {
		// show or hide camera button
		if (mType == CJayImage.TYPE_REPORT) {
			RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) mCameraButton.getLayoutParams();
			p.height = 0;
			mCameraButton.setLayoutParams(p);
		}

		imageLoader = ImageLoader.getInstance();

		initImageFeedAdapter(null);
	}

	@Click(R.id.btn_add_new)
	void cameraClicked() {
		if (mTakenImages == null) {
			mTakenImages = new ArrayList<CJayImage>();
		}

		Intent intent = new Intent(getActivity(), CameraActivity_.class);
		intent.putExtra(CameraActivity.CJAY_CONTAINER_SESSION_EXTRA, mIssue.getContainerSession().getUuid());
		intent.putExtra("type", CJayImage.TYPE_REPAIRED);
		intent.putExtra("tag", LOG_TAG);
		startActivity(intent);
	}

	private void initImageFeedAdapter(ArrayList<CJayImage> containers) {
		BindDictionary<CJayImage> feedsDict = new BindDictionary<CJayImage>();
		feedsDict.addDynamicImageField(R.id.item_picture, new StringExtractor<CJayImage>() {
			@Override
			public String getStringValue(CJayImage item, int position) {
				return Utils.stripNull(item.getUri());
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
		mFeedsAdapter = new FunDapter<CJayImage>(getActivity(), containers, R.layout.list_item_image, feedsDict);
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
			// retrieve image
			try {
				if (event.getTag().equals(LOG_TAG) && mTakenImages != null) {
					mTakenImages.add(event.getCJayImage());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public void onResume() {

		// update new images and database

		Logger.Log("onResume");
		if (mTakenImages != null && mTakenImages.size() > 0) {
			try {
				CJayImageDaoImpl cJayImageDaoImpl = CJayClient.getInstance().getDatabaseManager()
																.getHelper(getActivity()).getCJayImageDaoImpl();

				for (CJayImage cJayImage : mTakenImages) {
					cJayImageDaoImpl.refresh(cJayImage);
					cJayImage.setIssue(mIssue);
					cJayImage.setContainerSession(mIssue.getContainerSession());
					cJayImageDaoImpl.update(cJayImage);
				}

				// refresh menu
				getActivity().supportInvalidateOptionsMenu();

			} catch (SQLException e) {
				e.printStackTrace();
			}

			mTakenImages.clear();
			mTakenImages = null;
		}

		// refresh list
		if (mFeedsAdapter != null) {
			refresh();
		}
		super.onResume();
	}

	private void populateCJayImages() {
		mFeeds = new ArrayList<CJayImage>();
		try {
			IssueDaoImpl issueDaoImpl = CJayClient.getInstance().getDatabaseManager().getHelper(getActivity())
													.getIssueDaoImpl();
			mIssue = issueDaoImpl.queryForId(mIssueUUID);

			if (null != mIssue) {
				for (CJayImage cJayImage : mIssue.getCJayImages()) {
					if (cJayImage.getType() == mType) {
						mFeeds.add(cJayImage);
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void refresh() {
		populateCJayImages();
		mFeedsAdapter.updateData(mFeeds);
	}

	public void setIssueUUID(String issueUUID) {
		mIssueUUID = issueUUID;
	}

	public void setType(int type) {
		mType = type;
	}
}
