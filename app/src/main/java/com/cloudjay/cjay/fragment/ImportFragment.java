package com.cloudjay.cjay.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
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
import com.cloudjay.cjay.activity.CameraActivity;
import com.cloudjay.cjay.adapter.GateImageAdapter;
import com.cloudjay.cjay.event.GateImagesGotEvent;
import com.cloudjay.cjay.event.ImageCapturedEvent;
import com.cloudjay.cjay.event.OperatorCallbackEvent;
import com.cloudjay.cjay.fragment.dialog.SearchOperatorDialog_;
import com.cloudjay.cjay.model.GateImage;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.jobqueue.UploadSessionJob;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.StringHelper;
import com.cloudjay.cjay.util.Utils;
import com.cloudjay.cjay.util.enums.Step;
import com.path.android.jobqueue.JobManager;
import com.snappydb.SnappydbException;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.CheckedChange;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.Touch;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

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

	//region Controls and Views
	@ViewById(R.id.btn_camera)
	ImageButton btnCamera;

	@ViewById(R.id.btn_continue)
	Button btnContinue;

	@ViewById(R.id.btn_complete)
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
	TextView tvContainerCode;

	@ViewById(R.id.lv_image)
	ListView lvImages;
	//endregion

	//region ATTRIBUTE
	public final static String CONTAINER_ID_EXTRA = "com.cloudjay.wizard.containerID";

	@Bean
	DataCenter dataCenter;

	@FragmentArg(CONTAINER_ID_EXTRA)
	String containerID;

	String operatorCode;

	GateImageAdapter gateImageAdapter = null;
	List<GateImage> gateImages = null;

	long preStatus = 0;
	Session currentSession;
	//endregion

	public ImportFragment() {
	}

	@AfterViews
	void doAfterViews() {

		// Set ActionBar Title
		getActivity().getActionBar().setTitle(R.string.fragment_import_title);

		// Trying to restore container status
		Session tmp = dataCenter.getSession(getActivity().getApplicationContext(), containerID);
		if (null == tmp) {

			// Set container ID for text View containerID
			tvContainerCode.setText(containerID);
		} else {

			containerID = tmp.getContainerId();
			operatorCode = tmp.getOperatorCode();

			tvContainerCode.setText(containerID);
			etOperator.setText(operatorCode);
		}
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

	@UiThread
	void onEvent(OperatorCallbackEvent event) {

		// Get selected operator from search operator dialog
		Operator operator = event.getOperator();
		operatorCode = operator.getOperatorCode();

		// Set operator to edit text
		etOperator.setText(operator.getOperatorName());

		// Add new session to database
		String currentTime = StringHelper.getCurrentTimestamp(CJayConstant.DAY_FORMAT);

		currentSession = new Session().withContainerId(containerID)
				.withOperatorCode(operatorCode)
				.withOperatorId(operator.getId())
				.withPreStatus(preStatus)
				.withStep(Step.IMPORT.value)
				.withCheckInTime(currentTime);

		dataCenter.addSession(currentSession);
	}

	@UiThread
	void onEvent(ImageCapturedEvent event) {
		try {
			dataCenter.getGateImages(CJayConstant.TYPE_IMPORT, event.getContainerId());
		} catch (SnappydbException e) {
			e.printStackTrace();
		}
	}

	@UiThread
	void onEvent(GateImagesGotEvent event) {

		// Get gate image objects from event post back
		gateImages = event.getGateImages();
		Logger.Log("count gate images: " + gateImages.size());

		// Init adapter if adapter is null and set adapter for list view
		if (gateImageAdapter == null) {
			Logger.Log("gateImageAdapter is null");
			gateImageAdapter = new GateImageAdapter(getActivity(), gateImages, false);
			lvImages.setAdapter(gateImageAdapter);
		}

		// Notify change
		gateImageAdapter.swapData(gateImages);

	}

	/**
	 * Open Camera to take IMPORT images
	 */
	@Click(R.id.btn_camera)
	void buttonCameraClicked() {

		if (!TextUtils.isEmpty(tvContainerCode.getText()) && !TextUtils.isEmpty(etOperator.getText())) {

			// Open camera activity
			Intent cameraActivityIntent = new Intent(getActivity(), CameraActivity.class);
			cameraActivityIntent.putExtra(CameraFragment.CONTAINER_ID_EXTRA, containerID);
			cameraActivityIntent.putExtra(CameraFragment.OPERATOR_CODE_EXTRA, operatorCode);
			cameraActivityIntent.putExtra(CameraFragment.IMAGE_TYPE_EXTRA, CJayConstant.TYPE_IMPORT);
			cameraActivityIntent.putExtra(CameraFragment.CURRENT_STEP_EXTRA, Step.IMPORT.value);
			startActivity(cameraActivityIntent);

		} else {

			// Alert: require select operator first
			Utils.showCrouton(getActivity(), R.string.require_select_operator_first);
		}
	}

	/**
	 *
	 */
	@Click(R.id.btn_continue)
	void buttonContinueClicked() {
        //Upload container
        JobManager jobManager = App.getJobManager();
        jobManager.addJobInBackground(new UploadSessionJob(currentSession));
        //TODO add gate image to current session
		// Go to next fragment
		AuditAndRepairFragment fragment = new AuditAndRepairFragment_().builder().containerID(containerID).build();
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
		transaction.replace(R.id.ll_main, fragment);
		transaction.commit();
	}

	@Click(R.id.btn_complete)
	void buttonCompletedClicked() {
		// Finish import fragment, close Wizzard Activity and go back to Home Activity with Search Fragment tab

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
		}
	}

	@CheckedChange(R.id.rdn_status_b)
	void preStatusBChecked(boolean isChecked) {
		if (isChecked == true) {
			preStatus = 1;
		}
	}

	@CheckedChange(R.id.rdn_status_c)
	void preStatusCChecked(boolean isChecked) {
		if (isChecked == true) {
			preStatus = 2;
		}
	}
	//endregion
}
