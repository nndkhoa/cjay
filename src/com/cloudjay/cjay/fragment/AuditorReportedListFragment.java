package com.cloudjay.cjay.fragment;

import java.sql.SQLException;
import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.ami.fundapter.BindDictionary;
import com.ami.fundapter.FunDapter;
import com.ami.fundapter.extractors.StringExtractor;
import com.ami.fundapter.interfaces.DynamicImageLoader;
import com.cloudjay.cjay.*;
import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.events.ContainerSessionEnqueueEvent;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.network.CJayClient;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.Utils;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ItemLongClick;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import com.nostra13.universalimageloader.core.ImageLoader;

import de.greenrobot.event.EventBus;

@EFragment(R.layout.fragment_auditor_reported)
@OptionsMenu(R.menu.menu_auditor_reported)
public class AuditorReportedListFragment extends SherlockFragment {

	private final static String LOG_TAG = "AuditorReportedListFragment";
	private ArrayList<ContainerSession> mFeeds;
	private FunDapter<ContainerSession> mFeedsAdapter;
	private ContainerSession mSelectedContainerSession;

	@ViewById(R.id.container_list)
	ListView mFeedListView;

	private ImageLoader imageLoader;

	@AfterViews
	void afterViews() {
		imageLoader = ImageLoader.getInstance();

		// load list data
		mFeeds = (ArrayList<ContainerSession>) DataCenter.getInstance()
				.getListReportedContainerSessions(getActivity());
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

	@ItemClick(R.id.container_list)
	void listItemClicked(int position) {
		// refresh highlighting
		mFeedListView.setItemChecked(position, false);

		// clear current selection
		mSelectedContainerSession = null;
		getActivity().supportInvalidateOptionsMenu();

		Intent intent = new Intent(getActivity(),
				AuditorContainerActivity_.class);
		intent.putExtra(AuditorContainerActivity_.CJAY_CONTAINER_SESSION_EXTRA,
				mFeedsAdapter.getItem(position).getUuid());
		startActivity(intent);
	}

	@OptionsItem(R.id.menu_upload)
	void uploadMenuItemSelected() {
		if (mSelectedContainerSession != null) {
			try {

				Logger.Log(LOG_TAG, "Menu upload item clicked");

				ContainerSessionDaoImpl containerSessionDaoImpl = CJayClient
						.getInstance().getDatabaseManager()
						.getHelper(getActivity()).getContainerSessionDaoImpl();

				// User confirm upload
				mSelectedContainerSession.setUploadConfirmation(true);

				mSelectedContainerSession
						.setUploadState(ContainerSession.STATE_UPLOAD_WAITING);

				containerSessionDaoImpl.update(mSelectedContainerSession);

				// It will trigger `UploadsFragment` Adapter
				// notifyDataSetChanged
				EventBus.getDefault().post(
						new ContainerSessionEnqueueEvent(
								mSelectedContainerSession));

				hideMenuItems();

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	void hideMenuItems() {
		mSelectedContainerSession = null;
		getActivity().supportInvalidateOptionsMenu();
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

		if (null != mFeedsAdapter) {
			// refresh list
			mFeeds = (ArrayList<ContainerSession>) DataCenter.getInstance()
					.getListReportedContainerSessions(getActivity());
			mFeedsAdapter.updateData(mFeeds);
		}
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
							imageLoader.displayImage(url, view);
						}
					}
				});
		mFeedsAdapter = new FunDapter<ContainerSession>(getActivity(),
				containers, R.layout.list_item_audit_container, feedsDict);
		mFeedListView.setAdapter(mFeedsAdapter);
	}

	public void onEvent(ContainerSessionEnqueueEvent event) {
		Logger.Log(LOG_TAG, "onEvent ContainerSessionEnqueueEvent");
		refresh();
	}

	public void refresh() {
		Logger.Log(LOG_TAG, "refresh");
		mFeeds = (ArrayList<ContainerSession>) DataCenter.getInstance()
				.getListReportedContainerSessions(getActivity());
		mFeedsAdapter.updateData(mFeeds);
	}
}