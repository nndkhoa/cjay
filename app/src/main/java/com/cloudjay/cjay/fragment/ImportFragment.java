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
import com.cloudjay.cjay.activity.CameraActivity_;
import com.cloudjay.cjay.activity.HomeActivity_;
import com.cloudjay.cjay.adapter.GateImageAdapter;
import com.cloudjay.cjay.event.ImageCapturedEvent;
import com.cloudjay.cjay.event.OperatorCallbackEvent;
import com.cloudjay.cjay.fragment.dialog.SearchOperatorDialog_;
import com.cloudjay.cjay.model.GateImage;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.jobqueue.UploadSessionJob;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.StringUtils;
import com.cloudjay.cjay.util.Utils;
import com.cloudjay.cjay.util.enums.ImageType;
import com.cloudjay.cjay.util.enums.Step;
import com.path.android.jobqueue.JobManager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
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

	@ViewById(R.id.btn_complete_repair)
	Button btnContinue;

	@ViewById(R.id.btn_complete_audit)
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

	GateImageAdapter mAdapter = null;

	long preStatus = 1;
	Session mSession;
	//endregion

	public ImportFragment() {
	}

	@AfterViews
	void doAfterViews() {

		// Set ActionBar Title
		getActivity().getActionBar().setTitle(R.string.fragment_import_title);

		etOperator.setFocusable(false);
		etOperator.setCursorVisible(false);
		etOperator.clearFocus();

		mAdapter = new GateImageAdapter(getActivity(), R.layout.item_image_gridview, false);
		lvImages.setAdapter(mAdapter);


		// Trying to restore container status
		mSession = dataCenter.getSession(getActivity().getApplicationContext(), containerID);
		if (null == mSession) {

			// Set container ID for text View containerID
			tvContainerCode.setText(containerID);

		} else {

			containerID = mSession.getContainerId();
			operatorCode = mSession.getOperatorCode();

			tvContainerCode.setText(containerID);
			etOperator.setText(operatorCode);

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

			refresh();
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
		String currentTime = StringUtils.getCurrentTimestamp(CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE);

		mSession = new Session().withContainerId(containerID)
				.withOperatorCode(operatorCode)
				.withOperatorId(operator.getId())
				.withPreStatus(preStatus)
				.withStep(Step.IMPORT.value)
				.withCheckInTime(currentTime);

		// Save normal session and working session.
		// add working session also post an event
		dataCenter.addSession(mSession);
		dataCenter.addWorkingSession(mSession);

	}

	/**
	 * Event được trigger khi chụp hình xong bấm nút Done ở camera.
	 * Refresh container session.
	 *
	 * @param event
	 */
	@UiThread
	void onEvent(ImageCapturedEvent event) {

		Logger.Log("onEvent Image Captured");

		// Re-query container session with given containerId
		String containerId = event.getContainerId();
		mSession = dataCenter.getSession(getActivity().getApplicationContext(), containerId);
		refresh();
	}

	@Background
	void refresh() {

		if (mSession != null) {
			List<GateImage> list = mSession.getImportImages();
			updatedData(list);
		}
	}

	@UiThread
	public void updatedData(List<GateImage> gateImageList) {

		mAdapter.clear();
		if (gateImageList != null) {
			for (GateImage object : gateImageList) {
				mAdapter.add(object);
			}
		}

		mAdapter.notifyDataSetChanged();
	}

	//region VIEW INTERACTION

	/**
	 * Open Camera to take IMPORT images
	 */
	@Click(R.id.btn_camera)
	void buttonCameraClicked() {

		if (!TextUtils.isEmpty(tvContainerCode.getText()) && !TextUtils.isEmpty(etOperator.getText())) {

			// Open camera activity
			Intent cameraActivityIntent = new Intent(getActivity(), CameraActivity_.class);
			cameraActivityIntent.putExtra(CameraFragment.CONTAINER_ID_EXTRA, containerID);
			cameraActivityIntent.putExtra(CameraFragment.OPERATOR_CODE_EXTRA, operatorCode);
			cameraActivityIntent.putExtra(CameraFragment.IMAGE_TYPE_EXTRA, ImageType.IMPORT);
			cameraActivityIntent.putExtra(CameraFragment.CURRENT_STEP_EXTRA, Step.IMPORT.value);
			startActivity(cameraActivityIntent);

		} else {

			// Alert: require select operator first
			Utils.showCrouton(getActivity(), R.string.require_select_operator_first);
		}
	}

	/**
	 * Add container session to upload queue. Then navigate user to Audit and Repair Fragment.
	 */
	@Click(R.id.btn_complete_repair)
	void buttonContinueClicked() {

		if (mSession.isValidToUpload(Step.IMPORT) == false) {
			Utils.showCrouton(getActivity(), "Container chưa được báo cáo đầy đủ");
			return;
		}

		// Add current container to job queue
		JobManager jobManager = App.getJobManager();
		jobManager.addJobInBackground(new UploadSessionJob(mSession));

		// Go to next fragment
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
	@Click(R.id.btn_complete_audit)
	void buttonCompletedClicked() {

		if (mSession.isValidToUpload(Step.IMPORT) == false) {
			Utils.showCrouton(getActivity(), "Container chưa được báo cáo đầy đủ");
			return;
		}

		// Add container session to upload queue
		JobManager jobManager = App.getJobManager();
		jobManager.addJobInBackground(new UploadSessionJob(mSession));

		// Navigate to HomeActivity
//		Intent intent = new Intent(getActivity().getApplicationContext(), HomeActivity_.class);
//		startActivity(intent);
//		getActivity().finish();
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
			btnContinue.setVisibility(View.GONE);
		}
	}

	@CheckedChange(R.id.rdn_status_b)
	void preStatusBChecked(boolean isChecked) {
		if (isChecked == true) {
			preStatus = 1;
			btnContinue.setVisibility(View.VISIBLE);
		}
	}

	@CheckedChange(R.id.rdn_status_c)
	void preStatusCChecked(boolean isChecked) {
		if (isChecked == true) {
			preStatus = 2;
			btnContinue.setVisibility(View.VISIBLE);
		}
	}
	//endregion
}
