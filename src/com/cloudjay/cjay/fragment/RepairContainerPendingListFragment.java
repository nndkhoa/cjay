package com.cloudjay.cjay.fragment;

import java.sql.SQLException;
import java.util.ArrayList;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
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
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.MultiChoiceModeListener;
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
import com.cloudjay.cjay.adapter.IssueContainerCursorAdapter;
import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.events.ContainerRepairedEvent;
import com.cloudjay.cjay.events.ContainerSessionChangedEvent;
import com.cloudjay.cjay.events.PostLoadDataEvent;
import com.cloudjay.cjay.events.PreLoadDataEvent;
import com.cloudjay.cjay.model.ContainerSession;
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

@EFragment(R.layout.fragment_repair_container_pending)
@OptionsMenu(R.menu.menu_repair_container_pending)
public class RepairContainerPendingListFragment extends SherlockFragment
		implements OnRefreshListener, LoaderCallbacks<Cursor> {

	private final static int LOADER_ID = CJayConstant.CURSOR_LOADER_ID_REPAIR_PENDING;

	private ArrayList<ContainerSession> mSelectedContainerSessions;
	private ContainerSessionDaoImpl containerSessionDaoImpl = null;
	private int mItemLayout = R.layout.list_item_repair_container;

	PullToRefreshLayout mPullToRefreshLayout;
	IssueContainerCursorAdapter cursorAdapter;

	@SystemService
	InputMethodManager inputMethodManager;

	@ViewById(R.id.container_list)
	ListView mFeedListView;

	@ViewById(R.id.search_edittext)
	EditText mSearchEditText;

	@ViewById(R.id.ll_empty_element)
	LinearLayout mEmptyElement;

	@ViewById(R.id.ll_loading_data)
	LinearLayout mLoadMoreDataLayout;

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
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		Context context = getActivity();

		return new CJayCursorLoader(context) {
			@Override
			public Cursor loadInBackground() {
				Cursor cursor = DataCenter.getInstance()
						.getPendingContainerSessionCursor(getContext());

				if (cursor != null) {
					// Ensure the cursor window is filled
					cursor.getCount();
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
					return DataCenter.getInstance().filterPendingCursor(
							getActivity(), constraint);
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

		mFeedListView.setMultiChoiceModeListener(new MultiChoiceModeListener() {
			@Override
			public void onItemCheckedStateChanged(ActionMode mode,
					int position, long id, boolean checked) {
				// Here you can do something when items are
				// selected/de-selected,
				// such as update the title in the CAB
				mode.setTitle(String.valueOf(mFeedListView
						.getCheckedItemCount()));
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
			public boolean onCreateActionMode(ActionMode mode,
					android.view.Menu menu) {
				// Inflate the menu for the CAB
				MenuInflater inflater = mode.getMenuInflater();
				inflater.inflate(R.menu.menu_repair_container_pending, menu);
				return true;
			}

			@Override
			public void onDestroyActionMode(ActionMode mode) {
				// Here you can make any necessary updates to the activity when
				// the CAB is removed. By default, selected items are
				// deselected/unchecked.
			}

			@Override
			public boolean onPrepareActionMode(ActionMode mode,
					android.view.Menu menu) {
				// Here you can perform updates to the CAB due to
				// an invalidate() request
				return false;
			}
		});

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
		String operatorCode = "";

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

			break;
		}
	}

	@ItemClick(R.id.container_list)
	void listItemClicked(int position) {
		Intent intent = new Intent(getActivity(),
				RepairContainerActivity_.class);

		Cursor cursor = (Cursor) cursorAdapter.getItem(position);
		String uuidString = cursor.getString(cursor
				.getColumnIndexOrThrow(ContainerSession.FIELD_UUID));

		intent.putExtra(RepairContainerActivity_.CJAY_CONTAINER_SESSION_EXTRA,
				uuidString);

		startActivity(intent);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
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

		if (cursorAdapter != null) {
			refresh();
		}

		if (DataCenter.getInstance().isUpdating(getActivity()) == false) {
			Logger.Log("is not updating");
			mLoadMoreDataLayout.setVisibility(View.GONE);
		}

		super.onResume();
	}

	void setSelectedContainersFixed() {
		// loop through all the selected container sessions
		// and set each of them as fixed
		SparseBooleanArray selected = mFeedListView.getCheckedItemPositions();
		mSelectedContainerSessions = new ArrayList<ContainerSession>();

		for (int i = 0; i < selected.size(); i++) {
			if (selected.valueAt(i) == true) {

				Cursor cursor = (Cursor) cursorAdapter.getItem(selected
						.keyAt(i));
				String uuidString = cursor.getString(cursor
						.getColumnIndexOrThrow(ContainerSession.FIELD_UUID));
				ContainerSession containerSession;
				try {
					containerSession = containerSessionDaoImpl
							.findByUuid(uuidString);
					mSelectedContainerSessions.add(containerSession);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		for (ContainerSession containerSession : mSelectedContainerSessions) {
			setContainerFixed(containerSession);
		}

		// TODO: does it need to force refresh
		EventBus.getDefault().post(
				new ContainerRepairedEvent(mSelectedContainerSessions)); // Force
																			// refresh
	}

	void setContainerFixed(ContainerSession containerSession) {
		// set fixed to true
		containerSession.setFixed(true);

		// save db records
		try {
			containerSessionDaoImpl.createOrUpdate(containerSession);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void refresh() {
		getLoaderManager().restartLoader(LOADER_ID, null, this);
	}

	public void onEvent(ContainerRepairedEvent event) {
		Logger.Log("onEvent ContainerRepairedEvent");
		refresh();
	}

	public void onEventMainThread(ContainerSessionChangedEvent event) {
		Logger.Log("onEvent ContainerSessionChangedEvent");
		refresh();
	}

	public void onEvent(PreLoadDataEvent event) {
		Logger.Log("onEvent PreLoadDataEvent");
		mLoadMoreDataLayout.setVisibility(View.VISIBLE);
	}

	public void onEvent(PostLoadDataEvent event) {
		Logger.Log("onEvent PostLoadDataEvent");
		mLoadMoreDataLayout.setVisibility(View.GONE);
	}

}