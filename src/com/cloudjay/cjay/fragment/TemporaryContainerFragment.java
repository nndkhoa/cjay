package com.cloudjay.cjay.fragment;

import java.sql.SQLException;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.KeyEvent;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.cloudjay.cjay.CJayApplication;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.adapter.TemporaryContainerCursorAdapter;
import com.cloudjay.cjay.events.ContainerSessionChangedEvent;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.CJayCursorLoader;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.StringHelper;

import de.greenrobot.event.EventBus;

@EFragment(R.layout.fragment_temporary)
public class TemporaryContainerFragment extends SherlockFragment implements LoaderCallbacks<Cursor> {

	@ViewById(R.id.feeds)
	ListView listView;

	TemporaryContainerCursorAdapter cursorAdapter;
	int mItemLayout = R.layout.list_item_container;
	private final static int LOADER_ID = CJayConstant.CURSOR_LOADER_ID_GATE_IMPORT;

	@AfterViews
	void afterView() {
		getLoaderManager().initLoader(LOADER_ID, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		Context context = getActivity();

		return new CJayCursorLoader(context) {
			@Override
			public Cursor loadInBackground() {
				Cursor cursor = DataCenter.getInstance().getTemporaryContainerCursor(getContext());

				if (cursor != null) {
					cursor.getCount();
					cursor.registerContentObserver(mObserver);
				}

				return cursor;
			}
		};
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		if (cursorAdapter == null) {
			cursorAdapter = new TemporaryContainerCursorAdapter(getActivity(), mItemLayout, arg1, 0);
			listView.setAdapter(cursorAdapter);
		} else {
			cursorAdapter.swapCursor(arg1);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		cursorAdapter.swapCursor(null);
	}

	public final static String LOG_TAG = "TemporaryFragment";
	public void onKeyDown(int keycode) {

		switch (keycode) {

			case KeyEvent.KEYCODE_VOLUME_UP:

				// Create temporary Container
				String currentTimeStamp = StringHelper.getCurrentTimestamp(CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE);
				ContainerSession containerSession = new ContainerSession(getActivity(), currentTimeStamp);
				containerSession.setOnLocal(true);
				containerSession.setTemporary(true);

				try {
					DataCenter.getDatabaseHelper(getActivity()).getContainerSessionDaoImpl()
								.addContainerSession(containerSession);
				} catch (SQLException e) {
					e.printStackTrace();
				}

				EventBus.getDefault().post(new ContainerSessionChangedEvent(containerSession));
				CJayApplication.openCamera(getActivity(), containerSession.getUuid(), CJayImage.TYPE_IMPORT, LOG_TAG);

				break;
		}
	}
	
	@Override
	public void onResume() {
		if (cursorAdapter != null) {
			refresh();
		}
		super.onResume();
	}
	
	public void refresh() {
		getLoaderManager().restartLoader(LOADER_ID, null, this);
	}
}
