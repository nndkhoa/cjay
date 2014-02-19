package com.cloudjay.cjay.fragment;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Locale;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.ami.fundapter.BindDictionary;
import com.ami.fundapter.FunDapter;
import com.ami.fundapter.extractors.StringExtractor;
import com.ami.fundapter.interfaces.DynamicImageLoader;
import com.cloudjay.cjay.CJayActivity;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.RepairContainerActivity_;
import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.events.ContainerRepairedEvent;
import com.cloudjay.cjay.events.ContainerSessionChangedEvent;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.Issue;
import com.cloudjay.cjay.network.CJayClient;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.NoConnectionException;
import com.cloudjay.cjay.util.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;

import de.greenrobot.event.EventBus;

@EFragment(R.layout.fragment_repair_container_pending)
@OptionsMenu(R.menu.menu_repair_container_pending)
public class RepairContainerPendingListFragment extends SherlockFragment
		implements OnRefreshListener {

	private final static String LOG_TAG = "RepairContainerPendingListFragment";

	private ArrayList<ContainerSession> mSelectedContainerSessions;
	private ArrayList<ContainerSession> mFeeds;
	private FunDapter<ContainerSession> mFeedsAdapter;
	private ImageLoader imageLoader;
	PullToRefreshLayout mPullToRefreshLayout;

	@ViewById(R.id.container_list)
	ListView mFeedListView;

	@ViewById(R.id.search_edittext)
	EditText mSearchEditText;

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		ViewGroup viewGroup = (ViewGroup) view;
		mPullToRefreshLayout = new PullToRefreshLayout(viewGroup.getContext());
		ActionBarPullToRefresh
				.from(getActivity())
				.insertLayoutInto(viewGroup)
				.theseChildrenArePullable(R.id.container_list,
						android.R.id.empty).listener(this)
				.setup(mPullToRefreshLayout);
	}

	@Override
	public void onRefreshStarted(View view) {
		/**
		 * Simulate Refresh with 4 seconds sleep
		 */
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {

				Logger.Log(LOG_TAG, "onRefreshStarted");

				try {
					DataCenter.getInstance().fetchData(getActivity());
				} catch (NoConnectionException e) {
					((CJayActivity) getActivity())
							.showCrouton(R.string.alert_no_network);
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);

				// Notify PullToRefreshLayout that the refresh has finished
				mPullToRefreshLayout.setRefreshComplete();
			}
		}.execute();
	}

	@AfterViews
	void afterViews() {
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
		
		mFeedListView.setMultiChoiceModeListener(new MultiChoiceModeListener() {
		    @Override
		    public void onItemCheckedStateChanged(ActionMode mode, int position,
		                                          long id, boolean checked) {
		        // Here you can do something when items are selected/de-selected,
		        // such as update the title in the CAB
		    	mode.setTitle(String.valueOf(mFeedListView.getCheckedItemCount()));
		    }

		    @Override
		    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		        // Respond to clicks on the actions in the CAB
		        switch (item.getItemId()) {
		            case R.id.menu_check:
		            	setSelectedContainersFixed();
		                mode.finish(); // Action picked, so close the CAB
		                return true;
		            default:
		                return false;
		        }
		    }

		    @Override
		    public boolean onCreateActionMode(ActionMode mode, android.view.Menu menu) {
		        // Inflate the menu for the CAB
		        MenuInflater inflater = mode.getMenuInflater();
		        inflater.inflate(R.menu.menu_repair_container_pending, menu);
		        return true;
		    }

		    @Override
		    public void onDestroyActionMode(ActionMode mode) {
		        // Here you can make any necessary updates to the activity when
		        // the CAB is removed. By default, selected items are deselected/unchecked.
		    }

		    @Override
		    public boolean onPrepareActionMode(ActionMode mode, android.view.Menu menu) {
		        // Here you can perform updates to the CAB due to
		        // an invalidate() request
		        return false;
		    }
		});

		imageLoader = ImageLoader.getInstance();
		initContainerFeedAdapter(null);
	}

//	@OptionsItem(R.id.menu_check)
//	void checkMenuItemSelected() {
//		setContainerFixed(mSelectedContainerSession);
//		hideMenuItems();
//	}

	@ItemClick(R.id.container_list)
	void listItemClicked(int position) {
		Intent intent = new Intent(getActivity(),
				RepairContainerActivity_.class);
		intent.putExtra(RepairContainerActivity_.CJAY_CONTAINER_SESSION_EXTRA,
				mFeedsAdapter.getItem(position).getUuid());
		startActivity(intent);
	}

//	@ItemLongClick(R.id.container_list)
//	void listItemLongClicked(int position) {
//		// refresh highlighting and menu
//		mFeedListView.setItemChecked(position, true);
//		mSelectedContainerSession = mFeedsAdapter.getItem(position);
//		getActivity().supportInvalidateOptionsMenu();
//	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

//		boolean isDisplayed = !(mSelectedContainerSession == null);
//		menu.findItem(R.id.menu_check).setVisible(isDisplayed);
		menu.findItem(R.id.menu_check).setVisible(false);
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
		if (null != mFeedsAdapter) {
			refresh();
		}
		super.onResume();
	}
	
	void setSelectedContainersFixed() {
		// loop through all the selected container sessions
		// and set each of them as fixed
		SparseBooleanArray selected = mFeedListView.getCheckedItemPositions();
		mSelectedContainerSessions = new ArrayList<ContainerSession>();
		
		for (int i = 0; i < selected.size(); i++) {
		    if(selected.valueAt(i) == true) {
		        ContainerSession containerSession = (ContainerSession)mFeedListView.getItemAtPosition(selected.keyAt(i));
		        mSelectedContainerSessions.add(containerSession);
		    }
		}
		
		for (ContainerSession containerSession : mSelectedContainerSessions) {
	        setContainerFixed(containerSession);			
		}
		
		EventBus.getDefault().post(new ContainerRepairedEvent(mSelectedContainerSessions)); // Force refresh
	}
	
	void setContainerFixed(ContainerSession containerSession) {
		// set fixed to true
		containerSession.setFixed(true);

		// save db records
		try {
			ContainerSessionDaoImpl containerSessionDaoImpl = CJayClient
					.getInstance().getDatabaseManager()
					.getHelper(getActivity()).getContainerSessionDaoImpl();
			containerSessionDaoImpl.createOrUpdate(containerSession);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

//	void hideMenuItems() {
//		mFeedListView.setItemChecked(-1, true);
//		getActivity().supportInvalidateOptionsMenu();
//	}

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

		if (mSearchEditText != null) {
			mSearchEditText.setText(""); // this will refresh the list
		}
	}

	public void onEvent(ContainerRepairedEvent event) {
		Logger.Log(LOG_TAG, "onEvent ContainerRepairedEvent");
		refresh();
	}

	public void onEventMainThread(ContainerSessionChangedEvent event) {
		Logger.Log(LOG_TAG, "onEvent ContainerSessionChangedEvent");
		refresh();
	}

	private void initContainerFeedAdapter(ArrayList<ContainerSession> containers) {
		BindDictionary<ContainerSession> feedsDict = new BindDictionary<ContainerSession>();
		feedsDict.addStringField(R.id.feed_item_container_id,
				new StringExtractor<ContainerSession>() {
					@Override
					public String getStringValue(ContainerSession item,
							int position) {
						return Utils.replaceNullBySpace(item.getContainerId());
					}
				});
		feedsDict.addStringField(R.id.feed_item_container_owner,
				new StringExtractor<ContainerSession>() {
					@Override
					public String getStringValue(ContainerSession item,
							int position) {
						return Utils.replaceNullBySpace(item.getOperatorName());
					}
				});
		feedsDict.addStringField(R.id.feed_item_container_import_date,
				new StringExtractor<ContainerSession>() {
					@Override
					public String getStringValue(ContainerSession item,
							int position) {
						return Utils.replaceNullBySpace(item.getCheckInTime());
					}
				});
		feedsDict.addStringField(R.id.feed_item_container_issues,
				new StringExtractor<ContainerSession>() {
					@Override
					public String getStringValue(ContainerSession item,
							int position) {
						return Utils.replaceNullBySpace(item.getIssueCount());
					}
				});
		feedsDict.addDynamicImageField(R.id.feed_item_picture,
				new StringExtractor<ContainerSession>() {
					@Override
					public String getStringValue(ContainerSession item,
							int position) {
						return Utils.stripNull(item.getImageIdPath());
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
//		feedsDict.addDynamicImageField(R.id.feed_item_background,
//				new StringExtractor<ContainerSession>() {
//					@Override
//					public String getStringValue(ContainerSession item,
//							int position) {
//						boolean fixed = item.getIssues().size() > 0;
//						for (Issue issue : item.getIssues()) {
//							if (!issue.isFixed()) {
//								fixed = false;
//								break;
//							}
//						}
//						return (fixed ? "fixed" : "not fixed");
//					}
//				}, new DynamicImageLoader() {
//					@Override
//					public void loadImage(String url, ImageView view) {
//						if (url.equals("fixed")) {
//							view.setBackgroundResource(R.color.list_item_bg_container_fixed);
//						} else {
//							view.setBackgroundResource(0);
//						}
//					}
//				});
		mFeedsAdapter = new FunDapter<ContainerSession>(getActivity(),
				containers, R.layout.list_item_repair_container, feedsDict);
		mFeedListView.setAdapter(mFeedsAdapter);
	}
}