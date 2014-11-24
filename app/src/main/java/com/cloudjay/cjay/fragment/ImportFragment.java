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

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.activity.CameraActivity_;
import com.cloudjay.cjay.activity.ReuseActivity;
import com.cloudjay.cjay.activity.ReuseActivity_;
import com.cloudjay.cjay.adapter.GateImageAdapter;
import com.cloudjay.cjay.event.image.RainyImagesDeletedEvent;
import com.cloudjay.cjay.event.image.RainyImagesGotEvent;
import com.cloudjay.cjay.event.operator.OperatorChosenEvent;
import com.cloudjay.cjay.event.session.ContainerGotEvent;
import com.cloudjay.cjay.event.session.ContainerSearchedEvent;
import com.cloudjay.cjay.fragment.dialog.SearchOperatorDialog_;
import com.cloudjay.cjay.model.GateImage;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.command.image.DeleteRainyImageCommand;
import com.cloudjay.cjay.task.command.session.get.GetSessionCommand;
import com.cloudjay.cjay.task.command.session.remove.RemoveWorkingSessionCommand;
import com.cloudjay.cjay.task.command.session.update.AddUploadingSessionCommand;
import com.cloudjay.cjay.task.command.session.update.AddWorkingSessionCommand;
import com.cloudjay.cjay.task.command.session.get.SearchCommand;
import com.cloudjay.cjay.task.command.session.update.SaveSessionCommand;
import com.cloudjay.cjay.task.job.UploadImageJob;
import com.cloudjay.cjay.task.job.UploadSessionJob;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.PreferencesUtil;
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

import java.io.File;
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

        updateList();
	}

	//region EVENT HANDLER
	@UiThread
	void onEvent(OperatorChosenEvent event) {

		// Get selected operator from search operator dialog
		Operator operator = event.getOperator();
		operatorCode = operator.getOperatorCode();
		operatorId = operator.getId();

		// Set operator to edit text
		etOperator.setText(operator.getOperatorCode());

        if (mSession != null) {
            mSession.setOperatorId(operatorId);
            mSession.setOperatorCode(operator.getOperatorCode());
            mSession.setGateImages(list);
        }

		// Save session
        if (!rainyMode) {
	        dataCenter.add(new SaveSessionCommand(getActivity(), mSession));
		}
	}

    @Override
    public void onResume() {
        super.onResume();
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
		dataCenter.add(new GetSessionCommand(getActivity(), containerID));
	}

	@UiThread
	public void updatedData() {

		if (null == mSession) {
			etContainerCode.setText(containerID);

		} else {
			list = mSession.getImportImages();
			mAdapter.setData(list);
			containerID = mSession.getContainerId();
			operatorCode = mSession.getOperatorCode();
			etContainerCode.setText(containerID);
			etOperator.setText(operatorCode);

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
		cameraActivityIntent.putExtra(CameraActivity_.CONTAINER_ID_EXTRA, containerID);
		cameraActivityIntent.putExtra(CameraActivity_.OPERATOR_CODE_EXTRA, operatorCode);
		cameraActivityIntent.putExtra(CameraActivity_.IMAGE_TYPE_EXTRA, ImageType.IMPORT.value);
		cameraActivityIntent.putExtra(CameraActivity_.CURRENT_STEP_EXTRA, Step.IMPORT.value);
		startActivity(cameraActivityIntent);
	}

	/**
	 * Add container session to upload queue. Then navigate user to Audit and Repair Fragment.
	 */
	@Click(R.id.btn_done)
	void buttonContinueClicked() {

		if (mSession.isValidToUpload(Step.IMPORT) == false) {
			Utils.showCrouton(getActivity(), getResources().getString(R.string.warning_container_invalid));
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

        containerID = etContainerCode.getText().toString();

		if (rainyMode) {
            performSearch(containerID);
            return;
		}

		if (mSession.isValidToUpload(Step.IMPORT) == false) {
			Utils.showCrouton(getActivity(), getResources().getString(R.string.warning_container_invalid));
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
		Intent intent = new Intent(getActivity(), ReuseActivity_.class);
		intent.setAction(CJayConstant.ACTION_PICK_MORE);
        intent.putExtra(ReuseActivity.CHECKED_IMAGES, imageUrls);
		startActivity(intent);
	}

	private void uploadImportSession(boolean clearFromWorking) {

		// Add to Uploading
		dataCenter.add(new AddUploadingSessionCommand(getActivity(), mSession));

		//Remove from working
		if (clearFromWorking) {
			dataCenter.add(new RemoveWorkingSessionCommand(getActivity(), containerID));
		}

		mSession.prepareForUploading();
		dataCenter.add(new SaveSessionCommand(getActivity(), mSession));

		// Add container session to upload queue
		JobManager jobManager = App.getJobManager();
		jobManager.addJobInBackground(new UploadSessionJob(mSession));
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
				dataCenter.add(new SaveSessionCommand(getActivity(), mSession));
				dataCenter.add(new AddWorkingSessionCommand(getActivity(), mSession));

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
				dataCenter.add(new SaveSessionCommand(getActivity(), mSession));
				dataCenter.add(new AddWorkingSessionCommand(getActivity(), mSession));

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
				dataCenter.add(new SaveSessionCommand(getActivity(), mSession));
				dataCenter.add(new AddWorkingSessionCommand(getActivity(), mSession));

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

        // get depot code
        String depotCode = PreferencesUtil.getPrefsValue(getActivity(), PreferencesUtil.PREF_USER_DEPOT);

        for (GateImage gateImage : list) {
            String uri = gateImage.getUrl();
            String imageName = gateImage.getName();

            // update image name
            String newImageName = imageName.replace("containerId", etContainerCode.getText().toString());
            newImageName = newImageName.replace("imageType", "gate-in");

            // Get image file from storage
            File directory = new File(CJayConstant.APP_DIRECTORY_FILE, depotCode + "/rainy_mode" );
            File file = new File(directory, imageName);

            // Create new image file
            File newFile = new File(directory, newImageName);

            // Rename image
            file.renameTo(newFile);

            //update uri
            String newUri = uri.replace(imageName, newImageName);

            // set name and uri in gate image
            gateImage.setName(newImageName);
            gateImage.setUrl(newUri);
        }

        mSession = new Session()
                .withContainerId(etContainerCode.getText().toString())
                .withOperatorCode(etOperator.getText().toString())
                .withOperatorId(operatorId)
                .withPreStatus(preStatus)
                .withGateImages(list)
                .withLocalStep(Step.IMPORT.value);

	    dataCenter.add(new SaveSessionCommand(getActivity(), mSession));

        // Add image to job queue
        for (GateImage gateImage : list) {
            String uri = Utils.parseUrltoUri(gateImage.getUrl());
            String imageName = gateImage.getName();
            String containerId = mSession.getContainerId();

            JobManager jobManager = App.getJobManager();
            jobManager.addJobInBackground(new UploadImageJob(uri, imageName, containerId, ImageType.IMPORT));
        }
        //Upload session
        uploadImportSession(false);

        // Delete selected image
        dataCenter.add(new DeleteRainyImageCommand(
		        getActivity().getApplicationContext(), imageUrls));
    }

    @UiThread
    void onEvent(RainyImagesDeletedEvent event) {
        // open reuse activity
        Intent intent = new Intent(getActivity(), ReuseActivity_.class);
        startActivity(intent);
        getActivity().finish();
    }

    @UiThread
    void onEvent (RainyImagesGotEvent event) {
        imageUrls = event.getImageUrls();
        updateList();
    }

    void updateList() {

        list.clear();
        mAdapter.clear();

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

    private void showInvalidIsoContainerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Alert");
        builder.setMessage(getResources().getString(R.string.dialog_container_id_invalid_iso));

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
                        .setBackgroundResource(R.drawable.btn_red_selector);

                // Set background and text color for BUTTON_POSITIVE
                ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(getActivity().getResources().getColor(android.R.color.white));
                ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE)
                        .setBackgroundColor(getActivity().getResources().getColor(android.R.color.darker_gray));
            }
        });
        dialog.show();
    }

    private void performSearch(String containerId) {
//        dataCenter.search(getActivity(), containerId, true);
	    dataCenter.add(new SearchCommand(getActivity(), containerId, true));
    }

    @UiThread
    public void onEvent(ContainerSearchedEvent event) {

        boolean searchInImportFragment = event.isSearchInImportFragment();

        if (searchInImportFragment) {
            if (rainyMode) {
                List<Session> result = event.getSessions();
                if (result.size() == 0) {
                    if (isValidToAddSession()) {
                        if (!Utils.isContainerIdValid(containerID)) {
                            showInvalidIsoContainerDialog();
                        } else {
                            saveSessionRainyMode();
                        }
                    } else {
                        Utils.showCrouton(getActivity(), getResources().getString(
                                R.string.warning_container_invalid));
                    }
                } else {
                    Utils.showCrouton(getActivity(), getResources().getString(
                            R.string.error_container_is_already_existed));
                }
            }
        }
    }
}
