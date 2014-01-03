package com.cloudjay.cjay.fragment;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Locale;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.ami.fundapter.BindDictionary;
import com.ami.fundapter.FunDapter;
import com.ami.fundapter.extractors.StringExtractor;
import com.ami.fundapter.interfaces.DynamicImageLoader;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.RepairContainerActivity_;
import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.events.ContainerRepairedEvent;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.network.CJayClient;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.Utils;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ItemClick;
import com.googlecode.androidannotations.annotations.ItemLongClick;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.ViewById;
import com.nostra13.universalimageloader.core.ImageLoader;

import de.greenrobot.event.EventBus;

@EFragment(R.layout.fragment_repair_container_pending)
@OptionsMenu(R.menu.menu_repair_container_pending)
public class RepairContainerPendingListFragment extends SherlockFragment {

	private ArrayList<ContainerSession> mFeeds;
	private FunDapter<ContainerSession> mFeedsAdapter;

	private ContainerSession mSelectedContainerSession;

	private ImageLoader imageLoader;

	@ViewById(R.id.container_list)
	ListView mFeedListView;
	@ViewById(R.id.search_edittext)
	EditText mSearchEditText;

	@AfterViews
	void afterViews() {
		imageLoader = ImageLoader.getInstance();

		mSearchEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable arg0) {
				search(arg0.toString());
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		});

		mFeeds = (ArrayList<ContainerSession>) DataCenter.getInstance()
				.getListPendingContainerSessions(getActivity());
		initContainerFeedAdapter(mFeeds);

		mSelectedContainerSession = null;
	}

	@OptionsItem(R.id.menu_check)
	void checkMenuItemSelected() {
		// set fixed to true
		mSelectedContainerSession.setFixed(true);
		
		// save db records
		try {
			ContainerSessionDaoImpl containerSessionDaoImpl = CJayClient
					.getInstance().getDatabaseManager().getHelper(getActivity())
					.getContainerSessionDaoImpl();
			containerSessionDaoImpl.createOrUpdate(mSelectedContainerSession);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		// clear current selection
		mSelectedContainerSession = null;
		getActivity().supportInvalidateOptionsMenu();
		
		EventBus.getDefault().post(new ContainerRepairedEvent(mSelectedContainerSession));
	}

	@ItemClick(R.id.container_list)
	void listItemClicked(int position) {
		// refresh highlighting
		mFeedListView.setItemChecked(position, false);

		// clear current selection
		mSelectedContainerSession = null;
		getActivity().supportInvalidateOptionsMenu();

		Intent intent = new Intent(getActivity(), RepairContainerActivity_.class);
		intent.putExtra(RepairContainerActivity_.CJAY_CONTAINER_SESSION_EXTRA,
				mFeedsAdapter.getItem(position).getUuid());
		startActivity(intent);
	}

	@ItemLongClick(R.id.container_list)
	void listItemLongClicked(int position) {
		// refresh highlighting
		mFeedListView.setItemChecked(position, true);

		// refresh menu
		mSelectedContainerSession = mFeedsAdapter.getItem(position);
		getActivity().supportInvalidateOptionsMenu();
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		boolean isDisplayed = !(mSelectedContainerSession == null);
		menu.findItem(R.id.menu_check).setVisible(isDisplayed);
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

	@Override
	public void onResume() {
		super.onResume();
		refresh();
	}

	private void search(String searchText) {
		if (searchText.equals("")) {
			mFeedsAdapter.updateData(mFeeds);
		} else {
			ArrayList<ContainerSession> searchFeeds = new ArrayList<ContainerSession>();
			for (ContainerSession containerSession : mFeeds) {
				if (containerSession.getContainerId().toLowerCase(Locale.US)
						.contains(searchText.toLowerCase(Locale.US))) {
					searchFeeds.add(containerSession);
				}
			}
			// refresh list
			mFeedsAdapter.updateData(searchFeeds);
		}
	}
	
	public void refresh() {
		mFeeds = (ArrayList<ContainerSession>) DataCenter.getInstance()
				.getListPendingContainerSessions(getActivity());

		if (null != mSearchEditText) {
			mSearchEditText.setText(""); // this will refresh the list
		}
	}

	public void onEvent(ContainerRepairedEvent event) {
		refresh();
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
}