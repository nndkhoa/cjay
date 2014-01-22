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

import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.cloudjay.cjay.*;
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
import com.cloudjay.cjay.view.AddContainerDialog;
import com.cloudjay.cjay.view.AuditorContainerListView;

import de.greenrobot.event.EventBus;

@EFragment(R.layout.fragment_auditor_reporting)
@OptionsMenu(R.menu.menu_auditor_reporting)
public class AuditorReportingListFragment extends SherlockFragment implements
		OnRefreshListener {

	public static final String LOG_TAG = "AuditorReportingListFragment";

	public static final int STATE_NOT_REPORTED = 0;
	public static final int STATE_REPORTING = 1;

	private ArrayList<Operator> mOperators;
	private ArrayList<ContainerSession> mFeeds;
	private int mState = STATE_NOT_REPORTED;
	private ContainerSession mSelectedContainerSession;
	PullToRefreshLayout mPullToRefreshLayout;

	@ViewById(R.id.container_list)
	AuditorContainerListView mFeedListView;

	@ViewById(R.id.search_edittext)
	EditText mSearchEditText;

	@ViewById(R.id.add_button)
	Button mAddButton;

	@ViewById(R.id.notfound_textview)
	TextView mNotfoundTextView;

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

					Thread.sleep(CJayConstant.SIMULATED_REFRESH_LENGTH);
				} catch (InterruptedException e) {
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
			containerId = "";
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

	public void onEvent(ContainerSessionEnqueueEvent event) {
		Logger.Log(LOG_TAG, "onEvent ContainerSessionEnqueueEvent");
		refresh();
	}

	public void onEventMainThread(ContainerSessionChangedEvent event) {
		Logger.Log(LOG_TAG, "onEvent ContainerSessionChangedEvent");
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

			// mFeedListView.setFeeds(mFeeds); // --> already setFeeds inside
			// setText("");
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