package com.cloudjay.cjay.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.activity.ReuseActivity_;
import com.cloudjay.cjay.event.ImageCapturedEvent;
import com.cloudjay.cjay.model.AuditImage;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.model.GateImage;
import com.cloudjay.cjay.task.jobqueue.UploadImageJob;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.PreferencesUtil;
import com.cloudjay.cjay.util.StringHelper;
import com.cloudjay.cjay.util.Utils;
import com.cloudjay.cjay.util.enums.ImageType;
import com.cloudjay.cjay.util.enums.Step;
import com.commonsware.cwac.camera.PictureTransaction;
import com.commonsware.cwac.camera.SimpleCameraHost;
import com.path.android.jobqueue.JobManager;
import com.snappydb.SnappydbException;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.UUID;

import de.greenrobot.event.EventBus;

/**
 * 1. Để xử lý hình ảnh sau khi chụp, xem hàm CameraHost#saveImage()
 * 2. Để xử lý upload image, xem hàm CameraHost#addImageToUploadQueue()
 * 3.
 */
@EFragment
public class CameraFragment extends com.commonsware.cwac.camera.CameraFragment {

	public interface Contract {
		boolean isSingleShotMode();

		void setSingleShotMode(boolean mode);
	}

	//region ATTR

	public final static String CONTAINER_ID_EXTRA = "com.cloudjay.wizard.containerID";
	public final static String OPERATOR_CODE_EXTRA = "com.cloudjay.wizard.operatorCode";
	public final static String IMAGE_TYPE_EXTRA = "com.cloudjay.wizard.imageType";
	public final static String CURRENT_STEP_EXTRA = "com.cloudjay.wizard.currentStep";
    // This Extra bundle is use to open Detail Issue Activity only
    public final static String AUDIT_ITEM_UUID_EXTRA = "com.cloudjay.wizard.auditItemUUID";

	private static final int PICTURE_SIZE_MAX_WIDTH = 640;
	private boolean singleShotProcessing = false;

	@FragmentArg(IMAGE_TYPE_EXTRA)
	int mType = 0;

	@FragmentArg(CONTAINER_ID_EXTRA)
	String containerId;

	@FragmentArg(OPERATOR_CODE_EXTRA)
	String operatorCode;

	@FragmentArg(CURRENT_STEP_EXTRA)
	int currentStep;

    @FragmentArg(AUDIT_ITEM_UUID_EXTRA)
    AuditItem auditItem;

	@Bean
	DataCenter dataCenter;

	//endregion

	//region VIEW

	@ViewById(R.id.btn_capture)
	ImageButton btnTakePicture;

	@ViewById(R.id.btn_capture_mode)
	ToggleButton btnCameraMode;

	@ViewById(R.id.btn_camera_done)
	Button btnDone;

	@ViewById(R.id.btn_use_gate_image)
	Button btnUseGateImage;
	//endregion

	//region VIEW INTERACTION
	@Click
	void btnUseGateImageClicked() {
		// Open ReuseActivity
		Intent intent = new Intent(getActivity(), ReuseActivity_.class);
		intent.putExtra(ReuseActivity_.CONTAINER_ID_EXTRA, containerId);
		startActivityForResult(intent, 1);
	}

	@Click(R.id.btn_camera_done)
	void btnDoneClicked() {
		EventBus.getDefault().post(new ImageCapturedEvent(containerId, mType, auditItem));
		getActivity().finish();
	}

	@Click(R.id.btn_capture)
	void btnTakePictureClicked() {
		btnTakePicture.setEnabled(false);
		autoFocus();
	}

	@Click(R.id.btn_capture_mode)
	void btnCameraModeClicked() {
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
	//endregion


	PictureTransaction xact;

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);
		SimpleCameraHost.Builder builder = new SimpleCameraHost.Builder(new CameraHost(getActivity()));
		setHost(builder.useFullBleedPreview(true).build());

		// You can move it to CameraHost#takeSimplePicture to tag Object
		xact = new PictureTransaction(getHost());
		xact.needBitmap(true);
		xact.flashMode(Camera.Parameters.FLASH_MODE_AUTO);

	}

	@AfterViews
	void afterView() {

		// Config shot mode. Default is FALSE.
		// Configure View visibility based on current step of session
		Step step = Step.values()[currentStep];

		Logger.Log("Current Step of session: " + step.toString());
		switch (step) {

			case AUDIT:
				btnUseGateImage.setVisibility(View.VISIBLE);
				btnCameraMode.setVisibility(View.VISIBLE);
				break;

			default:
				getContract().setSingleShotMode(false);
				btnUseGateImage.setVisibility(View.GONE);
				btnCameraMode.setVisibility(View.GONE);
				break;
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View cameraView = super.onCreateView(inflater, container, savedInstanceState);
		View results = inflater.inflate(R.layout.fragment_camera, container, false);
		((ViewGroup) results.findViewById(R.id.camera)).addView(cameraView);
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

	Contract getContract() {
		return ((Contract) getActivity());
	}

	/**
	 * Take a picture, need to call auto focus before taking picture
	 */
	public void takeSimplePicture() {

		if (getContract().isSingleShotMode() == true) {
			singleShotProcessing = true;
			btnTakePicture.setEnabled(false);
		}

		// xact.tag()

		// Call it with PictureTransaction to take picture with configuration in CameraHost
		// Process image in Subclass of `CameraHost#saveImage`
		takePicture(xact);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
			getActivity().finish();
		}
	}

	/**
	 * CameraHost is the interface use to configure behavior of camera ~ setting.
	 */
	class CameraHost extends SimpleCameraHost {

		public CameraHost(Context _ctxt) {
			super(_ctxt);
		}

		/**
		 * MAIN: > Process taken picture:
		 * 1. Create directory
		 * 2. Save Bitmap to File
		 * 3. Add image to Queue
		 *
		 * @param xact
		 * @param capturedBitmap
		 */
		@Override
		public void saveImage(PictureTransaction xact, Bitmap capturedBitmap) {

			if (useSingleShotMode()) {
				singleShotProcessing = false;
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						btnTakePicture.setEnabled(true);
					}
				});
			}

			// Save bitmap
			File photo = getFile();
			saveBitmapToFile(capturedBitmap, photo);

			// Add taken picture to job queue
			addImageToUploadQueue(photo.getAbsolutePath(), photo.getName());
		}

		@Override
		public boolean useSingleShotMode() {
			return (getContract().isSingleShotMode());
		}

		/**
		 * Add new image to database based on `mType`.
		 * Add image at `uri` to job queue.
		 *
		 * @param uri
		 * @param imageName
		 * @throws SnappydbException
		 */
		protected void addImageToUploadQueue(String uri, String imageName) {
			try {

				// Create image based on mType and add this image to database
				ImageType type = ImageType.values()[mType];
				switch (type) {
					case IMPORT:
					case EXPORT:
						GateImage gateImage = new GateImage()
								.withId(0)
								.withType(mType)
								.withName(imageName)
								.withUrl("file://" + uri);

						dataCenter.addGateImage(getActivity().getApplicationContext(), gateImage, containerId);
						break;

					case AUDIT:
					case REPAIRED:
					default:
						Logger.Log("save audit / repaired image to database");
						AuditImage auditImage = new AuditImage()
								.withId(0)
								.withType(mType)
								.withUrl("file://" + uri)
								.withName(imageName);

						dataCenter.addAuditImage(getActivity().getApplicationContext(), auditImage, containerId);
						break;
				}

				// Add image to job queue
				JobManager jobManager = App.getJobManager();
				jobManager.addJobInBackground(new UploadImageJob(uri, imageName, containerId));

			} catch (SnappydbException e) {
				Utils.showCrouton(getActivity(), e.getMessage());
			}
		}

		/**
		 * Save bitmap to file
		 *
		 * @param bitmap
		 * @param filename
		 */
		protected void saveBitmapToFile(Bitmap bitmap, File filename) {
			try {
				FileOutputStream out = new FileOutputStream(filename);
				bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
				out.flush();
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/**
		 * Get image type string based on input image type
		 *
		 * @param imgType
		 * @return
		 */
		protected String getImageTypeString(int imgType) {
			ImageType type = ImageType.values()[imgType];
			switch (type) {
				case IMPORT:
					return "gate-in";

				case EXPORT:
					return "gate-out";

				case AUDIT:
					return "auditor";

				case REPAIRED:
				default:
					return "repair";
			}
		}

		/**
		 * Create directory for saving image
		 *
		 * @return
		 */
		protected File getFile() {

			// Save Bitmap to Files
			String uuid = UUID.randomUUID().toString();
			String imageType = getImageTypeString(mType);

			// create today String
			String today = StringHelper.getCurrentTimestamp(CJayConstant.DAY_FORMAT);
			String depotCode = PreferencesUtil.getPrefsValue(getActivity(), PreferencesUtil.PREF_USER_DEPOT);

			// create directory to save images
			File newDirectory = new File(CJayConstant.APP_DIRECTORY_FILE, depotCode + "/" + today + "/" + imageType
					+ "/" + containerId);

			if (!newDirectory.exists()) {
				newDirectory.mkdirs();
			}

			// create image file name
			String fileName = depotCode + "-" + today + "-" + imageType + "-" + containerId + "-" + operatorCode + "-"
					+ uuid + ".jpg";

			File photo = new File(newDirectory, fileName);

			return photo;
		}

		@Override
		public void onCameraFail(com.commonsware.cwac.camera.CameraHost.FailureReason reason) {
			super.onCameraFail(reason);
			Toast.makeText(getActivity(), "Sorry, but you cannot use the camera now!", Toast.LENGTH_LONG).show();
		}

		@Override
		@TargetApi(16)
		public void onAutoFocus(boolean success, Camera camera) {
			super.onAutoFocus(success, camera);
			btnTakePicture.setEnabled(true);
			takeSimplePicture();
		}

		/**
		 * Calculate best size for saved image
		 *
		 * @param sizes
		 * @return
		 */
		protected Camera.Size determineBestSize(List<Camera.Size> sizes) {
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
			return determineBestSize(sizes);
		}

		@Override
		public Camera.Size getPictureSize(PictureTransaction xact, Camera.Parameters parameters) {
			List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
			return determineBestSize(sizes);
		}


		boolean supportsFaces = false;
	}

}