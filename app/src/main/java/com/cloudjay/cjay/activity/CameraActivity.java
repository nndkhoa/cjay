package com.cloudjay.cjay.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.fragment.DemoCameraFragment;
import com.cloudjay.cjay.util.Logger;

import java.io.File;

public class CameraActivity extends Activity implements ActionBar.OnNavigationListener, DemoCameraFragment.Contract {

	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
	private static final String STATE_SINGLE_SHOT = "single_shot";
	private static final String STATE_LOCK_TO_LANDSCAPE = "lock_to_landscape";
	private static final int CONTENT_REQUEST = 1337;

	private DemoCameraFragment std = null;
	private DemoCameraFragment ffc = null;
	private DemoCameraFragment current = null;

	private boolean hasTwoCameras = (Camera.getNumberOfCameras() > 1);
	private boolean singleShot = false;
	private boolean isLockedToLandscape = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);

        Logger.Log("isLockedToLandscape = " + isLockedToLandscape);

        /*final ActionBar actionBar = getActionBar();
        actionBar.hide();

        Logger.Log("New DemoCameraFragment");
        current = DemoCameraFragment.newInstance(false);
        getFragmentManager().beginTransaction()
                .replace(R.id.container, current).commit();*/

		if (hasTwoCameras) {

			Logger.Log("Has 2 camera");
			final ActionBar actionBar = getActionBar();
			actionBar.setDisplayShowTitleEnabled(false);
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
			ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(actionBar.getThemedContext(),
					R.array.nav,
					android.R.layout.simple_list_item_1);
			actionBar.setListNavigationCallbacks(adapter, this);
		} else {
			Logger.Log("New DemoCameraFragment");
			current = DemoCameraFragment.newInstance(false);
			getFragmentManager().beginTransaction()
					.replace(R.id.container, current).commit();
		}
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
        Logger.Log("isLockedToLandscape = " + isLockedToLandscape);

		if (hasTwoCameras) {
			if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
				Logger.Log("setSelectedNavigationItem");
				getActionBar().setSelectedNavigationItem(savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
			}
		}

		setSingleShotMode(savedInstanceState.getBoolean(STATE_SINGLE_SHOT));
		isLockedToLandscape = savedInstanceState.getBoolean(STATE_LOCK_TO_LANDSCAPE);
		if (current != null) {
			current.lockToLandscape(isLockedToLandscape);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (hasTwoCameras) {
			Logger.Log("XXX");
			outState.putInt(STATE_SELECTED_NAVIGATION_ITEM,
					getActionBar().getSelectedNavigationIndex());
		}
		outState.putBoolean(STATE_SINGLE_SHOT, isSingleShotMode());
		outState.putBoolean(STATE_LOCK_TO_LANDSCAPE, isLockedToLandscape);
	}

	// Will be call when set adapter
	@Override
	public boolean onNavigationItemSelected(int position, long id) {

		if (position == 0) {
			Logger.Log("Position 0");
			if (std == null) {
				std = DemoCameraFragment.newInstance(false);
			}
			current = std;
		} else {
			Logger.Log("Position 1");
			if (ffc == null) {
				ffc = DemoCameraFragment.newInstance(true);
			}
			current = ffc;
		}

		getFragmentManager().beginTransaction().replace(R.id.container, current).commit();
		findViewById(android.R.id.content).post(new Runnable() {
			@Override
			public void run() {
				current.lockToLandscape(isLockedToLandscape);
			}
		});

		return (true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		/*new MenuInflater(this).inflate(R.menu.main_camera, menu);
		menu.findItem(R.id.landscape).setChecked(isLockedToLandscape);*/
		return (super.onCreateOptionsMenu(menu));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		/*if (item.getItemId() == R.id.content) {
			Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
			File output = new File(dir, "CameraContentDemo.jpeg");
			i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(output));
			startActivityForResult(i, CONTENT_REQUEST);
		}
        else if (item.getItemId() == R.id.landscape) {
			item.setChecked(!item.isChecked());
			current.lockToLandscape(item.isChecked());
			isLockedToLandscape = item.isChecked();
		} else if (item.getItemId() == R.id.fullscreen) {
			startActivity(new Intent(this, FullScreenActivity.class));
		}*/

		return (super.onOptionsItemSelected(item));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CONTENT_REQUEST) {
			if (resultCode == RESULT_OK) {
				// do nothing
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_CAMERA && current != null
				&& !current.isSingleShotProcessing()) {
			current.takePicture();
			return (true);
		}

		return (super.onKeyDown(keyCode, event));
	}

	@Override
	public boolean isSingleShotMode() {
		return (singleShot);
	}

	@Override
	public void setSingleShotMode(boolean mode) {
		singleShot = mode;
	}
}