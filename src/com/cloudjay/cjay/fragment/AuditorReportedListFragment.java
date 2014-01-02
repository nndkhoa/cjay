package com.cloudjay.cjay.fragment;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import android.net.Uri;
import android.widget.ImageView;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.ami.fundapter.BindDictionary;
import com.ami.fundapter.FunDapter;
import com.ami.fundapter.extractors.StringExtractor;
import com.ami.fundapter.interfaces.DynamicImageLoader;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.Utils;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ItemLongClick;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.ViewById;

@EFragment(R.layout.fragment_auditor_reported)
@OptionsMenu(R.menu.menu_auditor_reported)
public class AuditorReportedListFragment extends SherlockFragment {
	private final static String TAG = "AuditorReportedListFragment";
	
	private ArrayList<ContainerSession> mFeeds;
	private FunDapter<ContainerSession> mFeedsAdapter;

	private ContainerSession mSelectedContainerSession;
	
	@ViewById(R.id.container_list) ListView mFeedListView;
	
	@AfterViews
	void afterViews() {
		// load list data
		populateReportedContainerFeeds();
		initContainerFeedAdapter(mFeeds);
		
		mSelectedContainerSession = null;
	}
	
	@ItemLongClick(R.id.container_list)
	void listItemLongClicked(int position) {
		// refresh highlighting
		mFeedListView.setItemChecked(position, true);

		// refresh menu
		mSelectedContainerSession = mFeedsAdapter.getItem(position);
		getActivity().supportInvalidateOptionsMenu();
	}
	
	@OptionsItem(R.id.menu_upload)
	void uploadMenuItemSelected() {
		if (mSelectedContainerSession != null) {
			// TODO			
		}
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		boolean isDisplayed = !(mSelectedContainerSession == null);
		menu.findItem(R.id.menu_upload).setVisible(isDisplayed);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		// refresh list
		populateReportedContainerFeeds();
		mFeedsAdapter.updateData(mFeeds);
	}
	
	private void populateReportedContainerFeeds() {
		ArrayList<ContainerSession> containerSessions = (ArrayList<ContainerSession>) DataCenter.getInstance().getListContainerSessions(getActivity());
		mFeeds = new ArrayList<ContainerSession>();
		for (ContainerSession containerSession : containerSessions) {
			Logger.Log(TAG, containerSession.getContainerId() + " - " + containerSession.getCJayImages().size());
			boolean reported = (containerSession.getCJayImages().size() > 0); // if has no images then not reported
			for (CJayImage cJayImage : containerSession.getCJayImages()) {
				if (cJayImage.getIssue() == null) {
					reported = false;
					break;
				}
			}
			if (reported) {
				mFeeds.add(containerSession);
			}
		}
	}
	
	private void initContainerFeedAdapter(ArrayList<ContainerSession> containers) {
		BindDictionary<ContainerSession> feedsDict = new BindDictionary<ContainerSession>();
		feedsDict.addStringField(R.id.feed_item_container_id,
				new StringExtractor<ContainerSession>() {
					@Override
					public String getStringValue(ContainerSession item,
							int position) {
						return Utils.stripNull(item.getContainerId());
					}
				});
		feedsDict.addStringField(R.id.feed_item_container_owner,
				new StringExtractor<ContainerSession>() {
					@Override
					public String getStringValue(ContainerSession item,
							int position) {
						return Utils.stripNull(item.getOperatorName());
					}
				});
		feedsDict.addStringField(R.id.feed_item_container_import_date,
				new StringExtractor<ContainerSession>() {
					@Override
					public String getStringValue(ContainerSession item,
							int position) {
						return Utils.stripNull(item.getCheckInTime());
					}
				});
		feedsDict.addStringField(R.id.feed_item_container_issues,
				new StringExtractor<ContainerSession>() {
					@Override
					public String getStringValue(ContainerSession item,
							int position) {
						return Utils.stripNull(item.getIssueCount());
					}
				});
		feedsDict.addDynamicImageField(R.id.feed_item_picture,
				new StringExtractor<ContainerSession>() {
					@Override
					public String getStringValue(ContainerSession item,
							int position) {
						return item.getOriginalPhotoUri().toString();
					}
				}, new DynamicImageLoader() {
					@Override
					public void loadImage(String url, ImageView view) {
						if (url != null) {
							try {
								view.setImageBitmap(Utils.decodeImage(
										getActivity().getContentResolver(),
										Uri.parse(url),
										Utils.MINI_THUMBNAIL_SIZE));
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							}
						}
					}
				});
		mFeedsAdapter = new FunDapter<ContainerSession>(getActivity(),
				containers, R.layout.list_item_audit_container, feedsDict);
		mFeedListView.setAdapter(mFeedsAdapter);
	}

	public void refresh() {
		mFeeds = (ArrayList<ContainerSession>) DataCenter.getInstance()
				.getListContainerSessions(getActivity());
		mFeedsAdapter.updateData(mFeeds);
	}
}