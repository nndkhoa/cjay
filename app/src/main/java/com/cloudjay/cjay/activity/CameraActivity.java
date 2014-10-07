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

public class CameraActivity extends Activity implements DemoCameraFragment.Contract {

	private static final String STATE_SINGLE_SHOT = "single_shot";
	private static final String STATE_LOCK_TO_LANDSCAPE = "lock_to_landscape";
	private static final int CONTENT_REQUEST = 1337;

	private DemoCameraFragment current = null;
	private boolean singleShot = false;
	private boolean isLockedToLandscape = true;

    // Bundles data
    private int mType;
    private String containerId;
    private String operatorCode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);

        // Get bundles data
        containerId = getIntent().getStringExtra("containerID");
        mType = getIntent().getIntExtra("imageType", 0);
        operatorCode = getIntent().getStringExtra("operatorCode");

        //Add data get from bundle to argument
        Bundle args = new Bundle();
        args.putString("containerId", containerId);
        args.putInt("imageType", mType);
        args.putString("operatorCode", operatorCode);

        current = DemoCameraFragment.newInstance(false);
        current.setArguments(args);
        getFragmentManager().beginTransaction()
                .replace(R.id.container, current).commit();
        findViewById(android.R.id.content).post(new Runnable() {
            @Override
            public void run() {
                current.lockToLandscape(isLockedToLandscape);
            }
        });
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {

		setSingleShotMode(savedInstanceState.getBoolean(STATE_SINGLE_SHOT));
		isLockedToLandscape = savedInstanceState.getBoolean(STATE_LOCK_TO_LANDSCAPE);
		if (current != null) {
			current.lockToLandscape(isLockedToLandscape);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(STATE_SINGLE_SHOT, isSingleShotMode());
		outState.putBoolean(STATE_LOCK_TO_LANDSCAPE, isLockedToLandscape);
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