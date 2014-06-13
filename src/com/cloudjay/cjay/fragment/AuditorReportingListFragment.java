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
import com.cloudjay.cjay.*;
import com.cloudjay.cjay.adapter.IssueContainerCursorAdapter;
import com.cloudjay.cjay.events.ContainerSessionChangedEvent;
import com.cloudjay.cjay.events.ContainerSessionEnqueueEvent;
import com.cloudjay.cjay.events.ContainerSessionUpdatedEvent;
import com.cloudjay.cjay.events.ListItemChangedEvent;
import com.cloudjay.cjay.events.PostLoadDataEvent;
import com.cloudjay.cjay.events.PreLoadDataEvent;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.Container;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.Operator;
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

@EFragment(R.layout.fragment_auditor_reporting)
@OptionsMenu(R.menu.menu_auditor_reporting)
public class AuditorReportingListFragment extends SherlockFragment implements OnRefreshListener,
																	LoaderCallbacks<Cursor> {

	public static final String LOG_TAG = "AuditorReportingListFragment";
	public static final int STATE_NOT_REPORTED = 0;
	public static final int STATE_REPORTING = 1;
	private int LOADER_ID;

	private ArrayList<Operator> mOperators;
	private int mState;

	String mSelectedUuid = "";
	String mSelectedContainerId = "";

	private int mItemLayout = R.layout.list_item_audit_container;

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

		// TODO: can optimize, by keep it already alive inside memory
		mOperators = (ArrayList<Operator>) DataCenter.getInstance().getListOperators(getActivity());

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

	void hideMenuItems() {
		mSelectedUuid = "";
		mSelectedContainerId = "";
		mFeedListView.setItemChecked(-1, true);
		getActivity().supportInvalidateOptionsMenu();
	}

	@ItemClick(R.id.container_list)
	void listItemClicked(int position) {
		// clear current selection
		hideMenuItems();
		Intent intent = new Intent(getActivity(), AuditorContainerActivity_.class);

		Cursor cursor = (Cursor) cursorAdapter.getItem(position);
		String uuidString = cursor.getString(cursor.getColumnIndexOrThrow(ContainerSession.FIELD_UUID));
		intent.putExtra(AuditorContainerActivity_.CJAY_CONTAINER_SESSION_EXTRA, uuidString);
		startActivity(intent);
	}

	@ItemLongClick(R.id.container_list)
	void listItemLongClicked(int position) {

		// refresh highlighting and menu
		mFeedListView.setItemChecked(position, true);

		Cursor cursor = (Cursor) cursorAdapter.getItem(position);
		mSelectedUuid = cursor.getString(cursor.getColumnIndexOrThrow(ContainerSession.FIELD_UUID));
		mSelectedContainerId = cursor.getString(cursor.getColumnIndexOrThrow(Container.CONTAINER_ID));

		getActivity().supportInvalidateOptionsMenu();
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

				// CJayApplication.gotoCamera(activity, containerSession,
				// CJayImage.TYPE_REPORT, LOG_TAG);

				Intent intent = new Intent(getActivity(), AuditorContainerActivity_.class);
				intent.putExtra(AuditorContainerActivity_.CJAY_CONTAINER_SESSION_EXTRA, containerSession.getUuid());
				intent.putExtra(AuditorContainerActivity_.START_CAMERA_EXTRA, true);
				startActivity(intent);

				break;

			case AddContainerDialog.CONTAINER_DIALOG_EDIT:
				DataCenter.getInstance().editContainer(getActivity(), mSelectedUuid, containerId, operatorCode);
				break;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		EventBus.getDefault().register(this);
		super.onCreate(savedInstanceState);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		Context context = getActivity();

		return new CJayCursorLoader(context) {
			@Override
			public Cursor loadInBackground() {

				Cursor cursor = null;
				if (mState == STATE_REPORTING) {
					cursor = DataCenter.getInstance().getReportingContainerSessionCursor(getContext());
				} else {
					cursor = DataCenter.getInstance().getNotReportedContainerSessionCursor(getContext());
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
	public void onDestroy() {
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}

	public void onEvent(ContainerSessionUpdatedEvent event) {
		refresh();
	}

	public void onEvent(ContainerSessionEnqueueEvent event) {
		Logger.Log("onEvent ContainerSessionEnqueueEvent");
		refresh();
	}

	public void onEvent(PostLoadDataEvent event) {
		Logger.Log("onEvent PostLoadDataEvent");
		mLoadMoreDataLayout.setVisibility(View.GONE);
	}

	public void onEvent(PreLoadDataEvent event) {
		Logger.Log("onEvent PreLoadDataEvent");
		mLoadMoreDataLayout.setVisibility(View.VISIBLE);
	}

	public void onEventMainThread(ContainerSessionChangedEvent event) {
		Logger.Log("onEvent ContainerSessionChangedEvent");
		refresh();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		cursorAdapter.swapCursor(null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {

		if (cursorAdapter == null) {
			cursorAdapter = new IssueContainerCursorAdapter(getActivity(), mItemLayout, cursor, 0, true);

			cursorAdapter.setFilterQueryProvider(new FilterQueryProvider() {
				@Override
				public Cursor runQuery(CharSequence constraint) {

					Cursor cursor = null;
					if (mState == STATE_REPORTING) {
						cursor = DataCenter.getInstance().filterReportingCursor(getActivity(), constraint);
					} else {
						cursor = DataCenter.getInstance().filterNotReportedCursor(getActivity(), constraint);
					}

					return cursor;
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

		if (mState == STATE_REPORTING) {
			boolean isDisplayed = !TextUtils.isEmpty(mSelectedUuid);
			menu.findItem(R.id.menu_upload).setVisible(isDisplayed);
			menu.findItem(R.id.menu_refresh_item).setVisible(false);
		} else {

			boolean isDisplayed = !TextUtils.isEmpty(mSelectedUuid);
			menu.findItem(R.id.menu_upload).setVisible(false);
			menu.findItem(R.id.menu_refresh_item).setVisible(isDisplayed);
		}
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
					DataCenter.getDatabaseHelper(getActivity()).addUsageLog("#refresh in fragment #AuditorReporting");
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
		Logger.Log("onRefresh with LOADER_ID: " + Integer.toString(LOADER_ID));

		getLoaderManager().restartLoader(LOADER_ID, null, this);

	}

	public void setState(int state) {
		mState = state;
	}

	void setTotalItems(int val) {
		totalItems = val;
		int position;

		if (mState == STATE_REPORTING) {
			position = 1;
		} else {
			position = 0;
		}

		EventBus.getDefault().post(new ListItemChangedEvent(position, totalItems));
	}

	@OptionsItem(R.id.menu_upload)
	void uploadMenuItemSelected() {
		if (!TextUtils.isEmpty(mSelectedUuid)) {

			Logger.Log("Menu upload item clicked");
			if (Utils.isValidForUpload(getActivity(), mSelectedUuid, CJayImage.TYPE_AUDIT)) {

				QueryHelper.update(	getActivity(), "container_session", ContainerSession.FIELD_UPLOAD_TYPE,
									Integer.toString(UploadType.AUDIT.getValue()), ContainerSession.FIELD_UUID + " = "
											+ Utils.sqlString(mSelectedUuid));

				CJayApplication.uploadContainer(getActivity(), mSelectedUuid, mSelectedContainerId);

				// hide menu items
				hideMenuItems();
			} else {
				Crouton.cancelAllCroutons();
				Crouton.makeText(getActivity(), R.string.alert_no_issue_container, Style.ALERT).show();
			}
		}
	}

	@OptionsItem(R.id.menu_refresh_item)
	void refreshMenuItemSelected() {
		if (!TextUtils.isEmpty(mSelectedUuid)) {

			SQLiteDatabase db = DataCenter.getDatabaseHelper(getActivity()).getWritableDatabase();
			Cursor cursor = db.rawQuery("select * from container_session where _id = ?",
										new String[] { mSelectedContainerId });
			int tmp = -1;
			if (cursor.moveToFirst()) {
				tmp = cursor.getInt(cursor.getColumnIndexOrThrow(ContainerSession.FIELD_ID));
			}

			cursor.close();
			final int id = tmp;

			Logger.Log("Menu upload item clicked");
			new AsyncTask<Void, Integer, Void>() {
				@Override
				protected Void doInBackground(Void... params) {

					try {
						DataCenter.getInstance().updateContainerSessionById(getActivity(), id, mSelectedUuid);
					} catch (NoConnectionException e) {
						Crouton.cancelAllCroutons();
						Crouton.makeText(getActivity(), R.string.alert_no_network, Style.ALERT).show();

					} catch (NullSessionException e) {
						CJayApplication.logOutInstantly(getActivity());
						getActivity().finish();
					}

					return null;
				}
			}.execute();

			hideMenuItems();
		}
	}
}