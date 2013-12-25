package com.cloudjay.cjay;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
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
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.aerilys.helpers.android.UIHelper;
import com.cloudjay.cjay.model.GateReportImage;
import com.cloudjay.cjay.model.TmpContainerSession;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.Mapper;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.NoTitle;
import com.googlecode.androidannotations.annotations.SystemService;
import com.googlecode.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_camera)
@NoTitle
public class CameraActivity extends Activity {

	public static CameraActivity instance = null;

	Camera camera = null;
	MediaPlayer shootMediaPlayer = null;
	private SurfaceHolder previewHolder = null;
	private boolean inPreview = false;
	private boolean cameraConfigured = false;

	String itemUri;
	String itemId;
	String flashMode;
	int cameraMode;

	private List<String> photos;

	private static final int PICTURE_SIZE_MAX_WIDTH = 1920;
	private static final int PREVIEW_SIZE_MAX_WIDTH = 1920;

	// region GetViewById
	@ViewById(R.id.camera_preview)
	SurfaceView preview;
	// CameraPreview preview;

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

	@SystemService
	AudioManager audioManager;

	// endregion

	SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {

		public void surfaceCreated(SurfaceHolder holder) {
			// no-op -- wait until surfaceChanged()
			Logger.Log("onSurfaceCreated");
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {

			Logger.Log("onSurfaceChanged");

			initPreview(width, height);
			startPreview();

			Logger.Log("endSurfaceChanged");
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			// no-op
			Logger.Log("onSurfaceDestroyed");

		}
	};

	Camera.PictureCallback photoCallback = new Camera.PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			Logger.Log("onPictureTaken");
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

			Logger.Log("onShutter");
		}
	};

	@SuppressWarnings("deprecation")
	@AfterViews
	void initCamera() {

		instance = this;

		Logger.Log("initCamera(), addSurfaceCallback");

		// WARNING: this block should be run before onResume()
		// Setup Surface Holder
		previewHolder = preview.getHolder();
		previewHolder.addCallback(surfaceCallback);
		previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		// Restore camera state from database or somewhere else
		flashMode = Camera.Parameters.FLASH_MODE_OFF;
		cameraMode = Camera.CameraInfo.CAMERA_FACING_BACK;

		photos = new ArrayList<String>();
	}

	private void initPreview(int width, int height) {
		Logger.Log("initPreview()");

		if (camera != null && previewHolder.getSurface() != null) {
			try {
				Logger.Log("setPreviewDisplay");
				camera.setPreviewDisplay(previewHolder);
				// camera.setPreviewCallback(null);
				// camera.setOneShotPreviewCallback(null);

			} catch (Throwable t) {

				Log.e("PreviewDemo-surfaceCallback",
						"Exception in setPreviewDisplay()", t);
				Toast.makeText(this, t.getMessage(), Toast.LENGTH_LONG).show();
			}

			if (!cameraConfigured) {

				Logger.Log("config Camera");

				Camera.Parameters parameters = camera.getParameters();

				// Camera.Size size = getBestPreviewSize(width, height,
				// parameters);
				// Camera.Size pictureSize = getBestPictureSize(parameters);

				Camera.Size size = determineBestPreviewSize(parameters);
				Camera.Size pictureSize = determineBestPictureSize(parameters);

				Logger.Log("PreviewSize: " + Integer.toString(size.width) + "/"
						+ Integer.toString(size.height));

				Logger.Log("PictureSize: "
						+ Integer.toString(pictureSize.width) + "/"
						+ Integer.toString(pictureSize.height));

				try {
					if (size != null && pictureSize != null) {
						parameters.setPreviewSize(size.width, size.height);

						parameters.setPictureSize(pictureSize.width,
								pictureSize.height);

						parameters.setPictureFormat(ImageFormat.JPEG);
						parameters.setFlashMode(flashMode);

						List<String> modes = parameters
								.getSupportedFocusModes();

						for (String string : modes) {
							Logger.Log(string);
						}

						// parameters
						// .setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

						camera.setParameters(parameters);
						cameraConfigured = true;
					}
				} catch (NotFoundException e) {

				}
			}
		}
	}

	private void startPreview() {
		Logger.Log("startPreview");

		if (cameraConfigured && camera != null) {
			Logger.Log("cameraConfigured and camera != null");

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

	Bitmap saveToBitmap(byte[] data) {

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;

		if (data != null) {
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

				Bitmap bm = BitmapFactory.decodeByteArray(data, 0,
						(data != null) ? data.length : 0);

				Logger.Log("Captured bitmap size: "
						+ Integer.toString(bm.getWidth()) + "/"
						+ Integer.toString(bm.getHeight()));

				Matrix mtx = new Matrix();
				mtx.postRotate(90);

				Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0,
						bm.getWidth(), bm.getHeight(), mtx, true);

				if (bm != null) {
					bm.recycle();
					bm = null;
					System.gc();
				}

				return rotatedBitmap;

			} else {

				// LANDSCAPE MODE
				// No need to reverse width and height
				Bitmap bm = BitmapFactory.decodeByteArray(data, 0,
						(data != null) ? data.length : 0);
				return bm;
			}
		}
		return null;
	}

	void saveBitmap(Bitmap bitmap, File filename) {

		Logger.Log("===== On SaveBitmap =====");
		Logger.Log("Width/Height: " + Integer.toString(bitmap.getWidth()) + "/"
				+ Integer.toString(bitmap.getHeight()));

		try {
			FileOutputStream out = new FileOutputStream(filename);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
			out.flush();
			out.close();

			Logger.Log("Path: " + filename.getAbsolutePath());

			photos.add(filename.getAbsolutePath());

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
		String fileName = uuid + ".jpg";
		File photo = new File(CJayConstant.APP_DIRECTORY_FILE, fileName);

		// Save Bitmap to JPEG
		saveBitmap(capturedBitmap, photo);

		if (capturedBitmap != null) {
			capturedBitmap.recycle();
			capturedBitmap = null;
			System.gc();
		}
	}

	// region Override Activity

	@Override
	protected void onResume() {
		Logger.Log("onResume()");
		super.onResume();
		openCamera();
		setContentView(R.layout.activity_camera);
	}

	void openCamera() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			Camera.CameraInfo info = new Camera.CameraInfo();

			for (int i = 0; i < Camera.getNumberOfCameras(); i++) {

				Camera.getCameraInfo(i, info);
				if (info.facing == cameraMode) {
					Logger.Log("inside onResume: camera != null");

					camera = Camera.open(i);
					setCameraDisplayOrientation(this, cameraMode, camera);

				}
			}
		}

		if (camera == null) {

			Logger.Log("inside onResume: camera == null");
			camera = Camera.open(cameraMode);
		}

		startPreview();
	}

	void releaseCamera() {
		if (camera != null) {

			Logger.Log("Release camera ... ");

			if (inPreview) {
				Logger.Log("Stop Preview ... ");
				camera.stopPreview();
				// preview.getHolder().removeCallback(null);
			}

			// camera.setPreviewCallback(null);
			camera.release();
			camera = null;

			// WARNING: lots of bugs may appear
			inPreview = false;
			cameraConfigured = false;

			Logger.Log("Release camera complete");
		}
	}

	@Override
	protected void onPause() {
		releaseCamera();
		super.onPause();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent imageReturnedIntent) {

		super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

		switch (requestCode) {
		case CJayConstant.SELECT_PHOTO:
			if (resultCode == RESULT_OK) {
				String selectedImagePath;
				String fileManagerString;

				// Retrieve imageUri and update local variable
				Uri selectedImage = imageReturnedIntent.getData();

				// OI FILE Manager
				fileManagerString = selectedImage.getPath();

				// MEDIA GALLERY
				selectedImagePath = getPath(selectedImage);

				if (selectedImagePath != null)
					itemUri = selectedImagePath;
				else
					itemUri = fileManagerString;

				String uuid = UUID.randomUUID().toString();

				// Save to database
				// uploadImage(uuid, itemUri);
			}
		}
	}

	@SuppressWarnings("deprecation")
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

	// region UIView Interaction
	@Click(R.id.btn_back)
	void backButtonClicked() {
		// releaseCamera();
		this.onBackPressed();
	}

	@Click(R.id.btn_switch_camera)
	void switchCameraButtonClicked() {

		Logger.Log("switchCameraButtonClicked()");
		if (inPreview && camera != null) {
			if (Camera.getNumberOfCameras() > 1) {

				if (inPreview) {
					camera.stopPreview();
				}
				camera.release();

				if (cameraMode == CameraInfo.CAMERA_FACING_BACK) {
					cameraMode = CameraInfo.CAMERA_FACING_FRONT;
					Logger.Log("CameraInfo.CAMERA_FACING_FRONT");
				} else {
					cameraMode = CameraInfo.CAMERA_FACING_BACK;
					Logger.Log("CameraInfo.CAMERA_FACING_BACK");
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

	@Click(R.id.btn_toggle_flash)
	void toggleFlashButtonClicked() {

		Logger.Log("toggleFlashButtonClicked()");

		if (inPreview && camera != null) {
			Parameters params = camera.getParameters();
			flashMode = params.getFlashMode();

			if (flashMode.equalsIgnoreCase(Parameters.FLASH_MODE_OFF)) {

				// toggleFlashButton.setImageResource(R.drawable.ic_flash_auto);
				params.setFlashMode(Parameters.FLASH_MODE_AUTO);

			} else if (flashMode.equalsIgnoreCase(Parameters.FLASH_MODE_AUTO)) {

				// toggleFlashButton.setImageResource(R.drawable.ic_flash_on);
				params.setFlashMode(Parameters.FLASH_MODE_ON);

			} else if (flashMode.equalsIgnoreCase(Parameters.FLASH_MODE_ON)) {

				// toggleFlashButton.setImageResource(R.drawable.ic_flash_off);
				params.setFlashMode(Parameters.FLASH_MODE_OFF);

			} else {

			}

			// Update camera and camera setting
			camera.setParameters(params);
			flashMode = params.getFlashMode();
		} else {
			Logger.Log("Camera does not open");
		}
	}

	@Click(R.id.btn_capture)
	void captureButtonClicked() {
		Logger.Log("captureButtonClicked()");
		takePicture();
	}

	@Background
	void takePicture() {
		if (inPreview) {
			Logger.Log("Prepare to take picture");

			camera.takePicture(shutterCallback, null, photoCallback);
			inPreview = false;
		}
	}

	PictureCallback rawCallback = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			Logger.Log("rawCallback");
		}
	};

	@Click(R.id.btn_camera_done)
	void doneButtonClicked() {

		// TODO: this is the list
		for (String photo : photos) {
			// TODO: do something
		}

		this.onBackPressed();
	}

	// endregion

	// region Camera Support method
	// endregion

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

		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360; // compensate the mirror
		} else { // back-facing
			result = (info.orientation - degrees + 360) % 360;
		}
		camera.setDisplayOrientation(result);
	}

	private Camera.Size getBestPreviewSize(int width, int height,
			Camera.Parameters parameters) {
		Camera.Size result = null;

		for (Camera.Size size : parameters.getSupportedPreviewSizes()) {

			if ((size.width <= width && size.height <= height)
					|| (size.width <= height && size.height <= width)) {
				if (result == null) {
					result = size;
				} else {
					int resultArea = result.width * result.height;
					int newArea = size.width * size.height;

					if (newArea > resultArea) {
						result = size;
					}
				}
			}
		}

		return (result);
	}

	private Camera.Size getSmallestPictureSize(Camera.Parameters parameters) {
		Camera.Size result = null;

		for (Camera.Size size : parameters.getSupportedPictureSizes()) {
			if (result == null) {
				result = size;
			} else {
				int resultArea = result.width * result.height;
				int newArea = size.width * size.height;

				if (newArea < resultArea) {
					result = size;
				}
			}
		}

		return (result);
	}

	private Camera.Size getBestPictureSize(Camera.Parameters parameters) {

		for (Camera.Size size : parameters.getSupportedPictureSizes()) {

		}

		Camera.Size result = null;

		List<Size> sizes = parameters.getSupportedPictureSizes();
		int index = 2;
		if (sizes.size() > index) {
			Camera.Size first = sizes.get(sizes.size() - index - 1);
			Camera.Size second = sizes.get(index);

			int firstArea = first.width * first.height;
			int secondArea = second.width * second.height;

			if (firstArea < secondArea)
				result = second;
			else
				result = first;

		} else {
			result = sizes.get(sizes.size() - 1);
		}
		return (result);

		// for (Camera.Size size : parameters.getSupportedPictureSizes()) {
		// if (result == null) {
		// result = size;
		// } else {
		// int resultArea = result.width * result.height;
		// int newArea = size.width * size.height;
		//
		// if (newArea > resultArea) {
		// result = size;
		// }
		// }
		// }
		// return (result);
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

	// // TODO: for FOCUS modes
	// private Rect calculateFocusArea(float x, float y) {
	// int left = clamp(
	// Float.valueOf((x / getSurfaceView().getWidth()) * 2000 - 1000)
	// .intValue(), focusAreaSize);
	// int top = clamp(
	// Float.valueOf((y / getSurfaceView().getHeight()) * 2000 - 1000)
	// .intValue(), focusAreaSize);
	//
	// return new Rect(left, top, left + focusAreaSize, top + focusAreaSize);
	// }
	//
	// protected void focusOnTouch(MotionEvent event) {
	// if (camera != null) {
	//
	// camera.cancelAutoFocus();
	// Rect focusRect = calculateTapArea(event.getX(), event.getY(), 1f);
	// Rect meteringRect = calculateTapArea(event.getX(), event.getY(),
	// 1.5f);
	//
	// Parameters parameters = camera.getParameters();
	// parameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
	// parameters.setFocusAreas(Lists.newArrayList(new Camera.Area(
	// focusRect, 1000)));
	//
	// if (meteringAreaSupported) {
	// parameters.setMeteringAreas(Lists.newArrayList(new Camera.Area(
	// meteringRect, 1000)));
	// }
	//
	// camera.setParameters(parameters);
	// camera.autoFocus(this);
	// }
	// }
	//
	//
	// endregion
}
