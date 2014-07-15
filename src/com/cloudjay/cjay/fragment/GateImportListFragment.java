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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.adapter.GateImportContainerCursorAdapter;
import com.cloudjay.cjay.events.ContainerSessionChangedEvent;
import com.cloudjay.cjay.events.ContainerSessionEnqueueEvent;
import com.cloudjay.cjay.events.ContainerSessionUpdatedEvent;
import com.cloudjay.cjay.events.ListItemChangedEvent;
import com.cloudjay.cjay.events.UploadStateRestoredEvent;
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

@EFragment(R.layout.fragment_gate_import)
@OptionsMenu(R.menu.menu_gate_import)
public class GateImportListFragment extends SherlockFragment implements OnRefreshListener, LoaderCallbacks<Cursor> {

	public final static String LOG_TAG = "GateImportListFragment";
	private final static int LOADER_ID = CJayConstant.CURSOR_LOADER_ID_GATE_IMPORT;

	@ViewById(R.id.btn_add_new)
	Button mAddNewBtn;

	@ViewById(R.id.feeds)
	ListView mFeedListView;

	private ArrayList<Operator> mOperators;

	String mSelectedOperator = "";
	String mSelectedUuid = "";
	String mSelectedContainerId = "";
	Boolean isOnLocal = null;
	int mItemLayout = R.layout.list_item_container;

	PullToRefreshLayout mPullToRefreshLayout;
	GateImportContainerCursorAdapter cursorAdapter;

	int totalItems = 0;

	public GateImportListFragment() {
	}

	@Click(R.id.btn_add_new)
	void addContainerClicked() {
		// getResources().getString(R.string.default_container_id)
		CJayApplication.openContainerDetailDialog(this, "", "", AddContainerDialog.CONTAINER_DIALOG_ADD);
	}

	@AfterViews
	void afterViews() {

		mOperators = (ArrayList<Operator>) DataCenter.getInstance().getListOperators(getActivity());
		getLoaderManager().initLoader(LOADER_ID, null, this);

		mFeedListView.setScrollingCacheEnabled(false);
		mFeedListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState != 0) {
					((GateImportContainerCursorAdapter) mFeedListView.getAdapter()).isScrolling = true;
				} else {
					((GateImportContainerCursorAdapter) mFeedListView.getAdapter()).isScrolling = false;
					((GateImportContainerCursorAdapter) mFeedListView.getAdapter()).notifyDataSetChanged();
				}
			}
		});
	}

	@OptionsItem(R.id.menu_camera)
	void cameraMenuItemSelected() {
		CJayApplication.openCamera(getActivity(), mSelectedUuid, CJayImage.TYPE_IMPORT, LOG_TAG);
	}

	@OptionsItem(R.id.menu_edit_container)
	void editMenuItemSelected() {
		CJayApplication.openContainerDetailDialog(	this, mSelectedContainerId, mSelectedOperator,
													AddContainerDialog.CONTAINER_DIALOG_EDIT);
	}

	void hideMenuItems() {
		isOnLocal = null;
		mSelectedOperator = "";
		mSelectedUuid = "";
		mSelectedContainerId = "";
		mFeedListView.setItemChecked(-1, true);
		getActivity().supportInvalidateOptionsMenu();
	}

	@ItemClick(R.id.feeds)
	void listItemClicked(int position) {
		Logger.Log("Clicked item at position: " + position);
		hideMenuItems();

		Cursor cursor = (Cursor) cursorAdapter.getItem(position);
		String uuidString = cursor.getString(cursor.getColumnIndexOrThrow(ContainerSession.FIELD_UUID));
		String containerId = cursor.getString(cursor.getColumnIndexOrThrow(Container.CONTAINER_ID));
		CJayApplication.openPhotoGridView(	getActivity(), uuidString, containerId, CJayImage.TYPE_IMPORT,
											GateImportListFragment.LOG_TAG);
	}

	// refresh highlighting and menu
	@ItemLongClick(R.id.feeds)
	void listItemLongClicked(int position) {
		mFeedListView.setItemChecked(position, true);

		Cursor cursor = (Cursor) cursorAdapter.getItem(position);
		mSelectedUuid = cursor.getString(cursor.getColumnIndexOrThrow(ContainerSession.FIELD_UUID));
		mSelectedContainerId = cursor.getString(cursor.getColumnIndexOrThrow(Container.CONTAINER_ID));
		mSelectedOperator = cursor.getString(cursor.getColumnIndexOrThrow(Operator.FIELD_NAME));
		isOnLocal = cursor.getInt(cursor.getColumnIndexOrThrow(ContainerSession.FIELD_LOCAL)) != 0;

		getActivity().supportInvalidateOptionsMenu();
	}

	public void OnContainerInputCompleted(String containerId, String operatorName, int mode) {

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

				// Mark it's still temporary
				containerSession.setOnLocal(true);
				// containerSession.setUploadType(UploadType.NONE);

				try {
					DataCenter.getDatabaseHelper(getActivity()).getContainerSessionDaoImpl()
								.addContainerSession(containerSession);
				} catch (SQLException e) {
					e.printStackTrace();
				}

				DataCenter.getDatabaseHelper(getActivity()).addUsageLog(getActivity(),
																		"Create new container: "
																				+ containerSession.getContainerId());
				EventBus.getDefault().post(new ContainerSessionChangedEvent(containerSession));
				CJayApplication.openCamera(activity, containerSession.getUuid(), CJayImage.TYPE_IMPORT, LOG_TAG);

				// temporary upload with upload_type = NONE;
				// CJayApplication.uploadContainerSesison(getActivity(), containerSession);
				break;

			case AddContainerDialog.CONTAINER_DIALOG_EDIT:
				DataCenter.getInstance().editContainer(getActivity(), mSelectedUuid, containerId, operatorCode);
				break;
		}

	}

	public void onEvent(ContainerSessionUpdatedEvent event) {
		refresh();
	}

	public void onEvent(UploadStateRestoredEvent event) {
		refresh();
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

	public void onEventMainThread(ContainerSessionChangedEvent event) {
		refresh();
	}

	public void onEventMainThread(ContainerSessionEnqueueEvent event) {
		refresh();
	}

	// 4.
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		Context context = getActivity();

		return new CJayCursorLoader(context) {

			@Override
			public Cursor loadInBackground() {
				Cursor cursor = DataCenter.getInstance().getLocalContainerSessionCursor(getContext());

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

			cursorAdapter = new GateImportContainerCursorAdapter(getActivity(), mItemLayout, cursor, 0, true);

			cursorAdapter.setFilterQueryProvider(new FilterQueryProvider() {
				@Override
				public Cursor runQuery(CharSequence constraint) {
					return DataCenter.getInstance().filterLocalCursor(getActivity(), constraint);
				}
			});

			mFeedListView.setAdapter(cursorAdapter);

		} else {

			cursorAdapter.swapCursor(cursor);
		}
	}

	public void OnOperatorSelected(String containerId, String operatorName, int mode) {
		CJayApplication.openContainerDetailDialog(this, containerId, operatorName, mode);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		boolean isDisplayed = !(TextUtils.isEmpty(mSelectedUuid));
		menu.findItem(R.id.menu_camera).setVisible(isDisplayed);
		menu.findItem(R.id.menu_edit_container).setVisible(isDisplayed);
		menu.findItem(R.id.menu_trash).setVisible(isDisplayed);
		menu.findItem(R.id.menu_upload).setVisible(isDisplayed);
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
					DataCenter.getDatabaseHelper(getSherlockActivity()).addUsageLog(getActivity(),
																					"#refresh in fragment #GateImport");

				} catch (NoConnectionException e) {

					((CJayActivity) getActivity()).showCrouton(R.string.alert_no_network);
					e.printStackTrace();

				} catch (NullSessionException e) {

					CJayApplication.logOutInstantly(getActivity());
					onDestroy();

				} catch (Exception e) {

					((CJayActivity) getActivity()).showCrouton(R.string.alert_try_again);
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

	@Override
	public void onResume() {
		if (cursorAdapter != null) {
			refresh();
		}
		super.onResume();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		ViewGroup viewGroup = (ViewGroup) view;
		mPullToRefreshLayout = new PullToRefreshLayout(viewGroup.getContext());

		ActionBarPullToRefresh.from(getActivity()).insertLayoutInto(viewGroup)
								.theseChildrenArePullable(R.id.feeds, android.R.id.empty).listener(this)
								.setup(mPullToRefreshLayout);
	}

	public void refresh() {
		getLoaderManager().restartLoader(LOADER_ID, null, this);
	}

	void setTotalItems(int val) {
		totalItems = val;
		EventBus.getDefault().post(new ListItemChangedEvent(0, totalItems));
	}

	@OptionsItem(R.id.menu_trash)
	void trashMenuItemSelected() {
		if (!TextUtils.isEmpty(mSelectedUuid) && isOnLocal == true) {
			SQLiteDatabase db = DataCenter.getDatabaseHelper(getActivity()).getWritableDatabase();
			db.delete("cjay_image", "containerSession_id = ?", new String[] { mSelectedUuid });
			db.delete("container_session", "_id = ?", new String[] { mSelectedUuid });

			EventBus.getDefault().post(new ContainerSessionChangedEvent());
			hideMenuItems();
		}
	}

	@OptionsItem(R.id.menu_upload)
	void uploadMenuItemSelected() {

		if (Utils.isValidForUpload(getActivity(), mSelectedUuid, CJayImage.TYPE_IMPORT)) {

			// Marked it's not temporary anymore
			// mSelectedContainerSession.setUploadType(UploadType.IN);
			QueryHelper.update(	getActivity(), "container_session", ContainerSession.FIELD_UPLOAD_TYPE,
								Integer.toString(UploadType.IN.getValue()),
								ContainerSession.FIELD_UUID + " = " + Utils.sqlString(mSelectedUuid));
			CJayApplication.uploadContainer(getActivity(), mSelectedUuid, mSelectedContainerId);

			DataCenter.getDatabaseHelper(getActivity())
						.addUsageLog(getActivity(),
										mSelectedContainerId + " | Prepare to add #IN container to upload queue");

			hideMenuItems();

		} else {

			Crouton.cancelAllCroutons();
			Crouton.makeText(getActivity(), R.string.alert_no_issue_container, Style.ALERT).show();
		}

	}
}
