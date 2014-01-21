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

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.events.ContainerSessionChangedEvent;
import com.cloudjay.cjay.events.ContainerSessionEnqueueEvent;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.network.CJayClient;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.StringHelper;
import com.cloudjay.cjay.util.Utils;
import com.cloudjay.cjay.view.AddContainerDialog;
import com.nostra13.universalimageloader.core.ImageLoader;

import de.greenrobot.event.EventBus;

@EFragment(R.layout.fragment_gate_export)
@OptionsMenu(R.menu.menu_gate_export)
public class GateExportListFragment extends SherlockFragment {

	private final static String LOG_TAG = "GateExportListFragment";

	private ArrayList<Operator> mOperators;
	private ArrayList<ContainerSession> mFeeds;
	private FunDapter<ContainerSession> mFeedsAdapter;

	private ContainerSession mSelectedContainerSession;
	private ImageLoader imageLoader;

	@ViewById(R.id.container_list)
	ListView mFeedListView;
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

		mSearchEditText
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == EditorInfo.IME_ACTION_SEARCH) {
							// hide keyboard
							InputMethodManager imm = (InputMethodManager) getActivity()
									.getSystemService(
											Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(
									mSearchEditText.getWindowToken(), 0);
							return true;
						}
						return false;
					}
				});

		imageLoader = ImageLoader.getInstance();
		mOperators = (ArrayList<Operator>) DataCenter.getInstance()
				.getListOperators(getActivity());

		initContainerFeedAdapter(null);
		mSelectedContainerSession = null;

	}

	void hideMenuItems() {
		mSelectedContainerSession = null;
		mFeedListView.setItemChecked(-1, true);
		getActivity().supportInvalidateOptionsMenu();
	}

	@OptionsItem(R.id.menu_upload)
	void uploadMenuItemSelected() {

		synchronized (this) {
			if (null != mSelectedContainerSession) {
				try {

					Logger.Log(LOG_TAG, "Menu upload item clicked");

					ContainerSessionDaoImpl containerSessionDaoImpl = CJayClient
							.getInstance().getDatabaseManager()
							.getHelper(getActivity())
							.getContainerSessionDaoImpl();

					// User confirm upload
					mSelectedContainerSession.setUploadConfirmation(true);

					mSelectedContainerSession
							.setCheckOutTime(StringHelper
									.getCurrentTimestamp(CJayConstant.CJAY_SERVER_DATETIME_FORMAT));

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
	}

	@Click(R.id.add_button)
	void addButtonClicked() {
		// show add container dialog
		String containerId;
		if (!TextUtils.isEmpty(mSearchEditText.getText().toString())) {
			containerId = mSearchEditText.getText().toString();
		} else {
			containerId = "";
		}
		showContainerDetailDialog(containerId, "",
				AddContainerDialog.CONTAINER_DIALOG_ADD);
	}

	@ItemClick(R.id.container_list)
	void listItemClicked(int position) {
		// clear current selection
		hideMenuItems();

		// get the selected container session and open camera
		ContainerSession containerSession = mFeedsAdapter.getItem(position);
		ContainerSession.gotoCamera(getActivity(), containerSession,
				CJayImage.TYPE_EXPORT);
	}

	@ItemLongClick(R.id.container_list)
	void listItemLongClicked(int position) {
		// refresh highlighting and menu
		mFeedListView.setItemChecked(position, true);
		mSelectedContainerSession = mFeedsAdapter.getItem(position);
		getActivity().supportInvalidateOptionsMenu();
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		boolean isDisplayed = !(mSelectedContainerSession == null);
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
		// Get the container id and container operator code
		String operatorCode = "";
		for (Operator operator : mOperators) {
			if (operator.getName().equals(operatorName)) {
				operatorCode = operator.getCode();
				break;
			}
		}

		if (TextUtils.isEmpty(containerId) || TextUtils.isEmpty(operatorCode)) {
			return;
		}

		switch (mode) {
		case AddContainerDialog.CONTAINER_DIALOG_ADD:
			try {
				ContainerSession containerSession = ContainerSession
						.createContainerSession(getActivity(), containerId,
								operatorCode);
				ContainerSession.gotoCamera(getActivity(), containerSession,
						CJayImage.TYPE_EXPORT);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			break;
		}
	}

	private void search(String searchText) {
		if (searchText.equals("")) {
			configureControls(mFeeds);

			if (null != mFeeds)
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

		boolean hasContainers = list != null && list.size() > 0;
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

	public void onEventMainThread(ContainerSessionChangedEvent event) {
		Logger.Log(LOG_TAG, "onEventMainThread ContainerSessionChangedEvent");
		refresh();
	}

	public void refresh() {
		Logger.Log(LOG_TAG, "onRefresh");

		mFeeds = (ArrayList<ContainerSession>) DataCenter.getInstance()
				.getListCheckOutContainerSessions(getActivity());

		// mFeedsAdapter.updateData(mFeeds);
		mSearchEditText.setText(""); // this will refresh the list
	}

	@Override
	public void onResume() {

		Logger.Log(LOG_TAG, "onResume " + LOG_TAG);

		if (mFeedsAdapter != null) {
			refresh();
		}

		super.onResume();
	}
}
