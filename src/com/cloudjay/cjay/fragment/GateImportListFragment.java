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
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.FilterQueryProvider;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.cloudjay.cjay.CJayActivity;
import com.cloudjay.cjay.CJayApplication;
import com.cloudjay.cjay.*;
import com.cloudjay.cjay.adapter.GateContainerCursorAdapter;
import com.cloudjay.cjay.dao.CJayImageDaoImpl;
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
import com.cloudjay.cjay.util.DatabaseHelper;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.NoConnectionException;
import com.cloudjay.cjay.util.StringHelper;
import com.cloudjay.cjay.view.AddContainerDialog;

import de.greenrobot.event.EventBus;

@EFragment(R.layout.fragment_gate_import)
@OptionsMenu(R.menu.menu_gate_import)
public class GateImportListFragment extends SherlockFragment implements
		OnRefreshListener, LoaderCallbacks<Cursor> {

	private final static String LOG_TAG = "GateImportListFragment";
	private final static int LOADER_ID = CJayConstant.CURSOR_LOADER_ID_GATE_IMPORT;

	@ViewById(R.id.btn_add_new)
	Button mAddNewBtn;

	@ViewById(R.id.feeds)
	ListView mFeedListView;

	private ArrayList<Operator> mOperators;

	private ContainerSession mSelectedContainerSession = null;
	private ContainerSessionDaoImpl containerSessionDaoImpl = null;
	private int mItemLayout = R.layout.list_item_container;

	PullToRefreshLayout mPullToRefreshLayout;
	GateContainerCursorAdapter cursorAdapter;

	public GateImportListFragment() {
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

		mOperators = (ArrayList<Operator>) DataCenter.getInstance()
				.getListOperators(getActivity());

		// initContainerFeedAdapter(null);
		getLoaderManager().initLoader(LOADER_ID, null, this);

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
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}
		});
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		Context context = getActivity();

		return new CJayCursorLoader(context) {
			@Override
			public Cursor loadInBackground() {
				Cursor cursor = DataCenter.getInstance()
						.getLocalContainerSessionCursor(getContext());

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
					return DataCenter.getInstance().filterLocalCursor(
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
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		ViewGroup viewGroup = (ViewGroup) view;
		mPullToRefreshLayout = new PullToRefreshLayout(viewGroup.getContext());
		ActionBarPullToRefresh.from(getActivity()).insertLayoutInto(viewGroup)
				.theseChildrenArePullable(R.id.feeds, android.R.id.empty)
				.listener(this).setup(mPullToRefreshLayout);
	}

	@OptionsItem(R.id.menu_camera)
	void cameraMenuItemSelected() {
		Logger.Log("Menu camera item clicked");
		CJayApplication.gotoCamera(getActivity(), mSelectedContainerSession,
				CJayImage.TYPE_IMPORT, LOG_TAG);
	}

	@OptionsItem(R.id.menu_edit_container)
	void editMenuItemSelected() {
		Logger.Log("Menu edit item clicked");

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

	@OptionsItem(R.id.menu_trash)
	void trashMenuItemSelected() {
		if (mSelectedContainerSession != null
				&& mSelectedContainerSession.isOnLocal()) {
			try {
				// delete selected container session from database
				DatabaseHelper databaseHelper = CJayClient.getInstance()
						.getDatabaseManager().getHelper(getActivity());
				ContainerSessionDaoImpl containerSessionDaoImpl = databaseHelper
						.getContainerSessionDaoImpl();
				CJayImageDaoImpl cJayImageDaoImpl = databaseHelper
						.getCJayImageDaoImpl();

				// delete images from database
				for (CJayImage cJayImage : mSelectedContainerSession
						.getCJayImages()) {
					mSelectedContainerSession.getCJayImages().remove(cJayImage);
					cJayImageDaoImpl.delete(cJayImage);
				}

				// delete container session from database
				containerSessionDaoImpl.delete(mSelectedContainerSession);

				EventBus.getDefault().post(
						new ContainerSessionChangedEvent(
								mSelectedContainerSession));

				hideMenuItems();

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	@OptionsItem(R.id.menu_upload)
	void uploadMenuItemSelected() {
		try {

			Logger.Log("Menu upload item clicked");
			ContainerSessionDaoImpl containerSessionDaoImpl = CJayClient
					.getInstance().getDatabaseManager()
					.getHelper(getActivity()).getContainerSessionDaoImpl();

			// User confirm upload
			mSelectedContainerSession.setUploadConfirmation(true);

			mSelectedContainerSession
					.setUploadState(ContainerSession.STATE_UPLOAD_WAITING);
			mSelectedContainerSession.setOnLocal(false);

			containerSessionDaoImpl.update(mSelectedContainerSession);

			// It will trigger `UploadsFragment` Adapter notifyDataSetChanged
			EventBus.getDefault()
					.post(new ContainerSessionEnqueueEvent(
							mSelectedContainerSession));

			hideMenuItems();

		} catch (SQLException e) {

			mSelectedContainerSession.setUploadConfirmation(false);
			mSelectedContainerSession.setOnLocal(true);

			e.printStackTrace();
		}
	}

	@Click(R.id.btn_add_new)
	void addContainerClicked() {
		// getResources().getString(R.string.default_container_id)
		showContainerDetailDialog("", "",
				AddContainerDialog.CONTAINER_DIALOG_ADD);
	}

	@ItemClick(R.id.feeds)
	void listItemClicked(int position) {
		Logger.Log("Clicked item at position: " + position);
		hideMenuItems();

		Cursor cursor = (Cursor) cursorAdapter.getItem(position);
		String uuidString = cursor.getString(cursor
				.getColumnIndexOrThrow(ContainerSession.FIELD_UUID));

		Intent intent = new Intent(getActivity(), PhotoGridViewActivity_.class);
		intent.putExtra(PhotoGridViewActivity_.CJAY_CONTAINER_SESSION_EXTRA,
				uuidString);
		intent.putExtra(PhotoGridViewActivity_.CJAY_IMAGE_TYPE_EXTRA,
				CJayImage.TYPE_IMPORT);
		startActivity(intent);
	}

	@ItemLongClick(R.id.feeds)
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
		menu.findItem(R.id.menu_camera).setVisible(isDisplayed);
		menu.findItem(R.id.menu_edit_container).setVisible(isDisplayed);
		menu.findItem(R.id.menu_upload).setVisible(isDisplayed);
		menu.findItem(R.id.menu_trash).setVisible(isDisplayed);
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
					CJayImage.TYPE_IMPORT, LOG_TAG);

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

	public void onEventMainThread(ContainerSessionChangedEvent event) {
		Logger.Log("ContainerSessionChangedEvent");
		refresh();
	}

	public void onEventMainThread(ContainerSessionEnqueueEvent event) {
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
		super.onResume();
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
