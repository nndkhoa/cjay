package com.cloudjay.cjay.fragment;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
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
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.activity.DisplayActivity;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.StringHelper;
import com.commonsware.cwac.camera.CameraFragment;
import com.commonsware.cwac.camera.CameraHost;
import com.commonsware.cwac.camera.CameraUtils;
import com.commonsware.cwac.camera.PictureTransaction;
import com.commonsware.cwac.camera.SimpleCameraHost;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.UUID;

public class DemoCameraFragment extends CameraFragment implements
		SeekBar.OnSeekBarChangeListener {

    private static final int PICTURE_SIZE_MAX_WIDTH = 640;
    private static final int PREVIEW_SIZE_MAX_WIDTH = 1280;

	private static final String KEY_USE_FFC = "com.commonsware.cwac.camera.demo.USE_FFC";
	//private MenuItem autoFocusItem = null;

	private boolean singleShotProcessing = false;
	//private SeekBar zoom = null;
    private ImageButton btnTakePicture;
    private ImageButton btnFlashMode;
    private ToggleButton btnCameraMode;
    private Button btnDone;
	private long lastFaceToast = 0L;
	String flashMode = null; //flash mode parameter when take camera

    int mType = 0;

    String containerId;
    String depotCode;
    String operatorCode;

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
        //Set default flash mode parameter when take camera is OFF
        flashMode = "off";
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View cameraView =
				super.onCreateView(inflater, container, savedInstanceState);
		View results = inflater.inflate(R.layout.fragment_demo_camera, container, false);

		((ViewGroup) results.findViewById(R.id.camera)).addView(cameraView);
		/*zoom = (SeekBar) results.findViewById(R.id.zoom);
		zoom.setKeepScreenOn(true);*/
        btnTakePicture = (ImageButton) results.findViewById(R.id.btn_capture);
        btnTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnTakePicture.setEnabled(false);
                autoFocus();
            }
        });

        btnFlashMode = (ImageButton) results.findViewById(R.id.btn_toggle_flash);
        btnFlashMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flashMode.equals("off")) {
                    Logger.Log("Set auto");
                    flashMode = "auto";
                    btnFlashMode.setImageResource(R.drawable.ic_flash_auto);
                } else if (flashMode.equals("auto")) {
                    Logger.Log("Set on");
                    flashMode = "on";
                    btnFlashMode.setImageResource(R.drawable.ic_flash_on);
                } else if (flashMode.equals("on")) {
                    Logger.Log("Set off");
                    flashMode = "off";
                    btnFlashMode.setImageResource(R.drawable.ic_flash_off);
                }
            }
        });

        btnCameraMode = (ToggleButton) results.findViewById(R.id.btn_capture_mode);
        btnCameraMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btnCameraMode.isChecked() == true) {
                    getContract().setSingleShotMode(false);
                    Logger.Log("Single shot mode: " + getContract().isSingleShotMode());
                    Toast.makeText(getActivity(), "Kích hoạt chế độ chụp liên tục", Toast.LENGTH_SHORT).show();
                } else {
                    getContract().setSingleShotMode(true);
                    Logger.Log("Single shot mode: " + getContract().isSingleShotMode());
                    Toast.makeText(getActivity(), "Đã dừng chế độ chụp liên tục", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnDone = (Button) results.findViewById(R.id.btn_camera_done);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        return (results);
	}

	@Override
	public void onPause() {
		super.onPause();

		getActivity().invalidateOptionsMenu();
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

		if (getContract().isSingleShotMode()==true) {
            Logger.Log("Processing Single shot mode");
			singleShotProcessing = true;
			btnTakePicture.setEnabled(false);
		}

		// 2.
		PictureTransaction xact = new PictureTransaction(getHost());
        xact.needBitmap(true);

		// Tag another object along if you need to
		// xact.tag();
        xact.flashMode(flashMode);
		/*if (flashItem != null && flashItem.isChecked()) {
			xact.flashMode(flashMode);
		}*/

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
            return (!btnCameraMode.isChecked());
        }

        /**
         * Process taken picture
         *
         * @param xact
         * @param image
         */
        @Override
        public void saveImage(PictureTransaction xact, Bitmap capturedBitmap) {

            // TODO: Checkout cjay v1 flow

            // Convert rotated byte[] to Bitmap
            //Bitmap capturedBitmap = saveToBitmap(image);

            if (null == capturedBitmap) {
                Logger.Log("capturedBitmap is null");
            }

            // Save Bitmap to Files
            String uuid = UUID.randomUUID().toString();

            String imageType;
            switch (mType) {
                case CJayConstant.TYPE_IMPORT:
                    imageType = "gate-in";
                    break;

                case CJayConstant.TYPE_EXPORT:
                    imageType = "gate-out";
                    break;

                case CJayConstant.TYPE_AUDIT:
                    imageType = "auditor";
                    break;

                case CJayConstant.TYPE_REPAIRED:
                default:
                    imageType = "repair";
                    break;
            }

            // file name example:
            // [depot-code]-2013-12-19-[gate-in|gate-out|report]-[containerId]-[UUID].jpg

            //create today String
            String today = StringHelper.getCurrentTimestamp("yyyy-MM-dd");

            //create image file name
            /*String fileName = depotCode + "-" + today + "-" + imageType + "-" + containerId + "-" + operatorCode + "-"
                    + uuid + ".jpg";*/
            String fileName = "DemoDepotCode" + "-" + today + "-" + "DemoimageType" + "-" + "ContainerId" + "-" + "DemoOperatorCode" + "-"
                    + uuid + ".jpg";

            //create directory to save images
            File newDirectory = new File(CJayConstant.APP_DIRECTORY_FILE, "DemoDepotCode" + "/" + today + "/" + imageType
                    + "/" + "ContainerId");

            if (!newDirectory.exists()) {
                newDirectory.mkdirs();
            }



            if (useSingleShotMode()) {
                singleShotProcessing = false;

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnTakePicture.setEnabled(true);
                    }
                });

                //Todo: Open Dialg and Process here
                /*DisplayActivity.imageToShow = ca //image;
                startActivity(new Intent(getActivity(), DisplayActivity.class));*/
            }

            // Save Bitmap to JPEG
            File photo = new File(newDirectory, fileName);
            saveBitmapToFile(capturedBitmap, photo);
        }

        void saveBitmapToFile(Bitmap bitmap, File filename) {

            Logger.Log("File name: " + filename);

            try {

                FileOutputStream out = new FileOutputStream(filename);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void autoFocusAvailable() {
            if (supportsFaces)
                startFaceDetection();
        }

        @Override
        public void autoFocusUnavailable() {
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

            btnTakePicture.setEnabled(true);
            takeSimplePicture();
        }

        protected Camera.Size determineBestSize(List<Camera.Size> sizes, int widthThreshold) {
            Camera.Size bestSize = null;

            for (Camera.Size currentSize : sizes) {
                boolean isDesiredRatio = currentSize.width / 4 == currentSize.height / 3;
                boolean isBetterSize = bestSize == null || currentSize.width > bestSize.width;
                boolean isInBounds = currentSize.width <= PICTURE_SIZE_MAX_WIDTH;

                if (isDesiredRatio && isInBounds && isBetterSize) {
                    bestSize = currentSize;
                }
            }

            return bestSize;
        }

        @Override
        public Camera.Size getPreviewSize(int displayOrientation, int width, int height, Camera.Parameters parameters) {
            List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
            return determineBestSize(sizes, PREVIEW_SIZE_MAX_WIDTH);
        }

        @Override
        public Camera.Size getPictureSize(PictureTransaction xact, Camera.Parameters parameters) {
            List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
            return determineBestSize(sizes, PICTURE_SIZE_MAX_WIDTH);
        }
    }
}