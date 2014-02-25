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

import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

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
import android.widget.ListView;
import android.widget.TextView;
import com.actionbarsherlock.view.Menu;
import com.cloudjay.cjay.CJayActivity;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.adapter.CheckoutContainerCursorAdapter;
import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.events.ContainerSessionChangedEvent;
import com.cloudjay.cjay.events.ContainerSessionEnqueueEvent;
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
public class GateExportListFragment extends CJaySherlockFragment implements
		OnRefreshListener, LoaderCallbacks<Cursor> {

	private final static String LOG_TAG = "GateExportListFragment";
	private final static int LOADER_ID = 0;
	private ArrayList<Operator> mOperators;
	private ContainerSession mSelectedContainerSession = null;

	@ViewById(R.id.container_list)
	ListView mFeedListView;

	@ViewById(R.id.search_edittext)
	EditText mSearchEditText;

	@ViewById(R.id.add_button)
	Button mAddButton;

	@ViewById(R.id.notfound_textview)
	TextView mNotfoundTextView;

	PullToRefreshLayout mPullToRefreshLayout;
	CheckoutContainerCursorAdapter cursorAdapter;

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

		mSearchEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable arg0) {
				cursorAdapter.getFilter().filter(arg0.toString());
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

		getLoaderManager().initLoader(LOADER_ID, null, this);

		mFeedListView.setTextFilterEnabled(true);
		mFeedListView.setScrollingCacheEnabled(false);
		mFeedListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState != 0) {
					((CheckoutContainerCursorAdapter) mFeedListView
							.getAdapter()).isScrolling = true;
				} else {
					((CheckoutContainerCursorAdapter) mFeedListView
							.getAdapter()).isScrolling = false;
					((CheckoutContainerCursorAdapter) mFeedListView
							.getAdapter()).notifyDataSetChanged();
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}
		});

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

		if (cursor.getCount() != 0) {
			mFeedListView.setVisibility(View.VISIBLE);
			mAddButton.setVisibility(View.INVISIBLE);
			mNotfoundTextView.setVisibility(View.INVISIBLE);
		} else {
			mFeedListView.setVisibility(View.INVISIBLE);
			mAddButton.setVisibility(View.VISIBLE);
			mNotfoundTextView.setVisibility(View.VISIBLE);
		}

		if (cursorAdapter == null) {
			cursorAdapter = new CheckoutContainerCursorAdapter(getActivity(),
					R.layout.list_item_container, cursor, 0);

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
		ContainerSession containerSession = (ContainerSession) cursorAdapter
				.getItem(position);
		ContainerSession.gotoCamera(getActivity(), containerSession,
				CJayImage.TYPE_EXPORT);
	}

	@ItemLongClick(R.id.container_list)
	void listItemLongClicked(int position) {
		// refresh highlighting and menu
		mFeedListView.setItemChecked(position, true);
		mSelectedContainerSession = (ContainerSession) cursorAdapter
				.getItem(position);
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

	public void onEventMainThread(ContainerSessionChangedEvent event) {
		Logger.Log(LOG_TAG, "onEventMainThread ContainerSessionChangedEvent");
		refresh();
	}

	public void refresh() {
		Logger.Log(LOG_TAG, "onRefresh");
		getLoaderManager().restartLoader(LOADER_ID, null, this);
	}

	@Override
	public void onResume() {
		Logger.Log(LOG_TAG, "onResume " + LOG_TAG);
		if (cursorAdapter != null) {
			refresh();
		}
		super.onResume();
	}

	@Override
	public void onRefreshStarted(View view) {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {

				Logger.Log(LOG_TAG, "onRefreshStarted");
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
