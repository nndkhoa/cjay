package com.cloudjay.cjay.fragment;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Locale;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.ami.fundapter.BindDictionary;
import com.ami.fundapter.FunDapter;
import com.ami.fundapter.extractors.StringExtractor;
import com.ami.fundapter.interfaces.DynamicImageLoader;
import com.cloudjay.cjay.AuditorContainerActivity_;
import com.cloudjay.cjay.CameraActivity_;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.dao.ContainerDaoImpl;
import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.dao.OperatorDaoImpl;
import com.cloudjay.cjay.events.ContainerEditedEvent;
import com.cloudjay.cjay.events.ContainerSessionAddedEvent;
import com.cloudjay.cjay.model.Container;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.DatabaseHelper;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.network.CJayClient;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.Session;
import com.cloudjay.cjay.util.StringHelper;
import com.cloudjay.cjay.util.Utils;
import com.cloudjay.cjay.view.AddContainerDialog;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ItemClick;
import com.googlecode.androidannotations.annotations.ItemLongClick;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.ViewById;

import de.greenrobot.event.EventBus;

@EFragment(R.layout.fragment_auditor_reporting)
@OptionsMenu(R.menu.menu_auditor_reporting)
public class AuditorReportingListFragment extends SherlockFragment {

	private ArrayList<Operator> mOperators;
	private ArrayList<ContainerSession> mFeeds;
	private FunDapter<ContainerSession> mFeedsAdapter;

	private ContainerSession mSelectedContainerSession;
	
	@ViewById(R.id.container_list) 		ListView mFeedListView;
	@ViewById(R.id.search_edittext)		EditText mSearchEditText;
	@ViewById(R.id.add_button)			Button mAddButton;
	@ViewById(R.id.notfound_textview)	TextView mNotfoundTextView;
	
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
		mFeeds = (ArrayList<ContainerSession>) DataCenter.getInstance()
				.getListContainerSessions(getActivity());
		mOperators = (ArrayList<Operator>) DataCenter.getInstance()
				.getListOperators(getActivity());
		configureControls(mFeeds);
		initContainerFeedAdapter(mFeeds);

		mSelectedContainerSession = null;
	}
	
	@OptionsItem(R.id.menu_edit_container)
	void editMenuItemSelected() {
		// Open dialog for editing details
		showContainerDetailDialog(mSelectedContainerSession.getContainerId(),
				mSelectedContainerSession.getOperatorName(),
				AddContainerDialog.CONTAINER_DIALOG_EDIT);
	}
	
	@Click(R.id.add_button)
	void addButtonClicked() {
		// show add container dialog
		showContainerDetailDialog(
				getResources().getString(R.string.default_container_id), "",
				AddContainerDialog.CONTAINER_DIALOG_ADD);
	}
	
	@ItemClick(R.id.container_list)
	void listItemClicked(int position) {
		// refresh highlighting
		mFeedListView.setItemChecked(position, false);

		// clear current selection
		mSelectedContainerSession = null;
		getActivity().supportInvalidateOptionsMenu();
		
		Intent intent = new Intent(getActivity(), AuditorContainerActivity_.class);
		intent.putExtra(AuditorContainerActivity_.CJAY_CONTAINER_SESSION_EXTRA, mFeedsAdapter.getItem(position).getUuid());
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
		menu.findItem(R.id.menu_edit_container).setVisible(isDisplayed);
		menu.findItem(R.id.menu_upload).setVisible(isDisplayed);
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
			// Create Container Session object
			User currentUser = Session.restore(getActivity()).getCurrentUser();
			ContainerSession containerSession = new ContainerSession(
					getActivity(),
					containerId,
					operatorCode,
					StringHelper
							.getCurrentTimestamp(CJayConstant.CJAY_SERVER_DATETIME_FORMAT),
					currentUser.getDepot().getDepotCode());

			containerSession.setUploadConfirmation(false);
			containerSession.setOnLocal(true);
			containerSession.setUploadState(ContainerSession.STATE_NONE);

			try {
				ContainerSessionDaoImpl containerSessionDaoImpl = CJayClient
						.getInstance().getDatabaseManager()
						.getHelper(getActivity()).getContainerSessionDaoImpl();

				containerSessionDaoImpl.addContainerSessions(containerSession);
				
				// trigger update container lists
				EventBus.getDefault().post(new ContainerSessionAddedEvent(containerSession));

				Intent intent = new Intent(getActivity(), CameraActivity_.class);
				intent.putExtra(CameraActivity_.CJAY_CONTAINER_SESSION_EXTRA,
						containerSession.getUuid());
				intent.putExtra("type", 1); // in
				startActivity(intent);

			} catch (SQLException e) {
				e.printStackTrace();
			}
			break;
			
		case AddContainerDialog.CONTAINER_DIALOG_EDIT:
			try {
				if (mSelectedContainerSession.getContainerId().equals(containerId) && mSelectedContainerSession.getOperatorName().equals(operatorName)) {
					// do nothing
				} else {
					DatabaseHelper databaseHelper = CJayClient.getInstance().getDatabaseManager().getHelper(getActivity());
					OperatorDaoImpl operatorDaoImpl = databaseHelper.getOperatorDaoImpl();
					ContainerDaoImpl containerDaoImpl = databaseHelper.getContainerDaoImpl();
					ContainerSessionDaoImpl containerSessionDaoImpl = databaseHelper.getContainerSessionDaoImpl();
	
					// find operator
					Operator operator = operatorDaoImpl.findOperator(operatorCode);

					// update container details
					Container container = mSelectedContainerSession.getContainer();
					container.setContainerId(containerId);
					container.setOperator(operator);
	
					// save container details
					containerSessionDaoImpl.addContainerSessions(mSelectedContainerSession);
					
					// update database
					containerDaoImpl.update(container);
					containerSessionDaoImpl.update(mSelectedContainerSession);
					
					// trigger update container lists
					EventBus.getDefault().post(new ContainerEditedEvent(mSelectedContainerSession));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			break;
		}
	}

	private void search(String searchText) {
		if (searchText.equals("")) {
			configureControls(mFeeds);
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
			configureControls(searchFeeds);
			mFeedsAdapter.updateData(searchFeeds);
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
	
	private void initContainerFeedAdapter(ArrayList<ContainerSession> containers) {
		BindDictionary<ContainerSession> feedsDict = new BindDictionary<ContainerSession>();
		feedsDict.addStringField(R.id.feed_item_container_id,
				new StringExtractor<ContainerSession>() {
					@Override
					public String getStringValue(ContainerSession item, int position) {
						return item.getContainerId();
					}
				});
		feedsDict.addStringField(R.id.feed_item_container_owner,
				new StringExtractor<ContainerSession>() {
					@Override
					public String getStringValue(ContainerSession item, int position) {
						return item.getOperatorName();
					}
				});
		feedsDict.addStringField(R.id.feed_item_container_import_date,
				new StringExtractor<ContainerSession>() {
					@Override
					public String getStringValue(ContainerSession item, int position) {
						return item.getCheckInTime();
					}
				});
		feedsDict.addStringField(R.id.feed_item_container_issues,
				new StringExtractor<ContainerSession>() {
					@Override
					public String getStringValue(ContainerSession item, int position) {
						return item.getIssueCount();
					}
				});
		feedsDict.addDynamicImageField(R.id.feed_item_picture,
				new StringExtractor<ContainerSession>() {
					@Override
					public String getStringValue(ContainerSession item, int position) {
						return item.getOriginalPhotoUri().toString();
					}
				}, new DynamicImageLoader() {
					@Override
					public void loadImage(String url, ImageView view) {
						if (url != null) {
							try {
								view.setImageBitmap(Utils.decodeImage(getActivity().getContentResolver(), Uri.parse(url), Utils.MINI_THUMBNAIL_SIZE));
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