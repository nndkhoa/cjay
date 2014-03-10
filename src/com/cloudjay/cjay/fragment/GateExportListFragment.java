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
import com.cloudjay.cjay.CJayActivity;
import com.cloudjay.cjay.CJayApplication;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.adapter.GateContainerCursorAdapter;
import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.events.ContainerSessionChangedEvent;
import com.cloudjay.cjay.events.ContainerSessionEnqueueEvent;
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
import com.cloudjay.cjay.util.StringHelper;
import com.cloudjay.cjay.view.AddContainerDialog;
import de.greenrobot.event.EventBus;

@EFragment(R.layout.fragment_gate_export)
@OptionsMenu(R.menu.menu_gate_export)
public class GateExportListFragment extends SherlockFragment implements
		OnRefreshListener, LoaderCallbacks<Cursor> {

	private final static String LOG_TAG = "GateExportListFragment";
	private final static int LOADER_ID = 0;
	private ArrayList<Operator> mOperators;
	private ContainerSession mSelectedContainerSession = null;
	private ContainerSessionDaoImpl containerSessionDaoImpl = null;
	private int mItemLayout = R.layout.list_item_container;

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
	GateContainerCursorAdapter cursorAdapter;

	public GateExportListFragment() {
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		ViewGroup viewGroup = (ViewGroup) view;

		// As we're using a ListFragment we create a PullToRefreshLayout
		// manually
		mPullToRefreshLayout = new PullToRefreshLayout(viewGroup.getContext());

		// We can now setup the PullToRefreshLayout
		ActionBarPullToRefresh
				.from(getActivity())
				// We need to insert the PullToRefreshLayout into the Fragment's
				// ViewGroup
				.insertLayoutInto(viewGroup)
				// Here we mark just the ListView and it's Empty View as
				// pullable
				.theseChildrenArePullable(R.id.container_list,
						android.R.id.empty).listener(this)
				.setup(mPullToRefreshLayout);
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

		getLoaderManager().initLoader(LOADER_ID, null, this);

		mFeedListView.setTextFilterEnabled(true);
		mFeedListView.setScrollingCacheEnabled(false);

		mFeedListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState != 0) {
					((GateContainerCursorAdapter) mFeedListView.getAdapter()).isScrolling = true;
				} else {
					((GateContainerCursorAdapter) mFeedListView.getAdapter()).isScrolling = false;
					((GateContainerCursorAdapter) mFeedListView.getAdapter())
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

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		Context context = getActivity();

		return new CJayCursorLoader(context) {
			@Override
			public Cursor loadInBackground() {
				Cursor cursor = DataCenter.getInstance()
						.getCheckOutContainerSessionCursor(getContext());

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
			cursorAdapter = new GateContainerCursorAdapter(getActivity(),
					mItemLayout, cursor, 0);

			cursorAdapter.setFilterQueryProvider(new FilterQueryProvider() {
				@Override
				public Cursor runQuery(CharSequence constraint) {
					return DataCenter.getInstance().filterCheckoutCursor(
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

					Logger.Log("Menu upload item clicked");

					// User confirm upload
					mSelectedContainerSession.setUploadConfirmation(true);

					mSelectedContainerSession
							.setCheckOutTime(StringHelper
									.getCurrentTimestamp(CJayConstant.CJAY_SERVER_DATETIME_FORMAT));

					mSelectedContainerSession
							.setUploadState(ContainerSession.STATE_UPLOAD_WAITING);

					if (null == containerSessionDaoImpl) {
						containerSessionDaoImpl = CJayClient.getInstance()
								.getDatabaseManager().getHelper(getActivity())
								.getContainerSessionDaoImpl();
					}

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
		Cursor cursor = (Cursor) cursorAdapter.getItem(position);
		String uuidString = cursor.getString(cursor
				.getColumnIndexOrThrow(ContainerSession.FIELD_UUID));
		ContainerSession containerSession;
		try {

			containerSession = containerSessionDaoImpl.findByUuid(uuidString);
			CJayApplication.gotoCamera(getActivity(), containerSession,
					CJayImage.TYPE_EXPORT, LOG_TAG);

		} catch (SQLException e) {
			e.printStackTrace();
		}
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

			Activity activity = getActivity();
			String currentTimeStamp = StringHelper
					.getCurrentTimestamp(CJayConstant.CJAY_SERVER_DATETIME_FORMAT);

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
					CJayImage.TYPE_EXPORT, LOG_TAG);

			break;
		}
	}

	public void onEvent(PreLoadDataEvent event) {
		Logger.Log("PreLoadDataEvent");
		mLoadMoreDataLayout.setVisibility(View.VISIBLE);
	}

	public void onEvent(PostLoadDataEvent event) {
		Logger.Log("PostLoadDataEvent");
		mLoadMoreDataLayout.setVisibility(View.GONE);
	}

	public void onEvent(ContainerSessionEnqueueEvent event) {
		Logger.Log("ContainerSessionEnqueueEvent");
		refresh();
	}

	public void onEventMainThread(ContainerSessionChangedEvent event) {
		Logger.Log("ContainerSessionChangedEvent");
		refresh();
	}

	public void refresh() {
		getLoaderManager().restartLoader(LOADER_ID, null, this);
	}

	@Override
	public void onResume() {

		if (cursorAdapter != null) {
			refresh();
		}

		if (!DataCenter.getInstance().isUpdating(getActivity())) {
			mLoadMoreDataLayout.setVisibility(View.GONE);
		}

		super.onResume();
	}

	@Override
	public void onRefreshStarted(View view) {
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
}
