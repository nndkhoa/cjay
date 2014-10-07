package com.cloudjay.cjay.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.fragment.CameraFragment;

public class FullScreenActivity extends Activity implements CameraFragment.Contract {
	CameraFragment frag = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_full_screen);
		frag = (CameraFragment) getFragmentManager().findFragmentById(R.id.camera_preview);
	}

	@Override
	public boolean isSingleShotMode() {
		return (false);
	}

	@Override
	public void setSingleShotMode(boolean mode) {
		// hardcoded, unused
	}

	public void takePicture(View v) {
		frag.takeSimplePicture();
	}
}