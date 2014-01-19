package com.cloudjay.cjay;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.NoTitle;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.ViewById;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.aerilys.helpers.android.UIHelper;
import com.cloudjay.cjay.dao.CJayImageDaoImpl;
import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.events.CJayImageAddedEvent;
import com.cloudjay.cjay.model.AuditReportItem;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.GateReportImage;
import com.cloudjay.cjay.network.CJayClient;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.StringHelper;

import de.greenrobot.event.EventBus;

/**
 * Input:
 * 
 * - type: chỉ định đang mở Camera ở in | out | audit | repair
 * 
 * - uuid: container session uuid --> use to load object from db
 * 
 * Output:
 * 
 * - Mỗi lần chụp hình thực hiện tạo Object `GateReportImage` |
 * `AuditReportItem` phụ thuộc vào type ban đầu truyền vào và add vào
 * tmpContainerSession. Tạo con CJayImage để upload lên server.
 * 
 * 
 * - Sau khi click Done >> Convert tmpContainerSession > ContainerSession và
 * thực hiện enqueue (save to db)
 * 
 */
@SuppressWarnings("deprecation")
@EActivity(R.layout.activity_camera)
@NoTitle
public class CameraActivity extends Activity implements AutoFocusCallback {

	private static final String TAG = "CameraActivity";
	public static final String CJAY_CONTAINER_SESSION_EXTRA = "cjay_container_session";

	Camera camera = null;
	MediaPlayer shootMediaPlayer = null;
	private SurfaceHolder previewHolder = null;
	private boolean inPreview = false;
	private boolean cameraConfigured = false;

	String flashMode;
	int cameraMode;

	private final String LOG_TAG = "CameraActivity";

	List<GateReportImage> gateReportImages = new ArrayList<GateReportImage>();
	List<AuditReportItem> auditReportItems = new ArrayList<AuditReportItem>();
	List<CJayImage> cJayImages = new ArrayList<CJayImage>();

	private static final int PICTURE_SIZE_MAX_WIDTH = 640;
	private static final int PREVIEW_SIZE_MAX_WIDTH = 1280;

	@ViewById(R.id.camera_preview)
	SurfaceView preview;

	@ViewById(R.id.btn_back)
	ImageButton backButton;

	@ViewById(R.id.btn_switch_camera)
	ImageButton switchCameraButton;

	@ViewById(R.id.btn_toggle_flash)
	ImageButton toggleFlashButton;

	@ViewById(R.id.btn_capture)
	ImageButton captureButton;

	@ViewById(R.id.btn_camera_done)
	Button doneButton;

	@ViewById(R.id.rl_camera_done)
	RelativeLayout cameraDoneLayout;

	@SystemService
	AudioManager audioManager;

	ContainerSession containerSession = null;

	@Extra(CJAY_CONTAINER_SESSION_EXTRA)
	String containerSessionUUID = "";

	@Extra("type")
	int type = 0;

	@Extra("tag")
	String tag = "";

	// endregion

	SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {

		public void surfaceCreated(SurfaceHolder holder) {
			// no-op -- wait until surfaceChanged()
			Logger.Log(LOG_TAG, "onSurfaceCreated");
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {

			Logger.Log(LOG_TAG, "onSurfaceChanged");

			initPreview(width, height);
			startPreview();

			Logger.Log(LOG_TAG, "endSurfaceChanged");
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			// no-op
			Logger.Log(LOG_TAG, "onSurfaceDestroyed");

		}
	};

	Camera.PictureCallback photoCallback = new Camera.PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			Logger.Log(LOG_TAG, "onPictureTaken");

			savePhoto(data);
			camera.startPreview();
			inPreview = true;
		}
	};

	ShutterCallback shutterCallback = new ShutterCallback() {

		@Override
		public void onShutter() {
			int volume = audioManager
					.getStreamVolume(AudioManager.STREAM_NOTIFICATION);

			if (volume != 0) {
				if (shootMediaPlayer == null)
					shootMediaPlayer = MediaPlayer
							.create(getApplicationContext(),
									Uri.parse("file:///system/media/audio/ui/camera_click.ogg"));

				if (shootMediaPlayer != null)
					shootMediaPlayer.start();
			}

			Logger.Log(LOG_TAG, "onShutter");
		}
	};

	// @Override
	// public void onCreate(android.os.Bundle savedInstanceState) {
	// onConfigurationChanged(getResources().getConfiguration());
	// };

	@SuppressWarnings({})
	@AfterViews
	void initCamera() {
		Logger.Log(LOG_TAG, "initCamera(), addSurfaceCallback");

		// WARNING: this block should be run before onResume()
		// Setup Surface Holder
		previewHolder = preview.getHolder();
		previewHolder.addCallback(surfaceCallback);
		previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		// Restore camera state from database or somewhere else
		flashMode = Camera.Parameters.FLASH_MODE_AUTO;
		cameraMode = Camera.CameraInfo.CAMERA_FACING_BACK;

		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
	}

	@AfterViews
	void initContainerSession() {
		Logger.Log(LOG_TAG, "initContainerSession()");

		try {
			ContainerSessionDaoImpl containerSessionDaoImpl = CJayClient
					.getInstance().getDatabaseManager().getHelper(this)
					.getContainerSessionDaoImpl();

			containerSession = containerSessionDaoImpl
					.queryForId(containerSessionUUID);

			// TODO: xử lý container session
			if (null != containerSession) {

			} else {

			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private void initPreview(int width, int height) {
		Logger.Log(LOG_TAG, "initPreview()");

		if (camera != null && previewHolder.getSurface() != null) {
			try {
				Logger.Log(LOG_TAG, "setPreviewDisplay");
				camera.setPreviewDisplay(previewHolder);
				// camera.setPreviewCallback(null);
				// camera.setOneShotPreviewCallback(null);

			} catch (Throwable t) {

				Log.e("PreviewDemo-surfaceCallback",
						"Exception in setPreviewDisplay()", t);
				Toast.makeText(this, t.getMessage(), Toast.LENGTH_LONG).show();
			}

			if (!cameraConfigured) {

				Logger.Log(LOG_TAG, "config Camera");

				Camera.Parameters parameters = camera.getParameters();
				Camera.Size size = determineBestPreviewSize(parameters);
				Camera.Size pictureSize = determineBestPictureSize(parameters);

				Logger.Log(LOG_TAG,
						"PreviewSize: " + Integer.toString(size.width) + "/"
								+ Integer.toString(size.height));

				Logger.Log(LOG_TAG,
						"PictureSize: " + Integer.toString(pictureSize.width)
								+ "/" + Integer.toString(pictureSize.height));

				try {
					if (size != null && pictureSize != null) {
						parameters.setPreviewSize(size.width, size.height);

						parameters.setPictureSize(pictureSize.width,
								pictureSize.height);

						parameters.setPictureFormat(ImageFormat.JPEG);
						parameters.setFlashMode(flashMode);
						parameters
								.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

						List<String> modes = parameters
								.getSupportedFocusModes();

						for (String string : modes) {
							Logger.Log(LOG_TAG, string);
						}

						// parameters
						// setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

						camera.setParameters(parameters);
						cameraConfigured = true;
					}
				} catch (NotFoundException e) {

				}
			}
		}
	}

	private void startPreview() {
		Logger.Log(LOG_TAG, "startPreview");

		if (cameraConfigured && camera != null) {
			Logger.Log(LOG_TAG, "cameraConfigured and camera != null");

			camera.startPreview();
			inPreview = true;
		}
	}

	Bitmap createScaledBitmap(byte[] data) {
		if (data != null) {

			int screenWidth = getResources().getDisplayMetrics().widthPixels;
			int screenHeight = getResources().getDisplayMetrics().heightPixels;

			Bitmap bm = BitmapFactory.decodeByteArray(data, 0,
					(data != null) ? data.length : 0);

			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

				// Notice that width and height are reversed
				Bitmap scaled = Bitmap.createScaledBitmap(bm, screenHeight,
						screenWidth, true);
				int w = scaled.getWidth();
				int h = scaled.getHeight();

				// Setting post rotate to 90
				Matrix mtx = new Matrix();
				mtx.postRotate(90);

				// Rotating Bitmap
				bm = Bitmap.createBitmap(scaled, 0, 0, w, h, mtx, true);

			} else {

				// LANDSCAPE MODE
				// No need to reverse width and height
				Bitmap scaled = Bitmap.createScaledBitmap(bm, screenWidth,
						screenHeight, true);
				bm = scaled;
			}

			return bm;
		}
		return null;
	}

	/**
	 * save byte array to Bitmap
	 * 
	 * @param data
	 * @return
	 */
	Bitmap saveToBitmap(byte[] data) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;

		if (data != null) {
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

				Bitmap bm = BitmapFactory.decodeByteArray(data, 0,
						(data != null) ? data.length : 0);

				android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
				android.hardware.Camera.getCameraInfo(cameraMode, info);

				int rotation = getWindowManager().getDefaultDisplay()
						.getRotation();

				Matrix mtx = new Matrix();

				if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT
						&& rotation == Surface.ROTATION_0) {
					mtx.postRotate(270);
				} else {
					mtx.postRotate(90);
				}

				Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0,
						bm.getWidth(), bm.getHeight(), mtx, true);

				if (bm != null) {
					bm.recycle();
					bm = null;
					System.gc();
				}

				return rotatedBitmap;

			} else {

				int rotation = getWindowManager().getDefaultDisplay()
						.getRotation();

				if (rotation == Surface.ROTATION_270) {

					Matrix mtx = new Matrix();
					mtx.postRotate(180);

					// Flip Bitmap
					Logger.Log(TAG, "Flip image");
					Bitmap bm = BitmapFactory.decodeByteArray(data, 0,
							(data != null) ? data.length : 0);
					bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
							bm.getHeight(), mtx, true);
					return bm;

				} else {
					// LANDSCAPE MODE
					// No need to reverse width and height
					Bitmap bm = BitmapFactory.decodeByteArray(data, 0,
							(data != null) ? data.length : 0);
					return bm;
				}
			}
		}
		return null;
	}

	void saveBitmapToFile(Bitmap bitmap, File filename) {

		Logger.Log(LOG_TAG, "===== On SaveBitmap =====");
		Logger.Log(LOG_TAG,
				"Width/Height: " + Integer.toString(bitmap.getWidth()) + "/"
						+ Integer.toString(bitmap.getHeight()));

		try {
			FileOutputStream out = new FileOutputStream(filename);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
			out.flush();
			out.close();

			Logger.Log(LOG_TAG, "Path: " + filename.getAbsolutePath());

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Background
	void savePhoto(byte[] data) {
		// Convert rotated byte[] to Bitmap
		Bitmap capturedBitmap = saveToBitmap(data);

		// Save Bitmap to Files
		String uuid = UUID.randomUUID().toString();

		String imageType;
		switch (type) {
		case CJayImage.TYPE_IMPORT:
			imageType = "gate-in";
			break;

		case CJayImage.TYPE_EXPORT:
			imageType = "gate-out";
			break;

		case CJayImage.TYPE_REPORT:
			imageType = "report";
			break;

		case CJayImage.TYPE_REPAIRED:
		default:
			imageType = "repair";
			break;
		}

		// TODO: BUG HERE
		String depotCode = containerSession.getContainer().getDepot()
				.getDepotCode();

		// filename sample:
		// [depot-code]-2013-12-19-[gate-in|gate-out|report]-[UUID].jpg
		String fileName = depotCode + "-"
				+ StringHelper.getCurrentTimestamp("yyyy-mm-dd") + "-"
				+ imageType + "-" + uuid + ".jpg";

		File photo = new File(CJayConstant.APP_DIRECTORY_FILE, fileName);

		// Save Bitmap to JPEG
		saveBitmapToFile(capturedBitmap, photo);

		// Upload image
		uploadImage(uuid, "file://" + photo.getAbsolutePath(), fileName);

		if (capturedBitmap != null) {
			capturedBitmap.recycle();
			capturedBitmap = null;
			System.gc();
		}
	}

	private synchronized void uploadImage(String uuid, String uri,
			String image_name) {

		// Create Database Entity Object
		CJayImage uploadItem = new CJayImage();

		// Set Uploading Status
		uploadItem.setType(type);
		uploadItem.setTimePosted(StringHelper
				.getCurrentTimestamp(CJayConstant.CJAY_SERVER_DATETIME_FORMAT));
		uploadItem.setUploadState(CJayImage.STATE_UPLOAD_WAITING);
		uploadItem.setUuid(uuid);
		uploadItem.setUri(uri);
		uploadItem.setImageName(image_name);
		uploadItem.setContainerSession(containerSession);

		if (TextUtils.isEmpty(containerSession.getImageIdPath())) {
			Logger.Log(LOG_TAG, "image_id_path: " + uri);
			containerSession.setImageIdPath(uri);
		}

		try {
			CJayImageDaoImpl uploadList = CJayClient.getInstance()
					.getDatabaseManager().getHelper(getApplicationContext())
					.getCJayImageDaoImpl();

			cJayImages.add(uploadItem);
			uploadList.addCJayImage(uploadItem);

		} catch (SQLException e) {
			e.printStackTrace();
		}

		// tell people that an image has been created
		if (!TextUtils.isEmpty(tag)) {
			EventBus.getDefault()
					.post(new CJayImageAddedEvent(uploadItem, tag));
		}
	}

	@Click({ R.id.btn_camera_done, R.id.btn_back, R.id.rl_camera_done })
	void doneButtonClicked() {
		Logger.Log(LOG_TAG,
				"doneButtonClicked(). Ready to update Container Session.");

		try {

			ContainerSessionDaoImpl containerSessionDaoImpl = CJayClient
					.getInstance().getDatabaseManager().getHelper(this)
					.getContainerSessionDaoImpl();

			containerSessionDaoImpl.addContainerSession(containerSession);

		} catch (SQLException e) {
			e.printStackTrace();
		}

		this.onBackPressed();
	}

	// region Override Activity
	@Override
	protected void onResume() {
		Logger.Log(LOG_TAG, "onResume()");
		super.onResume();
		openCamera();
		setContentView(R.layout.activity_camera);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		setCameraDisplayOrientation(this, cameraMode, camera);

		setContentView(R.layout.activity_camera);
	}

	void openCamera() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			Camera.CameraInfo info = new Camera.CameraInfo();

			for (int i = 0; i < Camera.getNumberOfCameras(); i++) {

				Camera.getCameraInfo(i, info);
				if (info.facing == cameraMode) {
					Logger.Log(LOG_TAG, "inside onResume: camera != null");

					camera = Camera.open(i);
					setCameraDisplayOrientation(this, cameraMode, camera);

				}
			}
		}

		if (camera == null) {
			Logger.Log(LOG_TAG, "inside onResume: camera == null");
			camera = Camera.open(cameraMode);
		}

		startPreview();
	}

	void releaseCamera() {
		if (camera != null) {

			Logger.Log(LOG_TAG, "Release camera ... ");

			if (inPreview) {
				Logger.Log(LOG_TAG, "Stop Preview ... ");
				camera.stopPreview();
				// preview.getHolder().removeCallback(null);
			}

			// camera.setPreviewCallback(null);
			camera.release();
			camera = null;

			// WARNING: lots of bugs may appear
			inPreview = false;
			cameraConfigured = false;

			Logger.Log(LOG_TAG, "Release camera complete");
		}
	}

	@Override
	protected void onPause() {
		releaseCamera();
		super.onPause();
	}

	public String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = (Cursor) managedQuery(uri, projection, null, null, null);
		if (cursor != null) {

			// HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
			// THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} else
			return null;
	}

	@Click(R.id.btn_switch_camera)
	void switchCameraButtonClicked() {

		Logger.Log(LOG_TAG, "switchCameraButtonClicked()");
		if (inPreview && camera != null) {
			if (Camera.getNumberOfCameras() > 1) {

				if (inPreview) {
					camera.stopPreview();
				}
				camera.release();

				if (cameraMode == CameraInfo.CAMERA_FACING_BACK) {
					cameraMode = CameraInfo.CAMERA_FACING_FRONT;
					Logger.Log(LOG_TAG, "CameraInfo.CAMERA_FACING_FRONT");
				} else {
					cameraMode = CameraInfo.CAMERA_FACING_BACK;
					Logger.Log(LOG_TAG, "CameraInfo.CAMERA_FACING_BACK");
				}

				camera = Camera.open(cameraMode);
				setCameraDisplayOrientation(this, cameraMode, camera);

				try {
					camera.setPreviewDisplay(previewHolder);
				} catch (IOException e) {
					e.printStackTrace();
				}
				camera.startPreview();

			} else {
				UIHelper.toast(this, "Device has only one camera");
			}
		}

	}

	/**
	 * Default:
	 * 
	 * OFF --> AUTO --> ON --> OFF
	 */
	@Click(R.id.btn_toggle_flash)
	void toggleFlashButtonClicked() {

		Logger.Log(LOG_TAG, "toggleFlashButtonClicked()");

		if (inPreview && camera != null) {
			Parameters params = camera.getParameters();
			flashMode = params.getFlashMode();

			if (flashMode.equalsIgnoreCase(Parameters.FLASH_MODE_OFF)) {

				toggleFlashButton.setImageResource(R.drawable.ic_flash_auto);
				params.setFlashMode(Parameters.FLASH_MODE_AUTO);

			} else if (flashMode.equalsIgnoreCase(Parameters.FLASH_MODE_AUTO)) {

				toggleFlashButton.setImageResource(R.drawable.ic_flash_on);
				params.setFlashMode(Parameters.FLASH_MODE_ON);

			} else if (flashMode.equalsIgnoreCase(Parameters.FLASH_MODE_ON)) {

				toggleFlashButton.setImageResource(R.drawable.ic_flash_off);
				params.setFlashMode(Parameters.FLASH_MODE_OFF);

			} else {

			}

			// Update camera and camera setting
			camera.setParameters(params);
			flashMode = params.getFlashMode();

		} else {
			Logger.Log(LOG_TAG, "Camera does not open");
		}
	}

	@Click(R.id.btn_capture)
	void captureButtonClicked() {
		Logger.Log(LOG_TAG, "captureButtonClicked()");
		takePicture();
	}

	@Background
	void takePicture() {
		// if (inPreview) {

		Logger.Log(LOG_TAG, "Prepare to take picture");

		Camera.Parameters cameraParameters = camera.getParameters();

		List<String> supportedFocusMode = cameraParameters
				.getSupportedFocusModes();
		for (String mode : supportedFocusMode) {
			Logger.Log(TAG, "Camera supports Focus Mode: " + mode);
		}

		// Submit focus area to camera
		if (supportedFocusMode.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
			cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
			camera.setParameters(cameraParameters);
			camera.autoFocus(this);
		} else {
			Logger.Log(TAG,
					"No auto focus mode supported, now just take picture");
			camera.takePicture(shutterCallback, null, photoCallback);
		}

		inPreview = false;

		// }
	}

	PictureCallback rawCallback = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			Logger.Log(LOG_TAG, "rawCallback");
		}
	};

	public static void setCameraDisplayOrientation(Activity activity,
			int cameraId, android.hardware.Camera camera) {

		android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
		android.hardware.Camera.getCameraInfo(cameraId, info);

		int rotation = activity.getWindowManager().getDefaultDisplay()
				.getRotation();

		int degrees = 0;
		switch (rotation) {
		case Surface.ROTATION_0:
			degrees = 0;
			break;
		case Surface.ROTATION_90:
			degrees = 90;
			break;
		case Surface.ROTATION_180:
			degrees = 180;
			break;
		case Surface.ROTATION_270:
			degrees = 270;
			break;
		}

		Logger.Log(TAG, "Rotate degree: " + degrees);

		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360; // compensate the mirror
		} else { // back-facing
			result = (info.orientation - degrees + 360) % 360;
		}
		camera.setDisplayOrientation(result);
	}

	private Size determineBestPreviewSize(Camera.Parameters parameters) {
		List<Size> sizes = parameters.getSupportedPreviewSizes();

		return determineBestSize(sizes, PREVIEW_SIZE_MAX_WIDTH);
	}

	private Size determineBestPictureSize(Camera.Parameters parameters) {
		List<Size> sizes = parameters.getSupportedPictureSizes();

		return determineBestSize(sizes, PICTURE_SIZE_MAX_WIDTH);
	}

	protected Size determineBestSize(List<Size> sizes, int widthThreshold) {
		Size bestSize = null;

		for (Size currentSize : sizes) {
			boolean isDesiredRatio = (currentSize.width / 4) == (currentSize.height / 3);
			boolean isBetterSize = (bestSize == null || currentSize.width > bestSize.width);
			boolean isInBounds = currentSize.width <= PICTURE_SIZE_MAX_WIDTH;

			if (isDesiredRatio && isInBounds && isBetterSize) {
				bestSize = currentSize;
			}
		}

		return bestSize;
	}

	@Override
	public void onAutoFocus(boolean arg0, Camera arg1) {
		Logger.Log(TAG, "Auto focused, now take picture");
		camera.takePicture(shutterCallback, null, photoCallback);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
				|| keyCode == KeyEvent.KEYCODE_VOLUME_UP) {

			takePicture();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}
}
