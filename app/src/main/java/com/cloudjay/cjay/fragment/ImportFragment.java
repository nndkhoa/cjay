package com.cloudjay.cjay.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.activity.CameraActivity_;
import com.cloudjay.cjay.activity.ReuseActivity;
import com.cloudjay.cjay.activity.ReuseActivity_;
import com.cloudjay.cjay.activity.WizardActivity;
import com.cloudjay.cjay.activity.WizardActivity_;
import com.cloudjay.cjay.adapter.GateImageAdapter;
import com.cloudjay.cjay.event.session.ContainerGotEvent;
import com.cloudjay.cjay.event.image.ImageCapturedEvent;
import com.cloudjay.cjay.event.operator.OperatorChosenEvent;
import com.cloudjay.cjay.fragment.dialog.SearchOperatorDialog_;
import com.cloudjay.cjay.model.GateImage;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.job.UploadImageJob;
import com.cloudjay.cjay.task.job.UploadImportJob;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.Utils;
import com.cloudjay.cjay.util.enums.ImageType;
import com.cloudjay.cjay.util.enums.Step;
import com.path.android.jobqueue.JobManager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.CheckedChange;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.Touch;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Màn hình nhập.
 * <p/>
 * 1. Nhận vào containerId, cố gắng Container Session từ db.
 * 2. Cho user nhập Operator (Hãng tàu) và save Session xuống db.
 * 3. Chụp ảnh bằng Camera.
 */
@EFragment(R.layout.fragment_import)
public class ImportFragment extends Fragment {

	//region VIEWS
	@ViewById(R.id.btn_camera)
	ImageButton btnCamera;

	@ViewById(R.id.btn_pick_more)
	Button btnPickMore;

	@ViewById(R.id.btn_done)
	Button btnContinue;

	@ViewById(R.id.btn_complete_import)
	Button btnComplete;

	@ViewById(R.id.et_operator)
	EditText etOperator;

	@ViewById(R.id.rdn_group_status)
	RadioGroup rdnGroupStatus;

	@ViewById(R.id.rdn_status_a)
	RadioButton rdnStatusA;

	@ViewById(R.id.rdn_status_b)
	RadioButton rdnStatusB;

	@ViewById(R.id.rdn_status_c)
	RadioButton rdnStatusC;

	@ViewById(R.id.tv_container_id)
	EditText etContainerCode;

	@ViewById(R.id.lv_image)
	ListView lvImages;
	//endregion

	//region ATTRIBUTE
	public final static String CONTAINER_ID_EXTRA = "com.cloudjay.wizard.containerId";
	public final static String IMAGE_URLS_EXTRA = "com.cloudjay.wizard.imageurls";

	@Bean
	DataCenter dataCenter;

	@FragmentArg(CONTAINER_ID_EXTRA)
	String containerID;

	@FragmentArg(IMAGE_URLS_EXTRA)
	ArrayList<String> imageUrls;

	String operatorCode;
	long operatorId;

	GateImageAdapter mAdapter = null;

	long preStatus = 1;
	Session mSession;
	List<GateImage> list = new ArrayList<>();

	boolean rainyMode;
	//endregion

	public ImportFragment() {
	}

	/**
	 * 1. Config Action Bar
	 * 2. Initial variables
	 * 3. Restore container information
	 */
	@AfterViews
	void doAfterViews() {

		rainyMode = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext())
				.getBoolean(getString(R.string.pref_key_enable_temporary_fragment_checkbox),
						false);

		// Set ActionBar Title
		getActivity().getActionBar().setTitle(R.string.fragment_import_title);

		etOperator.setFocusable(false);
		etOperator.setCursorVisible(false);
		etContainerCode.setKeyListener(null);
		etOperator.clearFocus();

		mAdapter = new GateImageAdapter(getActivity(), R.layout.item_image_gridview, false);
		lvImages.setAdapter(mAdapter);


		if (rainyMode) {
			configViewForRainyMode();
		} else {

			btnCamera.setVisibility(View.VISIBLE);
			btnPickMore.setVisibility(View.GONE);

			dataCenter.getSessionInBackground(getActivity(), containerID);
			refresh();
		}

	}

	/**
	 * configure view for rainy mode
	 */
	private void configViewForRainyMode() {
		btnPickMore.setVisibility(View.VISIBLE);
		btnCamera.setVisibility(View.GONE);
		btnContinue.setVisibility(View.GONE);

		Utils.setupEditText(etContainerCode);

		if (imageUrls != null) {
			for (int i = 0; i < imageUrls.size(); i++) {

				String imageName = Utils.getImageNameFromUrl(imageUrls.get(i));
				String uuid = Utils.getUuidFromImageName(imageName);

				GateImage gateImage = new GateImage()
						.withId(0)
						.withType(ImageType.IMPORT.value)
						.withName(imageName)
						.withUrl(imageUrls.get(i))
						.withUuid(uuid);

				list.add(gateImage);
			}
		}

		mAdapter.setData(list);
	}

	//region EVENT HANDLER
	@UiThread
	void onEvent(OperatorChosenEvent event) {

		// Get selected operator from search operator dialog
		Operator operator = event.getOperator();
		operatorCode = operator.getOperatorCode();
		operatorId = operator.getId();
		Logger.Log(" > Choose operator " + operatorCode);

		// Set operator to edit text
		etOperator.setText(operator.getOperatorCode());

		if (!rainyMode) {

			if (mSession != null) {
				mSession.setOperatorId(operatorId);
				mSession.setOperatorCode(operator.getOperatorCode());
				mSession.setGateImages(list);
			}

			// Save session
			dataCenter.updateImportSession(mSession);
		}
	}

	/**
	 * Event được trigger khi chụp hình xong bấm nút Done ở camera.
	 * Refresh container session.
	 *
	 * @param event
	 */
	void onEvent(ImageCapturedEvent event) {
		refresh();
	}

	/**
	 * @param event
	 */
	public void onEvent(ContainerGotEvent event) {

		// Trying to restore container status
		mSession = event.getSession();
		updatedData();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EventBus.getDefault().register(this);
	}

	@Override
	public void onDestroy() {
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}

	//endregion

	void refresh() {
		dataCenter.getSessionInBackground(getActivity(), containerID);

	}

	@UiThread
	public void updatedData() {

		if (null == mSession) {

			// Set container ID for text View containerId
			etContainerCode.setText(containerID);

		} else {
			list = mSession.getImportImages();
			mAdapter.setData(list);
			containerID = mSession.getContainerId();
			operatorCode = mSession.getOperatorCode();
			etContainerCode.setText(containerID);

			etOperator.setText(operatorCode);
//			Operator operator = dataCenter.getOperator(getActivity().getApplicationContext(), operatorCode);
//			if (operator != null) {
//				etOperator.setText(operator.getOperatorName());
//			}

			preStatus = mSession.getPreStatus();
			switch ((int) preStatus) {
				case 0:
					rdnStatusA.setChecked(true);
					break;
				case 1:
					rdnStatusB.setChecked(true);
					break;
				case 2:
					rdnStatusC.setChecked(true);
					break;
			}
		}
	}

	//region VIEW INTERACTION

	/**
	 * Open Camera to take IMPORT images
	 */
	@Click(R.id.btn_camera)
	void buttonCameraClicked() {

		// Open camera activity
		Intent cameraActivityIntent = new Intent(getActivity(), CameraActivity_.class);
		cameraActivityIntent.putExtra(CameraFragment.CONTAINER_ID_EXTRA, containerID);
		cameraActivityIntent.putExtra(CameraFragment.OPERATOR_CODE_EXTRA, operatorCode);
		cameraActivityIntent.putExtra(CameraFragment.IMAGE_TYPE_EXTRA, ImageType.IMPORT.value);
		cameraActivityIntent.putExtra(CameraFragment.CURRENT_STEP_EXTRA, Step.IMPORT.value);
		startActivity(cameraActivityIntent);
	}

	/**
	 * Add container session to upload queue. Then navigate user to Audit and Repair Fragment.
	 */
	@Click(R.id.btn_done)
	void buttonContinueClicked() {

		if (mSession.isValidToUpload(Step.IMPORT) == false) {
			Utils.showCrouton(getActivity(), "Container chưa được báo cáo đầy đủ");
			return;
		}

		//Upload import session
		uploadImportSession(false);

		// Go to audit and repair fragment
		AuditAndRepairFragment fragment = new AuditAndRepairFragment_().builder().containerID(containerID)
				.tabType(1).build();
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
		transaction.replace(R.id.ll_main, fragment);
		transaction.commit();
	}

	/**
	 * Finish import fragment, close Wizard Activity and go back to Home Activity with Search Fragment tab
	 */
	@Click(R.id.btn_complete_import)
	void buttonCompletedClicked() {

		if (rainyMode) {

			if (isValidToAddSession()) {
                Logger.Log("data is valid");
                if (Utils.isContainerIdValid(etContainerCode.getText().toString())) {
                    Logger.Log("container iso is invalid");
                    showInvalidIsoContainerDialog();
                } else {
                    Logger.Log("container iso is valid");
                    saveSessionRainyMode();
                }
			} else {
				Utils.showCrouton(getActivity(), getResources().getString(
						R.string.warning_container_invalid));
			}
            return;
		}

		if (mSession.isValidToUpload(Step.IMPORT) == false) {
			Utils.showCrouton(getActivity(), "Container chưa được báo cáo đầy đủ");
			return;
		}

		//Upload import session
		uploadImportSession(true);

		// Navigate to HomeActivity
		getActivity().finish();
	}

	@Click(R.id.btn_pick_more)
	void buttonPickMoreClicked() {
		// Open Reuse Activity
		openReuseActivity();
	}

	private void openReuseActivity() {
		ArrayList<String> gateImages = new ArrayList<>();

		for (int i = 0; i < list.size(); i++) {
			gateImages.add(list.get(i).getUrl());
		}
		Intent intent = new Intent(getActivity(), ReuseActivity_.class);
		intent.setAction(CJayConstant.ACTION_PICK_MORE);
		intent.putExtra(ReuseActivity.GATE_IMAGES_EXTRA, gateImages);
		startActivity(intent);
	}

	private void uploadImportSession(boolean clearFromWorking) {

		//Remove from working
		if (clearFromWorking) {
			dataCenter.removeWorkingSession(getActivity(), mSession.getContainerId());
		}

		// Add container session to upload queue
		JobManager jobManager = App.getJobManager();
		jobManager.addJobInBackground(new UploadImportJob(mSession));
	}

	@Touch(R.id.et_operator)
	void editTextOperatorTouched(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
			FragmentManager fm = getActivity().getSupportFragmentManager();
			SearchOperatorDialog_ searchOperatorDialog = new SearchOperatorDialog_();
			searchOperatorDialog.setParent(this);
			searchOperatorDialog.show(fm, "search_operator_dialog");
		}
	}
	//endregion

	//region HANDLE PRE-STATUS

	/**
	 * Handle container pre-status
	 *
	 * @param isChecked
	 */
	@CheckedChange(R.id.rdn_status_a)
	void preStatusAChecked(boolean isChecked) {
		if (isChecked == true) {
			preStatus = 0;

			if (!rainyMode) {
				mSession.setPreStatus(preStatus);
				dataCenter.updateImportSession(mSession);
				dataCenter.addWorkingSession(mSession);

				btnContinue.setVisibility(View.GONE);
			}

		}
	}

	@CheckedChange(R.id.rdn_status_b)
	void preStatusBChecked(boolean isChecked) {
		if (isChecked == true) {
			preStatus = 1;

			if (!rainyMode) {
				mSession.setPreStatus(preStatus);
				dataCenter.updateImportSession(mSession);
				dataCenter.addWorkingSession(mSession);

				btnContinue.setVisibility(View.VISIBLE);
			}
		}
	}

	@CheckedChange(R.id.rdn_status_c)
	void preStatusCChecked(boolean isChecked) {
		if (isChecked == true) {
			preStatus = 2;

			if (!rainyMode) {
				mSession.setPreStatus(preStatus);
				dataCenter.updateImportSession(mSession);
				dataCenter.addWorkingSession(mSession);

				btnContinue.setVisibility(View.VISIBLE);
			}
		}
	}
	//endregion

	boolean isValidToAddSession() {

		containerID = etContainerCode.getText().toString();

		if (containerID.length() < 11) {
			return false;
		}

		if (etContainerCode.getText().toString().equals("")) {
			return false;
		}

		if (null == list || list.size() == 0) {
			return false;
		}

		return true;
	}

    private void saveSessionRainyMode() {

        for (GateImage gateImage : list) {
            String uri = gateImage.getUrl();
            String oldImageName = gateImage.getName();

            // update image name
            String newImageName = oldImageName.replace("containerId", etContainerCode.getText().toString());
            newImageName = newImageName.replace("imageType", "gate-in");

            // TODO: rename file in storage


            //update uri
            String newUri = uri.replace(oldImageName, newImageName);

            // set name and uri in gate image
            gateImage.setName(newImageName);
            gateImage.setUrl(newUri);
        }

        mSession = new Session()
                .withContainerId(etContainerCode.getText().toString())
                .withOperatorCode(etOperator.getText().toString())
                .withPreStatus(preStatus)
                .withGateImages(list);

        dataCenter.addSession(mSession);

        // Add image to job queue
        for (GateImage gateImage : list) {
            String uri = Utils.parseUrltoUri(gateImage.getUrl());
            String imageName = gateImage.getName();
            String containerId = mSession.getContainerId();

            Logger.Log("imageName: " + imageName);
            Logger.Log("uri: " + uri);

            JobManager jobManager = App.getJobManager();
            jobManager.addJobInBackground(new UploadImageJob(uri, imageName, containerId, ImageType.IMPORT));
        }
        //Upload session
        uploadImportSession(false);

        // open reuse activity
        openReuseActivity();
    }

    private void showInvalidIsoContainerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Alert");
        builder.setMessage("Container ID này sai chuẩn ISO. Tiếp tục?");

        builder.setPositiveButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Open camera activity to take repair image
                saveSessionRainyMode();
                dialogInterface.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {

                // Set background and text color for BUTTON_NEGATIVE
                ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(getActivity().getResources().getColor(android.R.color.white));
                ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setBackgroundResource(R.drawable.btn_green_selector);

                // Set background and text color for BUTTON_POSITIVE
                ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(getActivity().getResources().getColor(android.R.color.white));
                ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE)
                        .setBackgroundColor(getActivity().getResources().getColor(android.R.color.darker_gray));
            }
        });
        dialog.show();
    }
}
