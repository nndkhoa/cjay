package com.cloudjay.cjay;

import android.content.Context;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.Session;
import com.cloudjay.cjay.util.Utils;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class CJayActivity extends SherlockFragmentActivity implements
		ICJayActivity {

	private static final String LOG_TAG = "CJayActivity";
	private Session session;
	private DataCenter dataCenter;

	public DataCenter getDataCenter() {
		return dataCenter;
	}

	public void setDataCenter(DataCenter dataCenter) {
		this.dataCenter = dataCenter;
	}

	public Session getSession() {
		return session;
	}

	public User getCurrentUser() {
		if (null == session)
			session = Session.restore(getApplicationContext());

		return session.getCurrentUser();
	}

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		session = Session.restore(getApplicationContext());

		if (Utils.isUploadingPaused(this)) {
			showUploadingDisabledCrouton();
		}
	}

	@Override
	public Context getContext() {
		return this;
	}

	@Override
	protected void onResume() {

		if (null != session)
			DataCenter.reload(getApplicationContext());

		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void setupPauseUploadingMenuItems(Menu menu) {

		MenuItem pauseItem = menu.findItem(R.id.menu_uploading_stop);
		MenuItem startItem = menu.findItem(R.id.menu_uploading_start);

		if (null != pauseItem && null != startItem) {
			startItem.setVisible(Utils.isUploadingPaused(this));
			pauseItem.setVisible(!startItem.isVisible());
		}
	}

	protected final void showUploadingDisabledCrouton() {
		Crouton.cancelAllCroutons();
		Crouton.showText(this, R.string.stopped_uploads, Style.ALERT);
	}

	protected final void showUploadingEnabledCrouton() {
		Crouton.cancelAllCroutons();
		Crouton.showText(this, R.string.started_uploads, Style.CONFIRM);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// TODO: xử lý start/pause upload

		// case R.id.menu_uploading_stop:
		// Utils.setUploadingPaused(this, true);
		// EventBus.getDefault().post(new UploadingPausedStateChangedEvent());
		// return true;
		//
		// case R.id.menu_uploading_start:
		// Utils.setUploadingPaused(this, false);
		// EventBus.getDefault().post(new UploadingPausedStateChangedEvent());
		// startService(Utils.getUploadAllIntent(this));
		// return true;

		case R.id.menu_retry_failed:
			// TODO: menu_retry_failed
			// mPhotoController.moveFailedToSelected();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (menu.size() == 0) {
			getSupportMenuInflater().inflate(R.menu.menu_photo_grid_uploads,
					menu);
			setupPauseUploadingMenuItems(menu);
		}
		return super.onCreateOptionsMenu(menu);
	}
}
