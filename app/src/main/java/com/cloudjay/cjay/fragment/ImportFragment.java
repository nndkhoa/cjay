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
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.activity.CameraActivity;
import com.cloudjay.cjay.adapter.GateImageAdapter;
import com.cloudjay.cjay.adapter.OperatorAdapter;
import com.cloudjay.cjay.event.GateImagesGotEvent;
import com.cloudjay.cjay.event.ImageCapturedEvent;
import com.cloudjay.cjay.event.OperatorCallbackEvent;
import com.cloudjay.cjay.model.GateImage;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.Utils;
import com.cloudjay.cjay.fragment.dialog.SearchOperatorDialog_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.Touch;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import de.greenrobot.event.EventBus;
import io.realm.RealmResults;

/**
 * Màn hình nhập
 */

@EFragment(R.layout.fragment_import)
public class ImportFragment extends Fragment {

	//region Controls and Views
	@ViewById(R.id.btn_camera)
	Button btnCamera;

	@ViewById(R.id.btn_continue)
	Button btnContinue;

	@ViewById(R.id.btn_complete)
	Button btnComplete;

	/*@ViewById(R.id.sp_operator)
	Spinner spOperator;*/

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

	@Bean
	DataCenter dataCenter;

	@FragmentArg("containerID")
	String containerID;
    GateImageAdapter gateImageAdapter = null;
    Operator selectedOperator;
    RealmResults<GateImage> gateImages = null;

	public ImportFragment() {
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
		selectedOperator = event.getOperator();

		// Set operator to edit text
		etOperator.setText(selectedOperator.getOperatorName());

		//Save session with containerId, operatorId and operatorCode into realm
		dataCenter.addSession(containerID, selectedOperator.getOperatorCode(), selectedOperator.getId());
	}

    @UiThread
    void onEvent(ImageCapturedEvent event) {
        // Get gate images from realm
        dataCenter.getGateImages(CJayConstant.TYPE_IMPORT, containerID);
    }

    @UiThread
    void onEvent(GateImagesGotEvent event) {

        // Get gate image objects from event post back
        gateImages = event.getGateImages();
        Logger.Log("count gate images: " + gateImages.size());

        //Init adapter if null and set adapter for listview
        if (gateImageAdapter == null) {
            Logger.Log("gateImageAdapter is null");
            gateImageAdapter = new GateImageAdapter(getActivity(), gateImages);
            lvImages.setAdapter(gateImageAdapter);
        }

        gateImageAdapter.notifyDataSetChanged();

    }

	@AfterViews
	void doAfterViews() {

		// Set container ID for text View containerID
		tvContainerCode.setText(containerID);
	}

	@Click(R.id.btn_camera)
	void buttonCameraClicked() {

		if (!TextUtils.isEmpty(tvContainerCode.getText()) && !TextUtils.isEmpty(etOperator.getText())) {
			// Open camera activity
			Intent cameraActivityIntent = new Intent(getActivity(), CameraActivity.class);
			cameraActivityIntent.putExtra("containerID", containerID);
			cameraActivityIntent.putExtra("imageType", CJayConstant.TYPE_IMPORT);
			cameraActivityIntent.putExtra("operatorCode", selectedOperator.getOperatorCode());
			startActivity(cameraActivityIntent);
		} else {
			// Alert: require select operator first
			Utils.showCrouton(getActivity(), R.string.require_select_operator_first);
		}
	}

	@Click(R.id.btn_continue)
	void buttonContinueClicked() {
        // Get selected radio type


		//Go to next fragment
		AuditFragment fragment = new AuditFragment_().builder().build();
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.replace(R.id.ll_main_process, fragment);
		transaction.commit();
	}

	@Touch(R.id.et_operator)
	void editTextOperatorTouched(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
			startSearchOperator();
		}
	}

	private void showDialogSearchOperator() {
		FragmentManager fm = getActivity().getSupportFragmentManager();
		SearchOperatorDialog_ searchOperatorDialog = new SearchOperatorDialog_();
		searchOperatorDialog.setParent(this);
		searchOperatorDialog.show(fm, "search_operator_dialog");
	}

	private void startSearchOperator() {
		// mContainerId = mContainerEditText.getText().toString();
		showDialogSearchOperator();
	}
}
