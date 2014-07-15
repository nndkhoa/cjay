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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.cloudjay.cjay.CJayActivity;
import com.cloudjay.cjay.CJayApplication;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.adapter.GateExportContainerCursorAdapter;
import com.cloudjay.cjay.events.ContainerSessionChangedEvent;
import com.cloudjay.cjay.events.ContainerSessionEnqueueEvent;
import com.cloudjay.cjay.events.ContainerSessionUpdatedEvent;
import com.cloudjay.cjay.events.ListItemChangedEvent;
import com.cloudjay.cjay.events.PostLoadDataEvent;
import com.cloudjay.cjay.events.PreLoadDataEvent;
import com.cloudjay.cjay.events.UploadStateRestoredEvent;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.Container;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.CJayCursorLoader;
import com.cloudjay.cjay.util.ContainerState;
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

@EFragment(R.layout.fragment_gate_export)
@OptionsMenu(R.menu.menu_gate_export)
public class GateExportListFragment extends SherlockFragment implements OnRefreshListener, LoaderCallbacks<Cursor> {

	public final static String LOG_TAG = "GateExportListFragment";
	private final static int LOADER_ID = CJayConstant.CURSOR_LOADER_ID_GATE_EXPORT;

	private ArrayList<Operator> mOperators;

	String mSelectedUuid = "";
	String mSelectedContainerId = "";
	ContainerState mSelectedState = null;

	private final int mItemLayout = R.layout.list_item_container;
	private int mCurrentPosition = -1;

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

	PullToRefreshLayout mPullToRefreshLayout;
	GateExportContainerCursorAdapter cursorAdapter;

	int totalItems = 0;

	public GateExportListFragment() {
	}

	@Click(R.id.add_button)
	void addButtonClicked() {

		// show add container dialog`
		String containerId;
		if (!TextUtils.isEmpty(mSearchEditText.getText().toString())) {
			containerId = mSearchEditText.getText().toString();
		} else {
			containerId = "";
		}

		CJayApplication.openContainerDetailDialog(this, containerId, "", AddContainerDialog.CONTAINER_DIALOG_ADD);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {

		Context context = getActivity();

		return new CJayCursorLoader(context) {

			@Override
			public Cursor loadInBackground() {

				// Cursor totalCursor = DataCenter.getInstance().getCheckOutContainerSessionCursor(getContext());
				// setTotalItems(totalCursor.getCount());
				// totalCursor.close();

				new AsyncTask<Void, Void, Void>() {
					@Override
					protected Void doInBackground(Void... params) {
						refreshTotalCount(); // refresh the total number containers. Run async
						return null;
					}
				}.execute();

				Cursor cursor = DataCenter.getInstance().getValidCheckOutContainerCursor(getContext());

				if (cursor != null) {
					cursor.getCount(); // Ensure the cursor window is filled
				}

				return cursor;
			}
		};
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {

		if (cursorAdapter == null) {

			cursorAdapter = new GateExportContainerCursorAdapter(getActivity(), mItemLayout, cursor, 0);
			cursorAdapter.setFilterQueryProvider(new FilterQueryProvider() {

				@Override
				public Cursor runQuery(CharSequence constraint) {

					if (TextUtils.isEmpty(constraint)) { return DataCenter.getInstance()
																			.getValidCheckOutContainerCursor(	getActivity()); }

					return DataCenter.getInstance().filterCheckoutCursor(getActivity(), constraint);
				}
			});

			// ####
			mFeedListView.setAdapter(cursorAdapter);
		} else {
			cursorAdapter.swapCursor(cursor);
		}
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

		mOperators = (ArrayList<Operator>) DataCenter.getInstance().getListOperators(getActivity());

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
					((GateExportContainerCursorAdapter) mFeedListView.getAdapter()).isScrolling = true;
				} else {
					((GateExportContainerCursorAdapter) mFeedListView.getAdapter()).isScrolling = false;
					((GateExportContainerCursorAdapter) mFeedListView.getAdapter()).notifyDataSetChanged();
				}

				inputMethodManager.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);
			}
		});

		mFeedListView.setEmptyView(mEmptyElement);
		mFeedListView.setLongClickable(true);
	}

	void hideMenuItems() {

		mSelectedUuid = "";
		mSelectedContainerId = "";
		mSelectedState = null;

		mFeedListView.setItemChecked(-1, true);
		getActivity().supportInvalidateOptionsMenu();
	}

	@ItemClick(R.id.container_list)
	void listItemClicked(int position) {

		long startTime = System.currentTimeMillis();

		hideMenuItems();
		Cursor cursor = (Cursor) cursorAdapter.getItem(position);
		ContainerState state = ContainerState.values()[cursor.getInt(cursor.getColumnIndexOrThrow(ContainerSession.FIELD_SERVER_STATE))];
		String containerId = cursor.getString(cursor.getColumnIndexOrThrow("container_id"));
		String uuid = cursor.getString(cursor.getColumnIndexOrThrow("_id"));

		if (state != ContainerState.AVAILABLE) {
			Logger.Log("User cannot open this container");

			Crouton.cancelAllCroutons();
			Crouton.makeText(getActivity(), R.string.alert_cannot_export_container, Style.ALERT).show();

		} else {

			Logger.Log("Click on container " + containerId + " | " + state.name());
			mCurrentPosition = position;
			handleContainerClicked(uuid, containerId);

		}

		long difference = System.currentTimeMillis() - startTime;
		Logger.w("---> Total time: " + Long.toString(difference));
		// 88 --> 90ms
	}

	@ItemLongClick(R.id.container_list)
	void listItemLongClicked(int position) {

		// refresh highlighting and menu
		mFeedListView.setItemChecked(position, true);

		Cursor cursor = (Cursor) cursorAdapter.getItem(position);
		mSelectedUuid = cursor.getString(cursor.getColumnIndexOrThrow(ContainerSession.FIELD_UUID));
		mSelectedContainerId = cursor.getString(cursor.getColumnIndexOrThrow(Container.CONTAINER_ID));
		mSelectedState = ContainerState.values()[cursor.getInt(cursor.getColumnIndexOrThrow(ContainerSession.FIELD_SERVER_STATE))];
		getActivity().supportInvalidateOptionsMenu();
		Logger.Log("mSelectedContainerSession: " + mSelectedContainerId);

	}

	void handleContainerClicked(String uuid, String containerId) {

		long startTime = System.currentTimeMillis();

		// find the last role that took picture of this issue
		SQLiteDatabase db = DataCenter.getDatabaseHelper(getActivity().getApplicationContext()).getWritableDatabase();
		String sql = "SELECT type FROM cjay_image WHERE containerSession_id LIKE ? AND type <> ? ORDER BY time_posted DESC LIMIT 1";
		Cursor imageCursor = db.rawQuery(sql, new String[] { uuid, String.valueOf(CJayImage.TYPE_EXPORT) });
		int lastRole = -1;
		if (imageCursor.moveToFirst()) {
			lastRole = imageCursor.getInt(imageCursor.getColumnIndexOrThrow("type"));
		}

		imageCursor.close();

		long difference = System.currentTimeMillis() - startTime;
		Logger.w("---> Total time: " + Long.toString(difference));

		// show images
		if (lastRole >= 0 && lastRole != CJayImage.TYPE_EXPORT) {
			CJayApplication.openPhotoGridView(	getActivity(), uuid, containerId, CJayImage.TYPE_EXPORT, lastRole,
												GateExportListFragment_.LOG_TAG);
		} else {
			CJayApplication.openPhotoGridView(	getActivity(), uuid, containerId, CJayImage.TYPE_EXPORT,
												GateExportListFragment_.LOG_TAG);
		}
	}

	@OptionsItem(R.id.menu_upload)
	void uploadMenuItemSelected() {

		synchronized (this) {
			if (!TextUtils.isEmpty(mSelectedUuid)) {

				if (Utils.isValidForUpload(getActivity(), mSelectedUuid, CJayImage.TYPE_EXPORT)) {

					String currentTime = StringHelper.getCurrentTimestamp(CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE);
					String[] fields = { ContainerSession.FIELD_UPLOAD_TYPE, ContainerSession.FIELD_CHECK_OUT_TIME };
					String[] values = { Integer.toString(UploadType.OUT.getValue()), currentTime };
					QueryHelper.update(getActivity(), "container_session", fields, values, ContainerSession.FIELD_UUID
							+ " = " + Utils.sqlString(mSelectedUuid));

					CJayApplication.uploadContainer(getActivity(), mSelectedUuid, mSelectedContainerId);
					DataCenter.getDatabaseHelper(getActivity())
								.addUsageLog(	getActivity(),
												mSelectedContainerId
														+ " | Prepare to add #OUT container to upload queue");

					hideMenuItems();
				} else {
					Crouton.cancelAllCroutons();
					Crouton.makeText(getActivity(), R.string.alert_no_issue_container, Style.ALERT).show();
				}
			}
		}
		hideMenuItems();
	}

	@OptionsItem(R.id.menu_av_export)
	void exportNonAvailable() {

		if (!TextUtils.isEmpty(mSelectedUuid)) {
			handleContainerClicked(mSelectedUuid, mSelectedContainerId);
		}

		hideMenuItems();
	}

	public void OnContainerInputCompleted(String containerId, String operatorName, int mode) {

		// Get the container id and container operator code
		String operatorCode = "";
		for (Operator operator : mOperators) {
			if (operator.getName().equals(operatorName)) {
				operatorCode = operator.getCode();
				break;
			}
		}

		if (TextUtils.isEmpty(containerId) || TextUtils.isEmpty(operatorCode)) return;

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
				containerSession.setExport(true);

				try {
					DataCenter.getDatabaseHelper(getActivity()).getContainerSessionDaoImpl()
								.addContainerSession(containerSession);
				} catch (SQLException e) {
					e.printStackTrace();
				}

				EventBus.getDefault().post(new ContainerSessionChangedEvent(containerSession));
				CJayApplication.openCamera(activity, containerSession.getUuid(), CJayImage.TYPE_EXPORT, LOG_TAG);

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

	public void onEvent(ContainerSessionEnqueueEvent event) {
		Logger.Log("ContainerSessionEnqueueEvent");
		refresh();
	}

	public void onEvent(PostLoadDataEvent event) {
		mLoadMoreDataLayout.setVisibility(View.GONE);
	}

	public void onEvent(PreLoadDataEvent event) {
		mLoadMoreDataLayout.setVisibility(View.VISIBLE);
	}

	public void onEventMainThread(ContainerSessionChangedEvent event) {
		refresh();
	}

	public void onEvent(UploadStateRestoredEvent event) {
		refresh();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		cursorAdapter.swapCursor(null);
	}

	public void OnOperatorSelected(String containerId, String operatorName, int mode) {
		CJayApplication.openContainerDetailDialog(this, containerId, operatorName, mode);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		if (TextUtils.isEmpty(mSelectedUuid)) {
			menu.findItem(R.id.menu_upload).setVisible(false);
			menu.findItem(R.id.menu_av_export).setVisible(false);
		} else if (mSelectedState != ContainerState.AVAILABLE) {
			menu.findItem(R.id.menu_upload).setVisible(false);
			menu.findItem(R.id.menu_av_export).setVisible(true);
		} else {
			menu.findItem(R.id.menu_upload).setVisible(true);
			menu.findItem(R.id.menu_av_export).setVisible(false);
		}
	}

	@Override
	public void onRefreshStarted(View view) {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {

				Logger.Log("onRefreshStarted");

				try {
					DataCenter.getInstance().fetchData(getActivity(), true);
					DataCenter.getDatabaseHelper(getActivity()).addUsageLog(getActivity(),
																			"#refresh in fragment #GateExport");

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

		if (!TextUtils.isEmpty(mSelectedUuid)) {
			mFeedListView.setItemChecked(mCurrentPosition, true);
			Logger.d(mSelectedContainerId);
		}

		if (cursorAdapter != null) {
			refresh();
		}

		if (!DataCenter.getInstance().isUpdating(getActivity())) {
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
								.theseChildrenArePullable(R.id.container_list).listener(this)
								.setup(mPullToRefreshLayout);

	}

	public void refresh() {
		getLoaderManager().restartLoader(LOADER_ID, null, this);
	}

	void refreshTotalCount() {

		// Reresh the total number of export containers.
		// The query takes a while to run, hence run in background

		Cursor totalCursor = DataCenter.getInstance().getCheckOutContainerSessionCursor(getActivity());
		setTotalItems(totalCursor.getCount());
		totalCursor.close();

	}

	void setTotalItems(int val) {
		totalItems = val;
		EventBus.getDefault().post(new ListItemChangedEvent(1, totalItems));
	}
}
