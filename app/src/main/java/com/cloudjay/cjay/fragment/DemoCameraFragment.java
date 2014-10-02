package com.cloudjay.cjay.fragment;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.activity.DisplayActivity;
import com.cloudjay.cjay.util.Logger;
import com.commonsware.cwac.camera.CameraFragment;
import com.commonsware.cwac.camera.CameraHost;
import com.commonsware.cwac.camera.CameraUtils;
import com.commonsware.cwac.camera.PictureTransaction;
import com.commonsware.cwac.camera.SimpleCameraHost;

public class DemoCameraFragment extends CameraFragment implements
		SeekBar.OnSeekBarChangeListener {

	private static final String KEY_USE_FFC = "com.commonsware.cwac.camera.demo.USE_FFC";
	private MenuItem singleShotItem = null;
	private MenuItem autoFocusItem = null;
	private MenuItem takePictureItem = null;
	private MenuItem flashItem = null;

	private boolean singleShotProcessing = false;
	//private SeekBar zoom = null;
    private Button btnTakePicture;
    private Button btnFlashMode;
	private long lastFaceToast = 0L;
	String flashMode = null;

	public static DemoCameraFragment newInstance(boolean useFFC) {
		Logger.Log("new DemoCameraFragment");
		DemoCameraFragment f = new DemoCameraFragment();
		Bundle args = new Bundle();
		args.putBoolean(KEY_USE_FFC, useFFC);
		f.setArguments(args);
		return (f);
	}

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);

		// 1. CameraHost is the interface use to configure behavior of camera
		// ~ setting
		SimpleCameraHost.Builder builder = new SimpleCameraHost.Builder(new DemoCameraHost(getActivity()));
		setHost(builder.useFullBleedPreview(true).build());

		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View cameraView =
				super.onCreateView(inflater, container, savedInstanceState);
		View results = inflater.inflate(R.layout.fragment_demo_camera, container, false);

		((ViewGroup) results.findViewById(R.id.camera)).addView(cameraView);
		/*zoom = (SeekBar) results.findViewById(R.id.zoom);
		zoom.setKeepScreenOn(true);*/
        btnTakePicture = (Button) results.findViewById(R.id.btn_capture);
        btnTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                autoFocus();
            }
        });

        btnFlashMode = (Button) results.findViewById(R.id.btn_toggle_flash);
        btnFlashMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Logger.i("Flash mode: " + getFlashMode());
                if (getFlashMode().equals("off")) {
                    Logger.Log("Flash on");
                    setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                } else {
                    Logger.Log("Flash off");
                    setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                }
            }
        });

		return (results);
	}

	@Override
	public void onPause() {
		super.onPause();

		getActivity().invalidateOptionsMenu();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.camera, menu);

		takePictureItem = menu.findItem(R.id.camera);
		singleShotItem = menu.findItem(R.id.single_shot);
		singleShotItem.setChecked(getContract().isSingleShotMode());
		autoFocusItem = menu.findItem(R.id.autofocus);
		flashItem = menu.findItem(R.id.flash);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.camera:
				takeSimplePicture();

				return (true);

			case R.id.autofocus:
				takePictureItem.setEnabled(false);
				/*autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean b, Camera camera) {

                    }
                });*/

				return (true);

			case R.id.single_shot:
				item.setChecked(!item.isChecked());
				getContract().setSingleShotMode(item.isChecked());

				return (true);

			case R.id.show_zoom:
				item.setChecked(!item.isChecked());
				//zoom.setVisibility(item.isChecked() ? View.VISIBLE : View.GONE);

				return (true);

			case R.id.flash:
		}

		return (super.onOptionsItemSelected(item));
	}

	public boolean isSingleShotProcessing() {
		return (singleShotProcessing);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
	                              boolean fromUser) {
		/*if (fromUser) {
			zoom.setEnabled(false);
			zoomTo(zoom.getProgress()).onComplete(new Runnable() {
				@Override
				public void run() {
					zoom.setEnabled(true);
				}
			}).go();
		}*/
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// ignore
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// ignore
	}

	Contract getContract() {
		return ((Contract) getActivity());
	}

	/**
	 * Take a picture, need to call auto focus before taking picture
	 */
	public void takeSimplePicture() {

		Logger.Log("Prepare to take picture");

		if (singleShotItem != null && singleShotItem.isChecked()) {
			singleShotProcessing = true;
			takePictureItem.setEnabled(false);
		}

		// 2.
		PictureTransaction xact = new PictureTransaction(getHost());

		// Tag another object along if you need to
		// xact.tag();

		if (flashItem != null && flashItem.isChecked()) {
			xact.flashMode(flashMode);
		}

		// Call it with PictureTransaction to take picture with configuration in CameraHost
		// Process image in Subclass of `CameraHost#saveImage`
		takePicture(xact);
	}

	public interface Contract {
		boolean isSingleShotMode();

		void setSingleShotMode(boolean mode);
	}

	class DemoCameraHost extends SimpleCameraHost implements
			Camera.FaceDetectionListener {
        boolean supportsFaces = false;

        public DemoCameraHost(Context _ctxt) {
            super(_ctxt);
        }

        @Override
        public boolean useFrontFacingCamera() {
            if (getArguments() == null) {
                return (false);
            }

            return (getArguments().getBoolean(KEY_USE_FFC));
        }

        @Override
        public boolean useSingleShotMode() {
            if (singleShotItem == null) {
                return (false);
            }

            return (singleShotItem.isChecked());
        }

        /**
         * Process taken picture
         *
         * @param xact
         * @param image
         */
        @Override
        public void saveImage(PictureTransaction xact, byte[] image) {

            // TODO: Checkout cjay v1 flow
            if (useSingleShotMode()) {
                singleShotProcessing = false;

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        takePictureItem.setEnabled(true);
                    }
                });

                DisplayActivity.imageToShow = image;
                startActivity(new Intent(getActivity(), DisplayActivity.class));
            } else {
                super.saveImage(xact, image);
            }
        }

        @Override
        public void autoFocusAvailable() {
            if (autoFocusItem != null) {
                autoFocusItem.setEnabled(true);

                if (supportsFaces)
                    startFaceDetection();
            }
        }

        @Override
        public void autoFocusUnavailable() {
            if (autoFocusItem != null) {
                stopFaceDetection();

                if (supportsFaces)
                    autoFocusItem.setEnabled(false);
            }
        }

        @Override
        public void onCameraFail(CameraHost.FailureReason reason) {
            super.onCameraFail(reason);

            Toast.makeText(getActivity(),
                    "Sorry, but you cannot use the camera now!",
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public Camera.Parameters adjustPreviewParameters(Camera.Parameters parameters) {
            flashMode =
                    CameraUtils.findBestFlashModeMatch(parameters,
                            Camera.Parameters.FLASH_MODE_RED_EYE,
                            Camera.Parameters.FLASH_MODE_AUTO,
                            Camera.Parameters.FLASH_MODE_ON);

			/*if (doesZoomReallyWork() && parameters.getMaxZoom() > 0) {
				zoom.setMax(parameters.getMaxZoom());
				zoom.setOnSeekBarChangeListener(DemoCameraFragment.this);
			} else {
				zoom.setEnabled(false);
			}*/

            if (parameters.getMaxNumDetectedFaces() > 0) {
                supportsFaces = true;
            } else {
                Toast.makeText(getActivity(),
                        "Face detection not available for this camera",
                        Toast.LENGTH_LONG).show();
            }

            return (super.adjustPreviewParameters(parameters));
        }

        @Override
        public void onFaceDetection(Camera.Face[] faces, Camera camera) {
            if (faces.length > 0) {
                long now = SystemClock.elapsedRealtime();

                if (now > lastFaceToast + 10000) {
                    Toast.makeText(getActivity(), "I see your face!",
                            Toast.LENGTH_LONG).show();
                    lastFaceToast = now;
                }
            }
        }

        @Override
        @TargetApi(16)
        public void onAutoFocus(boolean success, Camera camera) {
            super.onAutoFocus(success, camera);

            takePictureItem.setEnabled(true);
            takeSimplePicture();
        }
    }

}