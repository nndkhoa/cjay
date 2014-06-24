package com.cloudjay.cjay.fragment;

import java.sql.SQLException;
import java.util.HashMap;
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
import com.cloudjay.cjay.events.ContainerRepairedEvent;
import com.cloudjay.cjay.events.ContainerSessionChangedEvent;
import com.cloudjay.cjay.events.ContainerSessionEnqueueEvent;
import com.cloudjay.cjay.events.ContainerSessionUpdatedEvent;
import com.cloudjay.cjay.events.ListItemChangedEvent;
import com.cloudjay.cjay.events.PostLoadDataEvent;
import com.cloudjay.cjay.events.PreLoadDataEvent;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.Container;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.CJayCursorLoader;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.NoConnectionException;
import com.cloudjay.cjay.util.NullSessionException;
import com.cloudjay.cjay.util.QueryHelper;
import com.cloudjay.cjay.util.StringHelper;
import com.cloudjay.cjay.util.UploadType;
import com.cloudjay.cjay.util.Utils;
import com.cloudjay.cjay.view.AddContainerDialog;

import de.greenrobot.event.EventBus;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

@EFragment(R.layout.fragment_repair_container_pending)
@OptionsMenu(R.menu.menu_repair_container_pending)
public class RepairContainerPendingListFragment extends SherlockFragment implements OnRefreshListener,
																		LoaderCallbacks<Cursor> {

	private final static int LOADER_ID = CJayConstant.CURSOR_LOADER_ID_REPAIR_PENDING;
	HashMap<String, String> mSelectedItems;
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

	int totalItems = 0;

	@Click(R.id.add_button)
	void addButtonClicked() {

		// show add container dialog
		String containerId;
		if (!TextUtils.isEmpty(mSearchEditText.getText().toString())) {
			containerId = mSearchEditText.getText().toString();
		} else {
			containerId = "";
		}
		CJayApplication.openContainerDetailDialog(this, containerId, "", false, AddContainerDialog.CONTAINER_DIALOG_ADD);
	}

	@AfterViews
	void afterViews() {

		mSearchEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable arg0) {
				if (cursorAdapter != null) {
					cursorAdapter.getFilter().filter(arg0.toString());
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
		});

		mSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
				if (id == EditorInfo.IME_ACTION_SEARCH) {
					inputMethodManager.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);
					return true;
				}
				return false;
			}
		});

		mFeedListView.setMultiChoiceModeListener(new MultiChoiceModeListener() {

			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

				// Respond to clicks on the actions in the CAB
				switch (item.getItemId()) {
				// case R.id.menu_check:
				// setSelectedContainersFixed();
				// mode.finish(); // Action picked, so close the CAB
				// return true;

					case R.id.menu_upload:

						SparseBooleanArray selected = mFeedListView.getCheckedItemPositions();
						mSelectedItems = new HashMap<String, String>();
						HashMap<String, String> invalidItems = new HashMap<String, String>();

						for (int i = 0; i < selected.size(); i++) {
							if (selected.valueAt(i) == true) {
								Cursor cursor = (Cursor) cursorAdapter.getItem(selected.keyAt(i));
								String uuidString = cursor.getString(cursor.getColumnIndexOrThrow(ContainerSession.FIELD_UUID));
								String containerId = cursor.getString(cursor.getColumnIndexOrThrow(Container.CONTAINER_ID));

								mSelectedItems.put(uuidString, containerId);
							}
						}

						for (String key : mSelectedItems.keySet()) {

							if (Utils.isValidForUpload(getActivity(), key, CJayImage.TYPE_REPAIRED)) {

								QueryHelper.update(	getActivity(), "container_session",
													ContainerSession.FIELD_UPLOAD_TYPE,
													Integer.toString(UploadType.REPAIR.getValue()),
													ContainerSession.FIELD_UUID + " = " + Utils.sqlString(key));
								CJayApplication.uploadContainer(getActivity(), key, mSelectedItems.get(key));

							} else {
								Logger.w("Container " + mSelectedItems.get(key) + " is invalid for upload");
								invalidItems.put(key, mSelectedItems.get(key));
							}
						}

						if (invalidItems.size() > 0) {
							Crouton.makeText(getActivity(), R.string.alert_no_issue_container, Style.ALERT).show();
						}

						mode.finish();
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
				// the CAB is removed. By default, selected items are
				// deselected/unchecked.
			}

			@Override
			public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
				// Here you can do something when items are
				// selected/de-selected,
				// such as update the title in the CAB
				mode.setTitle(String.valueOf(mFeedListView.getCheckedItemCount()));
			}

			@Override
			public boolean onPrepareActionMode(ActionMode mode, android.view.Menu menu) {
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
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState != 0) {
					((IssueContainerCursorAdapter) mFeedListView.getAdapter()).isScrolling = true;
				} else {
					((IssueContainerCursorAdapter) mFeedListView.getAdapter()).isScrolling = false;
					((IssueContainerCursorAdapter) mFeedListView.getAdapter()).notifyDataSetChanged();
				}

				inputMethodManager.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);
			}
		});

		mFeedListView.setEmptyView(mEmptyElement);
	}

	@ItemClick(R.id.container_list)
	void listItemClicked(int position) {

		Cursor cursor = (Cursor) cursorAdapter.getItem(position);
		String uuidString = cursor.getString(cursor.getColumnIndexOrThrow(ContainerSession.FIELD_UUID));
		Intent intent = new Intent(getActivity(), RepairContainerActivity_.class);
		intent.putExtra(RepairContainerActivity_.CJAY_CONTAINER_SESSION_EXTRA, uuidString);
		startActivity(intent);
	}

	public void OnContainerInputCompleted(String containerId, String operatorName, int mode) {
		String operatorCode = "";

		switch (mode) {
			case AddContainerDialog.CONTAINER_DIALOG_ADD:

				Activity activity = getActivity();
				String currentTimeStamp = StringHelper.getCurrentTimestamp(CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE);

				String depotCode = "";
				if (getActivity() instanceof CJayActivity) {
					depotCode = ((CJayActivity) activity).getSession().getDepot().getDepotCode();
				}

				ContainerSession containerSession = new ContainerSession(activity, containerId, operatorCode,
																			currentTimeStamp, depotCode);
				containerSession.setOnLocal(true);

				try {
					DataCenter.getDatabaseHelper(getActivity()).getContainerSessionDaoImpl()
								.addContainerSession(containerSession);
				} catch (SQLException e) {
					e.printStackTrace();
				}

				EventBus.getDefault().post(new ContainerSessionChangedEvent(containerSession));

				break;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		EventBus.getDefault().register(this);
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onDestroy() {
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}

	public void onEvent(ContainerSessionUpdatedEvent event) {
		refresh();
	}

	public void onEvent(ContainerRepairedEvent event) {
		Logger.Log("onEvent ContainerRepairedEvent");
		refresh();
	}

	public void onEvent(PostLoadDataEvent event) {
		// Logger.Log("onEvent PostLoadDataEvent");
		mLoadMoreDataLayout.setVisibility(View.GONE);
	}

	public void onEvent(PreLoadDataEvent event) {
		// Logger.Log("onEvent PreLoadDataEvent");
		mLoadMoreDataLayout.setVisibility(View.VISIBLE);
	}

	public void onEventMainThread(ContainerSessionChangedEvent event) {
		Logger.Log("onEvent ContainerSessionChangedEvent");
		refresh();
	}

	public void onEventMainThread(ContainerSessionEnqueueEvent event) {
		refresh();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		Context context = getActivity();

		return new CJayCursorLoader(context) {
			@Override
			public Cursor loadInBackground() {
				Cursor cursor = DataCenter.getInstance().getPendingContainerSessionCursor(getContext());

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
	public void onLoaderReset(Loader<Cursor> arg0) {
		cursorAdapter.swapCursor(null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		if (cursorAdapter == null) {
			cursorAdapter = new IssueContainerCursorAdapter(getActivity(), mItemLayout, cursor, 0);

			cursorAdapter.setFilterQueryProvider(new FilterQueryProvider() {
				@Override
				public Cursor runQuery(CharSequence constraint) {
					return DataCenter.getInstance().filterPendingCursor(getActivity(), constraint);
				}
			});

			mFeedListView.setAdapter(cursorAdapter);

		} else {
			cursorAdapter.swapCursor(cursor);
		}

	}

	public void OnOperatorSelected(String containerId, String operatorName, int mode) {
		CJayApplication.openContainerDetailDialog(this, containerId, operatorName, false, mode);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {

		super.onPrepareOptionsMenu(menu);
		// menu.findItem(R.id.menu_check).setVisible(false);
		menu.findItem(R.id.menu_upload).setVisible(false);
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
					DataCenter.getInstance().fetchData(getActivity(), true);
					DataCenter.getDatabaseHelper(getActivity())
								.addUsageLog("#refresh in fragment #RepairContainerPending");
				} catch (NoConnectionException e) {
					((CJayActivity) getActivity()).showCrouton(R.string.alert_no_network);
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
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		ViewGroup viewGroup = (ViewGroup) view;
		mPullToRefreshLayout = new PullToRefreshLayout(viewGroup.getContext());
		ActionBarPullToRefresh.from(getActivity()).insertLayoutInto(viewGroup)
								.theseChildrenArePullable(R.id.container_list, android.R.id.empty).listener(this)
								.setup(mPullToRefreshLayout);
	}

	public void refresh() {
		getLoaderManager().restartLoader(LOADER_ID, null, this);
	}

	void setSelectedContainersFixed() {

		// loop through all the selected container sessions
		// and set each of them as fixed
		SparseBooleanArray selected = mFeedListView.getCheckedItemPositions();
		mSelectedItems = new HashMap<String, String>();

		for (int i = 0; i < selected.size(); i++) {
			if (selected.valueAt(i) == true) {
				Cursor cursor = (Cursor) cursorAdapter.getItem(selected.keyAt(i));
				String uuidString = cursor.getString(cursor.getColumnIndexOrThrow(ContainerSession.FIELD_UUID));
				String containerId = cursor.getString(cursor.getColumnIndexOrThrow(Container.CONTAINER_ID));
				mSelectedItems.put(uuidString, containerId);
			}
		}

		for (String key : mSelectedItems.keySet()) {
			QueryHelper.update(	getActivity(), "container_session", ContainerSession.FIELD_FIXED, "1",
								ContainerSession.FIELD_UUID + " = " + Utils.sqlString(key));
		}

		// TODO: does it need to force refresh?
		EventBus.getDefault().post(new ContainerRepairedEvent()); // Force refresh
	}

	void setTotalItems(int val) {
		totalItems = val;
		EventBus.getDefault().post(new ListItemChangedEvent(0, totalItems));
	}

}