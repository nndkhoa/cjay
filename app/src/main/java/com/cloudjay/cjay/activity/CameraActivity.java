package com.cloudjay.cjay.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.fragment.CameraFragment;

public class CameraActivity extends Activity implements CameraFragment.Contract {

	private static final String STATE_SINGLE_SHOT = "single_shot";
	private static final String STATE_LOCK_TO_LANDSCAPE = "lock_to_landscape";
	private static final int CONTENT_REQUEST = 1337;

	private CameraFragment current = null;
	private boolean singleShot = false;
	private boolean isLockedToLandscape = true;

    // Bundles data
    private int mType;
    private String containerId;
    private String operatorCode;
    private int currentStep;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);

        // Get bundles data
        containerId = getIntent().getStringExtra(CameraFragment.CONTAINER_ID_EXTRA);
        mType = getIntent().getIntExtra(CameraFragment.IMAGE_TYPE_EXTRA, 0);
        operatorCode = getIntent().getStringExtra(CameraFragment.OPERATOR_CODE_EXTRA);
        currentStep = getIntent().getIntExtra(CameraFragment.CURRENT_STEP_EXTRA, 0);

        //Add data get from bundle to argument
        Bundle args = new Bundle();
        args.putString(CameraFragment.CONTAINER_ID_EXTRA, containerId);
        args.putInt(CameraFragment.IMAGE_TYPE_EXTRA, mType);
        args.putString(CameraFragment.OPERATOR_CODE_EXTRA, operatorCode);
        args.putInt(CameraFragment.CURRENT_STEP_EXTRA, currentStep);

        current = CameraFragment.newInstance(false);
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