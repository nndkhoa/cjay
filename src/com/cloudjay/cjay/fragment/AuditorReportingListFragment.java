package com.cloudjay.cjay.fragment;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Locale;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ItemLongClick;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.cloudjay.cjay.AuditorContainerActivity_;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.events.ContainerSessionEnqueueEvent;
import com.cloudjay.cjay.events.DataLoadedEvent;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.network.CJayClient;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.view.AddContainerDialog;
import com.cloudjay.cjay.view.AuditorContainerListView;

import de.greenrobot.event.EventBus;

@EFragment(R.layout.fragment_auditor_reporting)
@OptionsMenu(R.menu.menu_auditor_reporting)
public class AuditorReportingListFragment extends SherlockFragment {

	public static final String LOG_TAG = "AuditorReportingListFragment";

	public static final int STATE_NOT_REPORTED = 0;
	public static final int STATE_REPORTING = 1;

	private ArrayList<Operator> mOperators;
	private ArrayList<ContainerSession> mFeeds;
	
	private int mState = STATE_NOT_REPORTED;

	private ContainerSession mSelectedContainerSession;

	@ViewById(R.id.container_list)
	AuditorContainerListView mFeedListView;
	@ViewById(R.id.search_edittext)
	EditText mSearchEditText;
	@ViewById(R.id.add_button)
	Button mAddButton;
	@ViewById(R.id.notfound_textview)
	TextView mNotfoundTextView;

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

		mOperators = (ArrayList<Operator>) DataCenter.getInstance()
				.getListOperators(getActivity());

		mFeedListView.initAdapter();
		mSelectedContainerSession = null;
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

				// hide menu items
				hideMenuItems();

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	@Click(R.id.add_button)
	void addButtonClicked() {
		// show add container dialog
		String containerId;
		if (!TextUtils.isEmpty(mSearchEditText.getText().toString())) {
			containerId = mSearchEditText.getText().toString();
		} else {
			containerId = getResources().getString(
					R.string.default_container_id);
		}
		showContainerDetailDialog(containerId, "",
				AddContainerDialog.CONTAINER_DIALOG_ADD);
	}

	@ItemClick(R.id.container_list)
	void listItemClicked(int position) {
		// clear current selection
		hideMenuItems();

		Intent intent = new Intent(getActivity(),
				AuditorContainerActivity_.class);
		intent.putExtra(AuditorContainerActivity_.CJAY_CONTAINER_SESSION_EXTRA,
				mFeedListView.getItem(position).getUuid());
		startActivity(intent);
	}

	@ItemLongClick(R.id.container_list)
	void listItemLongClicked(int position) {
		// refresh highlighting and menu
		mFeedListView.highlightAt(position);
		mSelectedContainerSession = mFeedListView.getItem(position);
		getActivity().supportInvalidateOptionsMenu();
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		if (mState == STATE_REPORTING) {
			boolean isDisplayed = (mSelectedContainerSession != null);
			menu.findItem(R.id.menu_upload).setVisible(isDisplayed);
		} else {
			menu.findItem(R.id.menu_upload).setVisible(false);
		}
	}

	public void setState(int state) {
		mState = state;
	}

	public void showContainerDetailDialog(String containerId,
			String operatorName, int mode) {
		FragmentManager fm = getActivity().getSupportFragmentManager();
		AddContainerDialog addContainerDialog = new AddContainerDialog();
		addContainerDialog.setContainerId(containerId);
		addContainerDialog.setOperatorName(operatorName);
		addContainerDialog.setMode(mode);
		addContainerDialog.setParent(this);
		addContainerDialog.show(fm, "add_container_dialog");
	}

	public void OnOperatorSelected(String containerId, String operatorName,
			int mode) {
		showContainerDetailDialog(containerId, operatorName, mode);
	}

	public void OnContainerInputCompleted(String containerId,
			String operatorName, int mode) {
		// Get the container id and container operator code
		String operatorCode = "";
		for (Operator operator : mOperators) {
			if (operator.getName().equals(operatorName)) {
				operatorCode = operator.getCode();
				break;
			}
		}

		switch (mode) {
		case AddContainerDialog.CONTAINER_DIALOG_ADD:
			try {
				ContainerSession containerSession = ContainerSession
						.createContainerSession(getActivity(), containerId,
								operatorCode);
				ContainerSession.gotoCamera(getActivity(), containerSession,
						CJayImage.TYPE_REPORT);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			break;

		case AddContainerDialog.CONTAINER_DIALOG_EDIT:
			try {
				ContainerSession.editContainerSession(getActivity(),
						mSelectedContainerSession, containerId, operatorCode);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			break;
		}
	}

	private void search(String searchText) {
		if (searchText.equals("")) {
			configureControls(mFeeds);
			mFeedListView.setFeeds(mFeeds);
		} else {
			ArrayList<ContainerSession> searchFeeds = new ArrayList<ContainerSession>();
			for (ContainerSession containerSession : mFeeds) {
				if (containerSession.getContainerId().toLowerCase(Locale.US)
						.contains(searchText.toLowerCase(Locale.US))) {
					searchFeeds.add(containerSession);
				}
			}
			// refresh list
			configureControls(searchFeeds);
			mFeedListView.setFeeds(searchFeeds);
		}
	}

	private void configureControls(ArrayList<ContainerSession> list) {
		boolean hasContainers = list.size() > 0;
		if (hasContainers) {
			mFeedListView.setVisibility(View.VISIBLE);
			mAddButton.setVisibility(View.INVISIBLE);
			mNotfoundTextView.setVisibility(View.INVISIBLE);
		} else {
			mFeedListView.setVisibility(View.INVISIBLE);
			mAddButton.setVisibility(View.VISIBLE);
			mNotfoundTextView.setVisibility(View.VISIBLE);
		}
	}

	public void onEvent(ContainerSessionEnqueueEvent event) {
		Logger.Log(LOG_TAG, "onEvent ContainerSessionEnqueueEvent");
		refresh();
	}

	public void onEventMainThread(DataLoadedEvent event) {
		Logger.Log(LOG_TAG, "onEvent DataLoadedEvent");
		refresh();
	}

	public void refresh() {
		Logger.Log(LOG_TAG, "onRefresh");

		if (mState == STATE_REPORTING) {
			mFeeds = (ArrayList<ContainerSession>) DataCenter.getInstance()
					.getListReportingContainerSessions(getActivity());
		} else {
			mFeeds = (ArrayList<ContainerSession>) DataCenter.getInstance()
					.getListNotReportedContainerSessions(getActivity());
		}

		if (mSearchEditText != null) {
			mFeedListView.setFeeds(mFeeds);
			mSearchEditText.setText(""); // this will refresh the list
		}
	}

	void hideMenuItems() {
		mSelectedContainerSession = null;
		mFeedListView.clearHighlighting();
		getActivity().supportInvalidateOptionsMenu();
	}

	@Override
	public void onResume() {
		refresh();
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
}

//package com.cloudjay.cjay.fragment;
//
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.Locale;
//
//import org.androidannotations.annotations.AfterViews;
//import org.androidannotations.annotations.Click;
//import org.androidannotations.annotations.EFragment;
//import org.androidannotations.annotations.ItemClick;
//import org.androidannotations.annotations.ItemLongClick;
//import org.androidannotations.annotations.OptionsItem;
//import org.androidannotations.annotations.OptionsMenu;
//import org.androidannotations.annotations.ViewById;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.support.v4.app.FragmentManager;
//import android.text.Editable;
//import android.text.TextUtils;
//import android.text.TextWatcher;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.ListView;
//import android.widget.TextView;
//
//import com.actionbarsherlock.app.SherlockFragment;
//import com.actionbarsherlock.view.Menu;
//import com.ami.fundapter.BindDictionary;
//import com.ami.fundapter.FunDapter;
//import com.ami.fundapter.extractors.StringExtractor;
//import com.ami.fundapter.interfaces.DynamicImageLoader;
//import com.cloudjay.cjay.*;
//import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
//import com.cloudjay.cjay.events.ContainerSessionEnqueueEvent;
//import com.cloudjay.cjay.events.DataLoadedEvent;
//import com.cloudjay.cjay.model.CJayImage;
//import com.cloudjay.cjay.model.ContainerSession;
//import com.cloudjay.cjay.model.Operator;
//import com.cloudjay.cjay.network.CJayClient;
//import com.cloudjay.cjay.util.DataCenter;
//import com.cloudjay.cjay.util.Logger;
//import com.cloudjay.cjay.util.Utils;
//import com.cloudjay.cjay.view.AddContainerDialog;
//import com.nostra13.universalimageloader.core.ImageLoader;
//
//import de.greenrobot.event.EventBus;
//
//@EFragment(R.layout.fragment_auditor_reporting)
//@OptionsMenu(R.menu.menu_auditor_reporting)
//public class AuditorReportingListFragment extends SherlockFragment {
//
//	public static final String LOG_TAG = "AuditorReportingListFragment";
//
//	public static final int STATE_NOT_REPORTED = 0;
//	public static final int STATE_REPORTING = 1;
//
//	private ArrayList<Operator> mOperators;
//	private ArrayList<ContainerSession> mFeeds;
//	private FunDapter<ContainerSession> mFeedsAdapter;
//	private int mState = STATE_NOT_REPORTED;
//
//	private ContainerSession mSelectedContainerSession;
//
//	private ImageLoader imageLoader;
//
//	@ViewById(R.id.container_list)
//	ListView mFeedListView;
//	@ViewById(R.id.search_edittext)
//	EditText mSearchEditText;
//	@ViewById(R.id.add_button)
//	Button mAddButton;
//	@ViewById(R.id.notfound_textview)
//	TextView mNotfoundTextView;
//
//	@AfterViews
//	void afterViews() {
//		mSearchEditText.addTextChangedListener(new TextWatcher() {
//			@Override
//			public void afterTextChanged(Editable arg0) {
//				search(arg0.toString());
//			}
//
//			public void beforeTextChanged(CharSequence s, int start, int count,
//					int after) {
//			}
//
//			public void onTextChanged(CharSequence s, int start, int before,
//					int count) {
//			}
//		});
//
//		imageLoader = ImageLoader.getInstance();
//
//		mOperators = (ArrayList<Operator>) DataCenter.getInstance()
//				.getListOperators(getActivity());
//
//		initContainerFeedAdapter(null);
//		mSelectedContainerSession = null;
//	}
//
//	// @OptionsItem(R.id.menu_edit_container)
//	// void editMenuItemSelected() {
//	// // Open dialog for editing details
//	// showContainerDetailDialog(mSelectedContainerSession.getContainerId(),
//	// mSelectedContainerSession.getOperatorName(),
//	// AddContainerDialog.CONTAINER_DIALOG_EDIT);
//	// }
//
//	@OptionsItem(R.id.menu_upload)
//	void uploadMenuItemSelected() {
//		if (mSelectedContainerSession != null) {
//			try {
//				Logger.Log(LOG_TAG, "Menu upload item clicked");
//
//				ContainerSessionDaoImpl containerSessionDaoImpl = CJayClient
//						.getInstance().getDatabaseManager()
//						.getHelper(getActivity()).getContainerSessionDaoImpl();
//
//				// User confirm upload
//				mSelectedContainerSession.setUploadConfirmation(true);
//
//				mSelectedContainerSession
//						.setUploadState(ContainerSession.STATE_UPLOAD_WAITING);
//
//				containerSessionDaoImpl.update(mSelectedContainerSession);
//
//				// It will trigger `UploadsFragment` Adapter
//				// notifyDataSetChanged
//				EventBus.getDefault().post(
//						new ContainerSessionEnqueueEvent(
//								mSelectedContainerSession));
//
//				// hide menu items
//				hideMenuItems();
//
//			} catch (SQLException e) {
//				e.printStackTrace();
//			}
//		}
//	}
//
//	@Click(R.id.add_button)
//	void addButtonClicked() {
//		// show add container dialog
//		String containerId;
//		if (!TextUtils.isEmpty(mSearchEditText.getText().toString())) {
//			containerId = mSearchEditText.getText().toString();
//		} else {
//			containerId = getResources().getString(
//					R.string.default_container_id);
//		}
//		showContainerDetailDialog(containerId, "",
//				AddContainerDialog.CONTAINER_DIALOG_ADD);
//	}
//
//	@ItemClick(R.id.container_list)
//	void listItemClicked(int position) {
//		// clear current selection
//		hideMenuItems();
//
//		Intent intent = new Intent(getActivity(),
//				AuditorContainerActivity_.class);
//		intent.putExtra(AuditorContainerActivity_.CJAY_CONTAINER_SESSION_EXTRA,
//				mFeedsAdapter.getItem(position).getUuid());
//		startActivity(intent);
//	}
//
//	@ItemLongClick(R.id.container_list)
//	void listItemLongClicked(int position) {
//		// refresh highlighting and menu
//		mFeedListView.setItemChecked(position, true);
//		mSelectedContainerSession = mFeedsAdapter.getItem(position);
//		getActivity().supportInvalidateOptionsMenu();
//	}
//
//	@Override
//	public void onPrepareOptionsMenu(Menu menu) {
//		super.onPrepareOptionsMenu(menu);
//
//		if (mState == STATE_REPORTING) {
//			boolean isDisplayed = (mSelectedContainerSession != null);
//			menu.findItem(R.id.menu_upload).setVisible(isDisplayed);
//		} else {
//			menu.findItem(R.id.menu_upload).setVisible(false);
//		}
//	}
//
//	public void setState(int state) {
//		mState = state;
//	}
//
//	public void showContainerDetailDialog(String containerId,
//			String operatorName, int mode) {
//		FragmentManager fm = getActivity().getSupportFragmentManager();
//		AddContainerDialog addContainerDialog = new AddContainerDialog();
//		addContainerDialog.setContainerId(containerId);
//		addContainerDialog.setOperatorName(operatorName);
//		addContainerDialog.setMode(mode);
//		addContainerDialog.setParent(this);
//		addContainerDialog.show(fm, "add_container_dialog");
//	}
//
//	public void OnOperatorSelected(String containerId, String operatorName,
//			int mode) {
//		showContainerDetailDialog(containerId, operatorName, mode);
//	}
//
//	public void OnContainerInputCompleted(String containerId,
//			String operatorName, int mode) {
//		// Get the container id and container operator code
//		String operatorCode = "";
//		for (Operator operator : mOperators) {
//			if (operator.getName().equals(operatorName)) {
//				operatorCode = operator.getCode();
//				break;
//			}
//		}
//
//		switch (mode) {
//		case AddContainerDialog.CONTAINER_DIALOG_ADD:
//			try {
//				ContainerSession containerSession = ContainerSession
//						.createContainerSession(getActivity(), containerId,
//								operatorCode);
//				ContainerSession.gotoCamera(getActivity(), containerSession,
//						CJayImage.TYPE_REPORT);
//			} catch (SQLException e) {
//				e.printStackTrace();
//			}
//			break;
//
//		case AddContainerDialog.CONTAINER_DIALOG_EDIT:
//			try {
//				ContainerSession.editContainerSession(getActivity(),
//						mSelectedContainerSession, containerId, operatorCode);
//			} catch (SQLException e) {
//				e.printStackTrace();
//			}
//			break;
//		}
//	}
//
//	private void search(String searchText) {
//		if (searchText.equals("")) {
//			configureControls(mFeeds);
//			mFeedsAdapter.updateData(mFeeds);
//		} else {
//			ArrayList<ContainerSession> searchFeeds = new ArrayList<ContainerSession>();
//			for (ContainerSession containerSession : mFeeds) {
//				if (containerSession.getContainerId().toLowerCase(Locale.US)
//						.contains(searchText.toLowerCase(Locale.US))) {
//					searchFeeds.add(containerSession);
//				}
//			}
//			// refresh list
//			configureControls(searchFeeds);
//			mFeedsAdapter.updateData(searchFeeds);
//		}
//	}
//
//	private void configureControls(ArrayList<ContainerSession> list) {
//		boolean hasContainers = list.size() > 0;
//		if (hasContainers) {
//			mFeedListView.setVisibility(View.VISIBLE);
//			mAddButton.setVisibility(View.INVISIBLE);
//			mNotfoundTextView.setVisibility(View.INVISIBLE);
//		} else {
//			mFeedListView.setVisibility(View.INVISIBLE);
//			mAddButton.setVisibility(View.VISIBLE);
//			mNotfoundTextView.setVisibility(View.VISIBLE);
//		}
//	}
//
//	private void initContainerFeedAdapter(ArrayList<ContainerSession> containers) {
//		BindDictionary<ContainerSession> feedsDict = new BindDictionary<ContainerSession>();
//		feedsDict.addStringField(R.id.feed_item_container_id,
//				new StringExtractor<ContainerSession>() {
//					@Override
//					public String getStringValue(ContainerSession item,
//							int position) {
//						return Utils.replaceNullBySpace(item.getContainerId());
//					}
//				});
//		feedsDict.addStringField(R.id.feed_item_container_owner,
//				new StringExtractor<ContainerSession>() {
//					@Override
//					public String getStringValue(ContainerSession item,
//							int position) {
//						return Utils.replaceNullBySpace(item.getOperatorName());
//					}
//				});
//		feedsDict.addStringField(R.id.feed_item_container_import_date,
//				new StringExtractor<ContainerSession>() {
//					@Override
//					public String getStringValue(ContainerSession item,
//							int position) {
//						return Utils.replaceNullBySpace(item.getCheckInTime());
//					}
//				});
//		feedsDict.addStringField(R.id.feed_item_container_issues,
//				new StringExtractor<ContainerSession>() {
//					@Override
//					public String getStringValue(ContainerSession item,
//							int position) {
//						return Utils.replaceNullBySpace(item.getIssueCount());
//					}
//				});
//		feedsDict.addDynamicImageField(R.id.feed_item_picture,
//				new StringExtractor<ContainerSession>() {
//					@Override
//					public String getStringValue(ContainerSession item,
//							int position) {
//						return Utils.stripNull(item.getImageIdPath());
//					}
//				}, new DynamicImageLoader() {
//					@Override
//					public void loadImage(String url, ImageView view) {
//						if (!TextUtils.isEmpty(url.trim())) {
//							imageLoader.displayImage(url, view);
//						} else {
//							view.setImageResource(R.drawable.ic_app);
//						}
//					}
//				});
//		mFeedsAdapter = new FunDapter<ContainerSession>(getActivity(),
//				containers, R.layout.list_item_audit_container, feedsDict);
//		mFeedListView.setAdapter(mFeedsAdapter);
//	}
//
//	public void onEvent(ContainerSessionEnqueueEvent event) {
//		Logger.Log(LOG_TAG, "onEvent ContainerSessionEnqueueEvent");
//		refresh();
//	}
//
//	public void onEventMainThread(DataLoadedEvent event) {
//		Logger.Log(LOG_TAG, "onEvent DataLoadedEvent");
//		refresh();
//	}
//
//	public void refresh() {
//		Logger.Log(LOG_TAG, "onRefresh");
//
//		if (mState == STATE_REPORTING) {
//			mFeeds = (ArrayList<ContainerSession>) DataCenter.getInstance()
//					.getListReportingContainerSessions(getActivity());
//		} else {
//			mFeeds = (ArrayList<ContainerSession>) DataCenter.getInstance()
//					.getListNotReportedContainerSessions(getActivity());
//		}
//
//		if (mSearchEditText != null) {
//			mFeedsAdapter.updateData(mFeeds);
//			mSearchEditText.setText(""); // this will refresh the list
//		}
//	}
//
//	void hideMenuItems() {
//		mSelectedContainerSession = null;
//		mFeedListView.setItemChecked(-1, true);
//		getActivity().supportInvalidateOptionsMenu();
//	}
//
//	@Override
//	public void onResume() {
//
//		if (mFeedsAdapter != null) {
//			refresh();
//		}
//		super.onResume();
//	}
//
//	@Override
//	public void onDestroy() {
//		EventBus.getDefault().unregister(this);
//		super.onDestroy();
//	}
//
//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//		EventBus.getDefault().register(this);
//		super.onCreate(savedInstanceState);
//	}
//}