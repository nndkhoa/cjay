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
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.ViewById;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.cloudjay.cjay.*;
import com.cloudjay.cjay.CJayActivity;
import com.cloudjay.cjay.CJayApplication;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.adapter.IssueContainerCursorAdapter;
import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.events.ContainerSessionChangedEvent;
import com.cloudjay.cjay.events.ContainerSessionEnqueueEvent;
import com.cloudjay.cjay.events.ListItemChangedEvent;
import com.cloudjay.cjay.events.PostLoadDataEvent;
import com.cloudjay.cjay.events.PreLoadDataEvent;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.network.CJayClient;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.CJayCursorLoader;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.NoConnectionException;
import com.cloudjay.cjay.util.NullSessionException;
import com.cloudjay.cjay.util.StringHelper;
import com.cloudjay.cjay.view.AddContainerDialog;

import de.greenrobot.event.EventBus;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

@EFragment(R.layout.fragment_auditor_reporting)
@OptionsMenu(R.menu.menu_auditor_reporting)
public class AuditorReportingListFragment extends SherlockFragment implements
		OnRefreshListener, LoaderCallbacks<Cursor> {

	public static final String LOG_TAG = "AuditorReportingListFragment";
	public static final int STATE_NOT_REPORTED = 0;
	public static final int STATE_REPORTING = 1;
	private int LOADER_ID;

	private ArrayList<Operator> mOperators;
	private int mState;
	private ContainerSession mSelectedContainerSession = null;
	private int mItemLayout = R.layout.list_item_audit_container;
	private ContainerSessionDaoImpl containerSessionDaoImpl = null;

	@SystemService
	InputMethodManager inputMethodManager;

	@ViewById(R.id.ll_empty_element)
	LinearLayout mEmptyElement;

	@ViewById(R.id.ll_loading_data)
	LinearLayout mLoadMoreDataLayout;

	@ViewById(R.id.container_list)
	ListView mFeedListView;

	@ViewById(R.id.search_edittext)
	EditText mSearchEditText;

	@ViewById(R.id.add_button)
	Button mAddButton;

	@ViewById(R.id.notfound_textview)
	TextView mNotfoundTextView;

	IssueContainerCursorAdapter cursorAdapter;
	PullToRefreshLayout mPullToRefreshLayout;

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

				Logger.Log("onRefreshStarted");

				try {
					DataCenter.getInstance().fetchData(getActivity());
					DataCenter.getDatabaseHelper(getActivity()).addUsageLog(
							"#refresh in fragment #AuditorReporting");
				} catch (NoConnectionException e) {
					((CJayActivity) getActivity())
							.showCrouton(R.string.alert_no_network);
					e.printStackTrace();
				} catch (NullSessionException e) {
					CJayApplication.logOutInstantly(getActivity());
					onDestroy();
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

		try {
			containerSessionDaoImpl = CJayClient.getInstance()
					.getDatabaseManager().getHelper(getActivity())
					.getContainerSessionDaoImpl();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		mSearchEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable arg0) {
				if (cursorAdapter != null) {
					cursorAdapter.getFilter().filter(arg0.toString());
				}
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
							inputMethodManager.hideSoftInputFromWindow(
									mSearchEditText.getWindowToken(), 0);
							return true;
						}
						return false;
					}
				});

		mOperators = (ArrayList<Operator>) DataCenter.getInstance()
				.getListOperators(getActivity());

		if (mState == STATE_REPORTING) {
			LOADER_ID = CJayConstant.CURSOR_LOADER_ID_AUDITOR_REPORTING;
		} else {
			LOADER_ID = CJayConstant.CURSOR_LOADER_ID_AUDITOR_NOT_REPORTED;
		}
		getLoaderManager().initLoader(LOADER_ID, null, this);

		mFeedListView.setTextFilterEnabled(true);
		mFeedListView.setScrollingCacheEnabled(false);
		mFeedListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

				if (scrollState != 0) {
					((IssueContainerCursorAdapter) mFeedListView.getAdapter()).isScrolling = true;
				} else {
					((IssueContainerCursorAdapter) mFeedListView.getAdapter()).isScrolling = false;
					((IssueContainerCursorAdapter) mFeedListView.getAdapter())
							.notifyDataSetChanged();
				}

				inputMethodManager.hideSoftInputFromWindow(
						mSearchEditText.getWindowToken(), 0);
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}
		});

		mFeedListView.setEmptyView(mEmptyElement);
	}

	int totalItems = 0;

	void setTotalItems(int val) {
		totalItems = val;
		int position;

		if (mState == STATE_REPORTING) {
			position = 1;
		} else {
			position = 0;
		}

		EventBus.getDefault().post(
				new ListItemChangedEvent(position, totalItems));
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		Context context = getActivity();

		return new CJayCursorLoader(context) {
			@Override
			public Cursor loadInBackground() {

				Cursor cursor = null;
				if (mState == STATE_REPORTING) {
					cursor = DataCenter.getInstance()
							.getReportingContainerSessionCursor(getContext());
				} else {
					cursor = DataCenter.getInstance()
							.getNotReportedContainerSessionCursor(getContext());
				}

				if (cursor != null) {
					// Ensure the cursor window is filled
					setTotalItems(cursor.getCount());
					cursor.registerContentObserver(mObserver);
				}

				return cursor;
			}
		};
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {

		if (cursorAdapter == null) {
			cursorAdapter = new IssueContainerCursorAdapter(getActivity(),
					mItemLayout, cursor, 0);

			cursorAdapter.setFilterQueryProvider(new FilterQueryProvider() {
				@Override
				public Cursor runQuery(CharSequence constraint) {

					Cursor cursor = null;
					if (mState == STATE_REPORTING) {
						cursor = DataCenter.getInstance()
								.filterReportingCursor(getActivity(),
										constraint);
					} else {
						cursor = DataCenter.getInstance()
								.filterNotReportedCursor(getActivity(),
										constraint);
					}

					return cursor;
				}
			});
			mFeedListView.setAdapter(cursorAdapter);

		} else {
			cursorAdapter.swapCursor(cursor);
		}

	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		cursorAdapter.swapCursor(null);
	}

	@OptionsItem(R.id.menu_upload)
	void uploadMenuItemSelected() {
		if (mSelectedContainerSession != null) {

			Logger.Log("Menu upload item clicked");
			if (mSelectedContainerSession
					.isValidForUpload(CJayImage.TYPE_REPORT)) {

				mSelectedContainerSession
						.setUploadType(ContainerSession.TYPE_AUDIT);
				CJayApplication.uploadContainerSesison(getActivity(),
						mSelectedContainerSession);

				// hide menu items
				hideMenuItems();
			} else {
				Crouton.cancelAllCroutons();
				Crouton.makeText(getActivity(),
						R.string.alert_no_issue_container, Style.ALERT).show();
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

		Cursor cursor = (Cursor) cursorAdapter.getItem(position);
		String uuidString = cursor.getString(cursor
				.getColumnIndexOrThrow(ContainerSession.FIELD_UUID));
		intent.putExtra(AuditorContainerActivity_.CJAY_CONTAINER_SESSION_EXTRA,
				uuidString);
		startActivity(intent);
	}

	@ItemLongClick(R.id.container_list)
	void listItemLongClicked(int position) {

		// refresh highlighting and menu
		mFeedListView.setItemChecked(position, true);
		Cursor cursor = (Cursor) cursorAdapter.getItem(position);
		String uuidString = cursor.getString(cursor
				.getColumnIndexOrThrow(ContainerSession.FIELD_UUID));
		try {
			mSelectedContainerSession = containerSessionDaoImpl
					.findByUuid(uuidString);
		} catch (SQLException e) {
			e.printStackTrace();
		}
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
		addContainerDialog.isOperatorRequired = false;
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

			Activity activity = getActivity();
			String currentTimeStamp = StringHelper
					.getCurrentTimestamp(CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE);

			String depotCode = "";
			if (getActivity() instanceof CJayActivity) {
				depotCode = ((CJayActivity) activity).getSession().getDepot()
						.getDepotCode();
			}

			ContainerSession containerSession = new ContainerSession(activity,
					containerId, operatorCode, currentTimeStamp, depotCode);
			containerSession.setOnLocal(true);

			try {
				containerSessionDaoImpl.addContainerSession(containerSession);
			} catch (SQLException e) {
				e.printStackTrace();
			}

			EventBus.getDefault().post(
					new ContainerSessionChangedEvent(containerSession));

			CJayApplication.gotoCamera(activity, containerSession,
					CJayImage.TYPE_REPORT, LOG_TAG);

			break;

		case AddContainerDialog.CONTAINER_DIALOG_EDIT:
			DataCenter.getInstance().editContainerSession(getActivity(),
					mSelectedContainerSession, containerId, operatorCode);
			break;
		}
	}

	public void onEvent(PreLoadDataEvent event) {
		Logger.Log("onEvent PreLoadDataEvent");
		mLoadMoreDataLayout.setVisibility(View.VISIBLE);
	}

	public void onEvent(PostLoadDataEvent event) {
		Logger.Log("onEvent PostLoadDataEvent");
		mLoadMoreDataLayout.setVisibility(View.GONE);
	}

	public void onEvent(ContainerSessionEnqueueEvent event) {
		Logger.Log("onEvent ContainerSessionEnqueueEvent");
		refresh();
	}

	public void onEventMainThread(ContainerSessionChangedEvent event) {
		Logger.Log("onEvent ContainerSessionChangedEvent");
		refresh();
	}

	public void refresh() {
		Logger.Log("onRefresh with LOADER_ID: " + Integer.toString(LOADER_ID));

		getLoaderManager().restartLoader(LOADER_ID, null, this);

	}

	void hideMenuItems() {
		mSelectedContainerSession = null;
		mFeedListView.setItemChecked(-1, true);
		getActivity().supportInvalidateOptionsMenu();
	}

	@Override
	public void onResume() {
		if (cursorAdapter != null) {
			refresh();
		}

		if (DataCenter.getInstance().isUpdating(getActivity()) == false) {
			Logger.Log("is not updating");
			mLoadMoreDataLayout.setVisibility(View.GONE);
		}
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