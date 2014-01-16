package com.cloudjay.cjay.fragment;

import java.sql.SQLException;
import java.util.ArrayList;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ItemLongClick;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.view.Menu;
import com.ami.fundapter.BindDictionary;
import com.ami.fundapter.FunDapter;
import com.ami.fundapter.extractors.StringExtractor;
import com.ami.fundapter.interfaces.DynamicImageLoader;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.events.ContainerCreatedEvent;
import com.cloudjay.cjay.events.ContainerEditedEvent;
import com.cloudjay.cjay.events.ContainerSessionEnqueueEvent;
import com.cloudjay.cjay.events.DataLoadedEvent;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.network.CJayClient;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.Utils;
import com.cloudjay.cjay.view.AddContainerDialog;
import com.nostra13.universalimageloader.core.ImageLoader;

import de.greenrobot.event.EventBus;

@EFragment(R.layout.fragment_gate_import)
@OptionsMenu(R.menu.menu_gate_import)
public class GateImportListFragment extends SherlockDialogFragment {

	private final static String LOG_TAG = "GateImportListFragment";

	@ViewById(R.id.btn_add_new)
	Button mAddNewBtn;
	
	@ViewById(R.id.feeds)
	ListView mFeedListView;

	private ArrayList<Operator> mOperators;
	private ArrayList<ContainerSession> mFeeds;
	private FunDapter<ContainerSession> mFeedsAdapter;

	private ImageLoader imageLoader;

	private ContainerSession mSelectedContainerSession;

	@AfterViews
	void afterViews() {
		imageLoader = ImageLoader.getInstance();

		mOperators = (ArrayList<Operator>) DataCenter.getInstance()
				.getListOperators(getActivity());

		initContainerFeedAdapter(null);
		mSelectedContainerSession = null;
	}

	@OptionsItem(R.id.menu_camera)
	void cameraMenuItemSelected() {
		Logger.Log(LOG_TAG, "Menu camera item clicked");

		ContainerSession.gotoCamera(getActivity(), mSelectedContainerSession,
				CJayImage.TYPE_IMPORT);
	}

	@OptionsItem(R.id.menu_edit_container)
	void editMenuItemSelected() {
		Logger.Log(LOG_TAG, "Menu edit item clicked");

		// Open dialog for editing details
		showContainerDetailDialog(mSelectedContainerSession.getContainerId(),
				mSelectedContainerSession.getOperatorName(),
				AddContainerDialog.CONTAINER_DIALOG_EDIT);
	}

	void hideMenuItems() {
		mSelectedContainerSession = null;
		mFeedListView.setItemChecked(-1, true);
		getActivity().supportInvalidateOptionsMenu();
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

			mSelectedContainerSession
					.setUploadState(ContainerSession.STATE_UPLOAD_WAITING);

			containerSessionDaoImpl.update(mSelectedContainerSession);

			// It will trigger `UploadsFragment` Adapter notifyDataSetChanged
			EventBus.getDefault()
					.post(new ContainerSessionEnqueueEvent(
							mSelectedContainerSession));

			hideMenuItems();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Click(R.id.btn_add_new)
	void addContainerClicked() {
		// getResources().getString(R.string.default_container_id)
		showContainerDetailDialog("", "", AddContainerDialog.CONTAINER_DIALOG_ADD);		
	}

	@ItemClick(R.id.feeds)
	void listItemClicked(int position) {
		// clear current selection
		hideMenuItems();

		Logger.Log(LOG_TAG, "Show item at position: " + position);

		// open photo viewer
		// Intent intent = new Intent(getActivity(),
		// PhotoViewerActivity_.class);
		// intent.putExtra(PhotoViewerActivity_.CJAY_CONTAINER_SESSION_EXTRA,
		// mFeedsAdapter.getItem(position).getUuid());
		// startActivity(intent);
	}

	@ItemLongClick(R.id.feeds)
	void listItemLongClicked(int position) {
		// refresh highlighting and menu
		mFeedListView.setItemChecked(position, true);
		mSelectedContainerSession = mFeedsAdapter.getItem(position);
		getActivity().supportInvalidateOptionsMenu();
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {

		Logger.Log(LOG_TAG, "onPrepareOptionsMenu");
		super.onPrepareOptionsMenu(menu);

		boolean isDisplayed = !(mSelectedContainerSession == null);
		menu.findItem(R.id.menu_camera).setVisible(isDisplayed);
		menu.findItem(R.id.menu_edit_container).setVisible(isDisplayed);
		menu.findItem(R.id.menu_upload).setVisible(isDisplayed);
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

		// TODO: Bug: chưa load xong data nên không có mOperators
		// Get the container id and container operator code
		String operatorCode = "";
		for (Operator operator : mOperators) {
			if (operator.getName().equals(operatorName)) {
				operatorCode = operator.getCode();
				break;
			}
		}

		if (TextUtils.isEmpty(containerId) || TextUtils.isEmpty(operatorCode)) { return; }
			
		switch (mode) {
		case AddContainerDialog.CONTAINER_DIALOG_ADD:
			try {
				ContainerSession containerSession = ContainerSession
						.createContainerSession(getActivity(), containerId,
								operatorCode);
				ContainerSession.gotoCamera(getActivity(), containerSession,
						CJayImage.TYPE_IMPORT);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			break;

		case AddContainerDialog.CONTAINER_DIALOG_EDIT:
			try {
				ContainerSession.editContainerSession(getActivity(),
						mSelectedContainerSession, containerId, operatorCode);
				hideMenuItems();
			} catch (SQLException e) {
				e.printStackTrace();
			}
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
		feedsDict.addStringField(R.id.feed_item_container_export_date,
				new StringExtractor<ContainerSession>() {
					@Override
					public String getStringValue(ContainerSession item,
							int position) {
						return Utils.replaceNullBySpace(item.getCheckOutTime());
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

		mFeedsAdapter = new FunDapter<ContainerSession>(getActivity(),
				containers, R.layout.list_item_container, feedsDict);

		mFeedListView.setAdapter(mFeedsAdapter);
	}

	public void onEvent(ContainerCreatedEvent event) {
		Logger.Log(LOG_TAG, "onEvent ContainerCreatedEvent");
		refresh();
	}

	public void onEvent(ContainerEditedEvent event) {
		Logger.Log(LOG_TAG, "onEvent ContainerEditedEvent");
		refresh();
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

		mFeeds = (ArrayList<ContainerSession>) DataCenter.getInstance()
				.getListLocalContainerSessions(getActivity());
		mFeedsAdapter.updateData(mFeeds);
	}

	@Override
	public void onResume() {
		if (mFeedsAdapter != null) {
			refresh();
		}
		super.onResume();
	}
}
