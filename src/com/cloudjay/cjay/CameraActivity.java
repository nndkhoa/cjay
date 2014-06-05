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
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.aerilys.helpers.android.UIHelper;
import com.cloudjay.cjay.dao.CJayImageDaoImpl;
import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.dao.IssueDaoImpl;
import com.cloudjay.cjay.events.CJayImageAddedEvent;
import com.cloudjay.cjay.events.ContainerSessionChangedEvent;
import com.cloudjay.cjay.events.ContainerSessionUpdatedEvent;
import com.cloudjay.cjay.events.UploadStateRestoredEvent;
import com.cloudjay.cjay.fragment.GateExportListFragment;
import com.cloudjay.cjay.fragment.GateImportListFragment;
import com.cloudjay.cjay.model.AuditReportItem;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.Container;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.Depot;
import com.cloudjay.cjay.model.GateReportImage;
import com.cloudjay.cjay.model.Issue;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.network.CJayClient;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.CJaySession;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.PreferencesUtil;
import com.cloudjay.cjay.util.QueryHelper;
import com.cloudjay.cjay.util.StringHelper;
import com.cloudjay.cjay.util.UserRole;
import com.cloudjay.cjay.util.Utils;

import de.greenrobot.event.EventBus;
import de.keyboardsurfer.android.widget.crouton.Crouton;

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

	public static final String CJAY_CONTAINER_SESSION_EXTRA = "cjay_container_session";
	public static final String CJAY_ISSUE_EXTRA = "cjay_issue_session";
	public static final String CJAY_IMAGE_TYPE_EXTRA = "type";
	public static final String SOURCE_TAG_EXTRA = "tag";

	public static void setCameraDisplayOrientation(Activity activity, int cameraId, android.hardware.Camera camera) {

		if (camera != null) {

			android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
			android.hardware.Camera.getCameraInfo(cameraId, info);

			int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();

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

			Logger.Log("Rotate degree: " + degrees);

			int result;
			if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
				result = (info.orientation + degrees) % 360;
				result = (360 - result) % 360; // compensate the mirror

			} else { // back-facing
				result = (info.orientation - degrees + 360) % 360;
			}

			camera.setDisplayOrientation(result);
		}
	}

	Camera mCamera = null;
	MediaPlayer mShootMediaPlayer = null;
	private SurfaceHolder mPreviewHolder = null;
	private boolean mInPreview = false;
	private boolean mCameraConfigured = false;
	String mFlashMode = Camera.Parameters.FLASH_MODE_OFF;
	int mCameraMode = Camera.CameraInfo.CAMERA_FACING_BACK;

	List<GateReportImage> mGateReportImages = new ArrayList<GateReportImage>();
	List<AuditReportItem> mAuditReportItems = new ArrayList<AuditReportItem>();

	private static final int PICTURE_SIZE_MAX_WIDTH = 640;
	private static final int PREVIEW_SIZE_MAX_WIDTH = 1280;

	@ViewById(R.id.camera_preview)
	SurfaceView mPreview;

	@ViewById(R.id.btn_back)
	ImageButton mBackButton;

	@ViewById(R.id.btn_switch_camera)
	ImageButton mSwitchCameraButton;

	@ViewById(R.id.btn_toggle_flash)
	ImageButton mToggleFlashButton;

	@ViewById(R.id.btn_capture)
	ImageButton mCaptureButton;

	@ViewById(R.id.btn_camera_done)
	Button mDoneButton;

	@ViewById(R.id.rl_camera_done)
	RelativeLayout mCameraDoneLayout;

	@ViewById(R.id.btn_capture_mode)
	ToggleButton captureModeToggleButton;

	@SystemService
	AudioManager mAudioManager;

	@Extra(CJAY_CONTAINER_SESSION_EXTRA)
	String mContainerSessionUUID = "";

	@Extra(CJAY_ISSUE_EXTRA)
	String mIssueUUID = "";

	@Extra(CJAY_IMAGE_TYPE_EXTRA)
	int mType = 0;

	// endregion

	@Extra(SOURCE_TAG_EXTRA)
	String mSourceTag = "";

	String containerId;
	String operatorCode;
	String depotCode;
	String imageIdPath;

	SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

			try {
				Logger.Log("onSurfaceChanged");

				initPreview(width, height);
				startPreview();

				Logger.Log("endSurfaceChanged");

			} catch (Exception e) {

				e.printStackTrace();
				Toast.makeText(getApplicationContext(), R.string.alert_camera_preview, Toast.LENGTH_LONG).show();
			}
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			// no-op -- wait until surfaceChanged()
			Logger.Log("onSurfaceCreated");
			mInPreview = true;
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			// no-op
			Logger.Log("onSurfaceDestroyed");

		}
	};

	Camera.PictureCallback photoCallback = new Camera.PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			try {
				savePhoto(data);
				camera.startPreview();
				mInPreview = true;
			} catch (Exception e) {

				e.printStackTrace();
				releaseCamera();

			}
		}
	};

	ShutterCallback shutterCallback = new ShutterCallback() {

		@Override
		public void onShutter() {
			int volume = mAudioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);

			if (volume != 0) {
				if (mShootMediaPlayer == null) {
					mShootMediaPlayer = MediaPlayer.create(	getApplicationContext(),
															Uri.parse("file:///system/media/audio/ui/camera_click.ogg"));
				}

				if (mShootMediaPlayer != null) {
					mShootMediaPlayer.start();
				}
			}

			// Logger.Log( "onShutter");
		}
	};

	@Click(R.id.btn_capture)
	void captureButtonClicked() {
		takePicture();
	}

	@Click(R.id.btn_capture_mode)
	void captureModeToggleButtonClicked() {

		if (captureModeToggleButton.isChecked()) {
			Toast.makeText(this, "Kích hoạt chế độ chụp liên tục", Toast.LENGTH_SHORT).show();
			PreferencesUtil.storePrefsValue(this, PreferencesUtil.PREF_CAMERA_MODE_CONTINUOUS, true);
		} else {
			Toast.makeText(this, "Đã dừng chế độ chụp liên tục", Toast.LENGTH_SHORT).show();
			PreferencesUtil.storePrefsValue(this, PreferencesUtil.PREF_CAMERA_MODE_CONTINUOUS, false);
		}

	}

	Bitmap createScaledBitmap(byte[] data) {
		if (data != null) {

			int screenWidth = getResources().getDisplayMetrics().widthPixels;
			int screenHeight = getResources().getDisplayMetrics().heightPixels;

			Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data != null ? data.length : 0);

			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

				// Notice that width and height are reversed
				Bitmap scaled = Bitmap.createScaledBitmap(bm, screenHeight, screenWidth, true);
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
				Bitmap scaled = Bitmap.createScaledBitmap(bm, screenWidth, screenHeight, true);
				bm = scaled;
			}

			return bm;
		}
		return null;
	}

	private Size determineBestPictureSize(Camera.Parameters parameters) {
		List<Size> sizes = parameters.getSupportedPictureSizes();

		return determineBestSize(sizes, PICTURE_SIZE_MAX_WIDTH);
	}

	private Size determineBestPreviewSize(Camera.Parameters parameters) {
		List<Size> sizes = parameters.getSupportedPreviewSizes();

		return determineBestSize(sizes, PREVIEW_SIZE_MAX_WIDTH);
	}

	protected Size determineBestSize(List<Size> sizes, int widthThreshold) {
		Size bestSize = null;

		for (Size currentSize : sizes) {
			boolean isDesiredRatio = currentSize.width / 4 == currentSize.height / 3;
			boolean isBetterSize = bestSize == null || currentSize.width > bestSize.width;
			boolean isInBounds = currentSize.width <= PICTURE_SIZE_MAX_WIDTH;

			if (isDesiredRatio && isInBounds && isBetterSize) {
				bestSize = currentSize;
			}
		}

		return bestSize;
	}

	@Click({ R.id.btn_camera_done, R.id.btn_back, R.id.rl_camera_done })
	void doneButtonClicked() {
		onBackPressed();
	}

	@AfterViews
	void afterViews() {

		// init container sessions
		// loadData();

		Logger.Log("----> initCamera(), addSurfaceCallback");

		// WARNING: this block should be run before onResume()
		// Setup Surface Holder
		mPreviewHolder = mPreview.getHolder();
		mPreviewHolder.addCallback(surfaceCallback);
		mPreviewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		// Restore camera state from database or somewhere else
		// mFlashMode = Camera.Parameters.FLASH_MODE_OFF;
		// mCameraMode = Camera.CameraInfo.CAMERA_FACING_BACK;
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		// TYPE_REPORT images can be captured in single mode (non-continuous)
		// Only two users use this:
		// - Auditor: can see captureModeToggleButton, can set mode to single/continuous
		// - Repair: can NOT see captureModeToggleButton, mode always is single,
		// user is forced to report issue after capturing an image
		try {
			if (mType == CJayImage.TYPE_AUDIT) {
				boolean isContinuous;
				if (CJaySession.restore(this).getUserRole() == UserRole.AUDITOR.getValue()) {
					isContinuous = PreferencesUtil.getPrefsValue(	getApplicationContext(),
																	PreferencesUtil.PREF_CAMERA_MODE_CONTINUOUS, false);
					captureModeToggleButton.setVisibility(View.VISIBLE);
				} else {
					isContinuous = false;
					PreferencesUtil.storePrefsValue(getApplicationContext(),
													PreferencesUtil.PREF_CAMERA_MODE_CONTINUOUS, false);
					captureModeToggleButton.setVisibility(View.GONE);
				}

				PreferencesUtil.storePrefsValue(getApplicationContext(), PreferencesUtil.PREF_CAMERA_MODE_CONTINUOUS,
												isContinuous);
				captureModeToggleButton.setChecked(isContinuous);

			} else {

				captureModeToggleButton.setVisibility(View.GONE);
				captureModeToggleButton.setChecked(true);
				PreferencesUtil.storePrefsValue(this, PreferencesUtil.PREF_CAMERA_MODE_CONTINUOUS, true);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		SQLiteDatabase db = DataCenter.getDatabaseHelper(getApplicationContext()).getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from cs_full_info_view where _id = ?",
									new String[] { mContainerSessionUUID });

		if (cursor.moveToFirst()) {
			containerId = cursor.getString(cursor.getColumnIndexOrThrow(Container.CONTAINER_ID));
			operatorCode = cursor.getString(cursor.getColumnIndexOrThrow(Operator.FIELD_CODE));
			depotCode = cursor.getString(cursor.getColumnIndexOrThrow(Depot.DEPOT_CODE));
			imageIdPath = cursor.getString(cursor.getColumnIndexOrThrow(ContainerSession.FIELD_IMAGE_ID_PATH));
		}
	}

	void initPreview(int width, int height) {
		Logger.Log("initPreview()");

		if (mCamera != null && mPreviewHolder.getSurface() != null) {
			try {
				Logger.Log("setPreviewDisplay");
				mCamera.setPreviewDisplay(mPreviewHolder);
				// camera.setPreviewCallback(null);
				// camera.setOneShotPreviewCallback(null);

			} catch (Throwable t) {
				Logger.e("Exception in setPreviewDisplay()");
				Toast.makeText(this, t.getMessage(), Toast.LENGTH_LONG).show();
			}

			if (!mCameraConfigured) {

				Logger.Log("config Camera");

				Camera.Parameters parameters = mCamera.getParameters();
				Camera.Size size = determineBestPreviewSize(parameters);
				Camera.Size pictureSize = determineBestPictureSize(parameters);

				// Logger.Log("PreviewSize: " + Integer.toString(size.width) + "/" + Integer.toString(size.height));
				// Logger.Log("PictureSize: " + Integer.toString(pictureSize.width) + "/"
				// + Integer.toString(pictureSize.height));

				try {
					if (size != null && pictureSize != null) {
						parameters.setPreviewSize(size.width, size.height);

						parameters.setPictureSize(pictureSize.width, pictureSize.height);

						parameters.setPictureFormat(ImageFormat.JPEG);
						parameters.setFlashMode(mFlashMode);
						parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

						mCamera.setParameters(parameters);
						mCameraConfigured = true;
					}
				} catch (NotFoundException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void onAutoFocus(boolean arg0, Camera arg1) {

		try {
			Logger.Log("Auto focused, now take picture");
			mCamera.takePicture(shutterCallback, null, photoCallback);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onBackPressed() {

		// Open GridView
		if (mSourceTag.equals(GateImportListFragment.LOG_TAG)) {
			CJayApplication.openPhotoGridView(	this, mContainerSessionUUID, containerId, CJayImage.TYPE_IMPORT,
												GateImportListFragment.LOG_TAG);
		} else if (mSourceTag.equals(GateExportListFragment.LOG_TAG)) {
			CJayApplication.openPhotoGridView(	this, mContainerSessionUUID, containerId, CJayImage.TYPE_EXPORT,
												CJayImage.TYPE_REPAIRED, GateExportListFragment.LOG_TAG);
		}

		super.onBackPressed();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {

		// cameraConfigured = false;
		super.onConfigurationChanged(newConfig);

		// setCameraDisplayOrientation(this, cameraMode, camera);
		// onResume();

		setContentView(R.layout.activity_camera);
		openCamera();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {

			takePicture();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onPause() {
		releaseCamera();
		super.onPause();
	}

	// region Override Activity
	@Override
	protected void onResume() {

		Logger.Log("----> onResume()");
		super.onResume();

		openCamera();
		setContentView(R.layout.activity_camera);

	}

	void openCamera() {

		if (mCamera != null) {
			mCamera.release();
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			Camera.CameraInfo info = new Camera.CameraInfo();

			for (int i = 0; i < Camera.getNumberOfCameras(); i++) {

				Camera.getCameraInfo(i, info);
				if (info.facing == mCameraMode) {
					Logger.Log("inside onResume: camera != null");

					mCamera = Camera.open(i);
					setCameraDisplayOrientation(this, mCameraMode, mCamera);
				}
			}
		}

		if (mCamera == null) {
			Logger.Log("inside onResume: camera == null");
			mCamera = Camera.open(mCameraMode);
		}

		startPreview();
	}

	void releaseCamera() {

		if (mCamera != null) {

			Logger.Log("Release camera ... ");

			if (mInPreview) {
				Logger.Log("Stop Preview ... ");
				mCamera.stopPreview();
				// preview.getHolder().removeCallback(null);
			}

			// camera.setPreviewCallback(null);
			mCamera.release();
			mCamera = null;

			// WARNING: lots of bugs may appear
			mInPreview = false;
			mCameraConfigured = false;

			Logger.Log("Release camera complete");
		}
	}

	void saveBitmapToFile(Bitmap bitmap, File filename) {

		// Logger.Log("===== On SaveBitmap =====");
		// Logger.Log("Width/Height: " + Integer.toString(bitmap.getWidth()) + "/" +
		// Integer.toString(bitmap.getHeight()));

		try {

			// Logger.Log("Path: " + filename.getAbsolutePath());

			FileOutputStream out = new FileOutputStream(filename);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
			out.flush();
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Background
	void savePhoto(byte[] data) {

		synchronized (this) {
			// Convert rotated byte[] to Bitmap
			Bitmap capturedBitmap = saveToBitmap(data);

			// Save Bitmap to Files
			String uuid = UUID.randomUUID().toString();

			String imageType;
			switch (mType) {
				case CJayImage.TYPE_IMPORT:
					imageType = "gate-in";
					break;

				case CJayImage.TYPE_EXPORT:
					imageType = "gate-out";
					break;

				case CJayImage.TYPE_AUDIT:
					imageType = "auditor";
					break;

				case CJayImage.TYPE_REPAIRED:
				default:
					imageType = "repair";
					break;
			}

			// file name example:
			// [depot-code]-2013-12-19-[gate-in|gate-out|report]-[containerId]-[UUID].jpg
			String currentTimestamp = StringHelper.getCurrentTimestamp("yyyy-MM-dd");

			String fileName = depotCode + "-" + currentTimestamp + "-" + imageType + "-" + containerId + "-"
					+ operatorCode + "-" + uuid + ".jpg";

			File newDirectory = new File(CJayConstant.APP_DIRECTORY_FILE, depotCode + "/" + currentTimestamp + "/"
					+ imageType + "/" + containerId);

			if (!newDirectory.exists()) {
				newDirectory.mkdirs();
			}

			// Save Bitmap to JPEG
			File photo = new File(newDirectory, fileName);
			saveBitmapToFile(capturedBitmap, photo);

			// Upload image --> add image to queue
			uploadImage(uuid, "file://" + photo.getAbsolutePath(), fileName);
			DataCenter.getDatabaseHelper(this).addUsageLog(containerId + " | Captured " + fileName);

			if (capturedBitmap != null) {
				capturedBitmap.recycle();
				capturedBitmap = null;
				System.gc();
			}
		}

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

				Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data != null ? data.length : 0);

				android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
				android.hardware.Camera.getCameraInfo(mCameraMode, info);

				int rotation = getWindowManager().getDefaultDisplay().getRotation();

				Matrix mtx = new Matrix();

				if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT && rotation == Surface.ROTATION_0) {
					mtx.postRotate(270);
				} else {
					mtx.postRotate(90);
				}

				Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), mtx, true);

				if (bm != null) {
					bm.recycle();
					bm = null;
					System.gc();
				}

				return rotatedBitmap;

			} else {

				int rotation = getWindowManager().getDefaultDisplay().getRotation();

				if (rotation == Surface.ROTATION_270) {

					Matrix mtx = new Matrix();
					mtx.postRotate(180);

					// Flip Bitmap
					Logger.Log("Flip image");
					Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data != null ? data.length : 0);
					bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), mtx, true);
					return bm;

				} else {
					// LANDSCAPE MODE --> No need to reverse width and height
					Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data != null ? data.length : 0);
					return bm;
				}
			}
		}
		return null;
	}

	void startPreview() {
		Logger.Log("----> startPreview");

		if (mCameraConfigured && mCamera != null) {

			Logger.Log("cameraConfigured and camera != null");
			mCamera.startPreview();
			mInPreview = true;
		}
	}

	@Click(R.id.btn_switch_camera)
	void switchCameraButtonClicked() {

		Logger.Log("switchCameraButtonClicked()");
		if (mInPreview && mCamera != null) {
			if (Camera.getNumberOfCameras() > 1) {

				if (mInPreview) {
					mCamera.stopPreview();
				}
				mCamera.release();

				if (mCameraMode == CameraInfo.CAMERA_FACING_BACK) {
					mCameraMode = CameraInfo.CAMERA_FACING_FRONT;
					Logger.Log("CameraInfo.CAMERA_FACING_FRONT");
				} else {
					mCameraMode = CameraInfo.CAMERA_FACING_BACK;
					Logger.Log("CameraInfo.CAMERA_FACING_BACK");
				}

				mCamera = Camera.open(mCameraMode);
				setCameraDisplayOrientation(this, mCameraMode, mCamera);

				try {
					mCamera.setPreviewDisplay(mPreviewHolder);
				} catch (IOException e) {
					e.printStackTrace();
				}
				mCamera.startPreview();

			} else {
				UIHelper.toast(this, "Device has only one camera");
			}
		}

	}

	@Background
	void takePicture() {

		if (mInPreview) {

			try {
				// Logger.Log( "Prepare to take picture");
				Camera.Parameters cameraParameters = mCamera.getParameters();

				List<String> supportedFocusMode = cameraParameters.getSupportedFocusModes();

				// Submit focus area to camera
				if (supportedFocusMode.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {

					cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
					mCamera.setParameters(cameraParameters);
					mCamera.autoFocus(this);

				} else {

					Logger.Log("No auto focus mode supported, now just take picture");
					mCamera.takePicture(shutterCallback, null, photoCallback);

				}

				mInPreview = false;

			} catch (Exception e) {
				e.printStackTrace();
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

		// Logger.Log( "toggleFlashButtonClicked()");

		if (mInPreview && mCamera != null) {
			Parameters params = mCamera.getParameters();
			mFlashMode = params.getFlashMode();

			if (mFlashMode.equalsIgnoreCase(Parameters.FLASH_MODE_OFF)) {

				mToggleFlashButton.setImageResource(R.drawable.ic_flash_auto);
				params.setFlashMode(Parameters.FLASH_MODE_AUTO);

			} else if (mFlashMode.equalsIgnoreCase(Parameters.FLASH_MODE_AUTO)) {

				mToggleFlashButton.setImageResource(R.drawable.ic_flash_on);
				params.setFlashMode(Parameters.FLASH_MODE_ON);

			} else if (mFlashMode.equalsIgnoreCase(Parameters.FLASH_MODE_ON)) {

				mToggleFlashButton.setImageResource(R.drawable.ic_flash_off);
				params.setFlashMode(Parameters.FLASH_MODE_OFF);

			} else {

			}

			// Update camera and camera setting
			mCamera.setParameters(params);
			mFlashMode = params.getFlashMode();

		} else {
			Logger.Log("Camera does not open");
		}
	}

	private synchronized void uploadImage(String uuid, String uri, String image_name) {

		// Set container session image_id_path
		if (TextUtils.isEmpty(imageIdPath)
				|| imageIdPath.equals("https://storage.googleapis.com/storage-cjay.cloudjay.com/")) {
			Logger.Log("Set container image_id_path: " + uri);
			imageIdPath = uri;
			QueryHelper.update(	getApplicationContext(), "container_session", ContainerSession.FIELD_IMAGE_ID_PATH,
								imageIdPath,
								ContainerSession.FIELD_UUID + " = " + Utils.sqlString(mContainerSessionUUID));
		}

		// Create new image and add to queue
		SQLiteDatabase db = DataCenter.getDatabaseHelper(getApplicationContext()).getWritableDatabase();
		ContentValues imageValues = new ContentValues();
		imageValues.put("containerSession_id", mContainerSessionUUID);
		imageValues.put("uuid", uuid);
		imageValues.put("image_name", image_name);
		imageValues.put("time_posted", StringHelper.getCurrentTimestamp(CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE));
		imageValues.put("_id", uri);
		imageValues.put("type", mType);
		imageValues.put("state", CJayImage.STATE_UPLOAD_WAITING);

		// imageValues.put("id", 0);
		if (TextUtils.isEmpty(mIssueUUID)) {
			imageValues.put("issue_id", mIssueUUID);
		}
		db.insertWithOnConflict("cjay_image", null, imageValues, SQLiteDatabase.CONFLICT_REPLACE);

		// 1. start broadcast receiver
		Intent i = new Intent();
		i.setAction(CJayConstant.INTENT_PHOTO_TAKEN);
		sendBroadcast(i);

		if (!Utils.isAlarmUp(this)) {
			Utils.startAlarm(this);
		}

		// tell people that an image has been created
		if (!TextUtils.isEmpty(mSourceTag)) {
			EventBus.getDefault().post(new CJayImageAddedEvent(uuid, mSourceTag));
			if (!PreferencesUtil.getPrefsValue(	getApplicationContext(), PreferencesUtil.PREF_CAMERA_MODE_CONTINUOUS,
												true)) {
				showIssueReportDialog(uuid);
			}
		}
	}

	@UiThread
	public void showIssueReportDialog(String cjayImageUuid) {

		if (mSourceTag.equals("AuditorContainerActivity")) {
			CJayApplication.openReportDialog(this, cjayImageUuid, mContainerSessionUUID);
		} else if (mSourceTag.equals("RepairIssuePendingListFragment")) {
			CJayApplication.openReportDialog(this, cjayImageUuid, mContainerSessionUUID);
		}

	}
}
