package com.cloudjay.cjay.fragment;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.widget.Button;
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
import com.cloudjay.cjay.dao.OperatorDaoImpl;
import com.cloudjay.cjay.events.ContainerSessionAddedEvent;
import com.cloudjay.cjay.model.Container;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.IDatabaseManager;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.network.CJayClient;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.Session;
import com.cloudjay.cjay.util.StringHelper;
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

@EFragment(R.layout.fragment_gate_import)
@OptionsMenu(R.menu.menu_gate_import)
public class GateImportListFragment extends SherlockFragment {

	private final static String LOG_TAG = "GateImportListFragment";

	@ViewById(R.id.btn_add_new)
	Button mAddNewBtn;
	@ViewById(R.id.feeds)
	ListView mFeedListView;

	private ArrayList<Operator> mOperators;
	private ArrayList<ContainerSession> mFeeds;
	private FunDapter<ContainerSession> mFeedsAdapter;

	private boolean mDirty;

	private ContainerSession mSelectedContainerSession;

	@OptionsItem(R.id.menu_camera)
	void cameraMenuItemSelected() {
		Logger.Log(LOG_TAG, "Menu camera item clicked");

		String uuid = mSelectedContainerSession.getUuid();
		Intent intent = new Intent(getActivity(), CameraActivity_.class);
		intent.putExtra(CameraActivity_.CJAY_CONTAINER_SESSION_EXTRA, uuid);
		intent.putExtra("type", 0); // in
		startActivity(intent);
	}

	@OptionsItem(R.id.menu_edit_container)
	void editMenuItemSelected() {
		Logger.Log(LOG_TAG, "Menu edit item clicked");

		// Open dialog for editing details
		showContainerDetailDialog(mSelectedContainerSession.getContainerId(),
				mSelectedContainerSession.getOperatorName(),
				AddContainerDialog.CONTAINER_DIALOG_EDIT);
	}

	@OptionsItem(R.id.menu_upload)
	void uploadMenuItemSelected() {
		try {

			Logger.Log(LOG_TAG, "Menu upload item clicked");

			ContainerSessionDaoImpl containerSessionDaoImpl = CJayClient
					.getInstance().getDatabaseManager()
					.getHelper(getActivity()).getContainerSessionDaoImpl();

			// User confirm upload
			mSelectedContainerSession.setUploadConfirmation(true);

			// this will call EventBus.post(`UploadsModifiedEvent`)
			mSelectedContainerSession
					.setUploadState(ContainerSession.STATE_UPLOAD_WAITING);

			containerSessionDaoImpl.update(mSelectedContainerSession);

			// It will trigger `UploadsFragment` Adapter notifyDataSetChanged
			EventBus.getDefault().post(
					new ContainerSessionAddedEvent(mSelectedContainerSession));

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@AfterViews
	void afterViews() {
		mFeeds = (ArrayList<ContainerSession>) DataCenter.getInstance()
				.getListLocalContainerSessions(getActivity());
		mOperators = (ArrayList<Operator>) DataCenter.getInstance()
				.getListOperators(getActivity());

		initContainerFeedAdapter(mFeeds);

		mSelectedContainerSession = null;
		mDirty = false;
	}

	@Click(R.id.btn_add_new)
	void addContainerClicked() {
		showContainerDetailDialog(
				getResources().getString(R.string.default_container_id), "",
				AddContainerDialog.CONTAINER_DIALOG_ADD);
	}

	@ItemClick(R.id.feeds)
	void listItemClicked(int position) {
		// refresh highlighting
		mFeedListView.setItemChecked(position, false);

		// clear current selection
		mSelectedContainerSession = null;
		getActivity().supportInvalidateOptionsMenu();

		android.util.Log.d(LOG_TAG, "Show item at position: " + position);

		// open photo viewer
//		Intent intent = new Intent(getActivity(), PhotoViewerActivity.class);
//		startActivity(intent);
	}

	@ItemLongClick(R.id.feeds)
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
		menu.findItem(R.id.menu_camera).setVisible(isDisplayed);
		menu.findItem(R.id.menu_edit_container).setVisible(isDisplayed);
		menu.findItem(R.id.menu_upload).setVisible(isDisplayed);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mDirty) {
			refresh();
		}
	}
	
	public void refresh() {
		mFeeds = (ArrayList<ContainerSession>) DataCenter.getInstance()
				.getListLocalContainerSessions(getActivity());
		mFeedsAdapter.updateData(mFeeds);
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

			User currentUser = Session.restore(getActivity()).getCurrentUser();

			// Create Container Session object
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

				Intent intent = new Intent(getActivity(), CameraActivity_.class);
				intent.putExtra(CameraActivity_.CJAY_CONTAINER_SESSION_EXTRA,
						containerSession.getUuid());
				intent.putExtra("type", 0); // in
				startActivity(intent);

			} catch (SQLException e) {
				e.printStackTrace();
			}

			mDirty = true;
			break;

		case AddContainerDialog.CONTAINER_DIALOG_EDIT:
			try {
				// find operator
				IDatabaseManager databaseManager = CJayClient.getInstance()
						.getDatabaseManager();
				OperatorDaoImpl operatorDaoImpl = databaseManager.getHelper(getActivity())
						.getOperatorDaoImpl();
				Operator operator = null;
				List<Operator> listOperators = operatorDaoImpl.queryForEq(
						Operator.CODE, operatorCode);
	
				if (listOperators.isEmpty()) {
					operator = new Operator();
					operator.setCode(operatorCode);
					operator.setName(operatorCode);
					operatorDaoImpl.addOperator(operator);
				} else {
					operator = listOperators.get(0);
				}
				
				// update container details
				Container container = mSelectedContainerSession.getContainer();
				container.setContainerId(containerId);
				container.setOperator(operator);

				// save container details
				ContainerSessionDaoImpl containerSessionDaoImpl = databaseManager
						.getHelper(getActivity()).getContainerSessionDaoImpl();
				containerSessionDaoImpl.addContainerSessions(mSelectedContainerSession);

			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			// refresh list view
			refresh();
			break;
		}

	}

	private void initContainerFeedAdapter(ArrayList<ContainerSession> containers) {
		BindDictionary<ContainerSession> feedsDict = new BindDictionary<ContainerSession>();
		feedsDict.addStringField(R.id.feed_item_container_id,
				new StringExtractor<ContainerSession>() {
					@Override
					public String getStringValue(ContainerSession item,
							int position) {
						return item.getContainerId();
					}
				});
		feedsDict.addStringField(R.id.feed_item_container_owner,
				new StringExtractor<ContainerSession>() {
					@Override
					public String getStringValue(ContainerSession item,
							int position) {
						return item.getOperatorName();
					}
				});
		feedsDict.addStringField(R.id.feed_item_container_import_date,
				new StringExtractor<ContainerSession>() {
					@Override
					public String getStringValue(ContainerSession item,
							int position) {
						return item.getCheckInTime();
					}
				});
		feedsDict.addStringField(R.id.feed_item_container_export_date,
				new StringExtractor<ContainerSession>() {
					@Override
					public String getStringValue(ContainerSession item,
							int position) {
						return item.getCheckOutTime();
					}
				});
		feedsDict.addDynamicImageField(R.id.feed_item_picture,
				new StringExtractor<ContainerSession>() {
					@Override
					public String getStringValue(ContainerSession item,
							int position) {
						return item.getFullContainerId();
					}
				}, new DynamicImageLoader() {
					@Override
					public void loadImage(String stringColor, ImageView view) {
						view.setImageResource(R.drawable.ic_app);
					}
				});
		mFeedsAdapter = new FunDapter<ContainerSession>(getActivity(),
				containers, R.layout.list_item_container, feedsDict);
		mFeedListView.setAdapter(mFeedsAdapter);
	}
}
