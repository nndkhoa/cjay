package com.cloudjay.cjay.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.fragment.CameraFragment;
import com.cloudjay.cjay.fragment.CameraFragment_;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.util.Logger;

import org.androidannotations.annotations.AfterExtras;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

@EActivity(R.layout.activity_camera)
public class CameraActivity extends Activity implements CameraFragment.Contract {

	public final static String CONTAINER_ID_EXTRA = "com.cloudjay.wizard.containerID";
	public final static String OPERATOR_CODE_EXTRA = "com.cloudjay.wizard.operatorCode";
	public final static String IMAGE_TYPE_EXTRA = "com.cloudjay.wizard.imageType";
	public final static String CURRENT_STEP_EXTRA = "com.cloudjay.wizard.currentStep";
    // This Extra bundle is use to open Detail Issue Activity only
    public final static String AUDIT_ITEM_UUID_EXTRA = "com.cloudjay.wizard.auditItemUUID";
    public final static String IS_OPENED = "com.cloudjay.wizard.isOpened";

	@Extra(IMAGE_TYPE_EXTRA)
	int mType;

	@Extra(CONTAINER_ID_EXTRA)
	String containerId;

	@Extra(OPERATOR_CODE_EXTRA)
	String operatorCode;

	@Extra(CURRENT_STEP_EXTRA)
	int currentStep;

    @Extra(AUDIT_ITEM_UUID_EXTRA)
    String auditItemUUID;

    @Extra(IS_OPENED)
    boolean isOpened;

	private static final String STATE_SINGLE_SHOT = "single_shot";
	private static final String STATE_LOCK_TO_LANDSCAPE = "lock_to_landscape";

	private boolean singleShot = false;

	private boolean isLockedToLandscape = true;
	private CameraFragment current = null;

	@AfterExtras
	void afterExtra() {

		Logger.Log("mType: " + mType);
        Logger.Log("isOpened: " + isOpened);

		current = CameraFragment_.builder()
				.currentStep(currentStep)
				.containerId(containerId)
				.operatorCode(operatorCode)
				.mType(mType)
                .auditItemUUID(auditItemUUID)
                .isOpened(isOpened)
				.build();

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
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                || keyCode == KeyEvent.KEYCODE_VOLUME_UP
                || keyCode == KeyEvent.KEYCODE_CAMERA
				&& current != null
				&& !current.isSingleShotProcessing()) {

			current.takeSimplePicture();
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