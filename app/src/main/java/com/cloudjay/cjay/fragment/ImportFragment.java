package com.cloudjay.cjay.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.activity.CameraActivity;
import com.cloudjay.cjay.adapter.OperatorAdapter;
import com.cloudjay.cjay.event.OperatorsGotEvent;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ItemSelect;
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

	@FragmentArg("containerID")
	String containerID;
    String operatorCode;

	@Bean
	DataCenter dataCenter;

	OperatorAdapter operatorAdapter;

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
	public void onEvent(OperatorsGotEvent event) {

		// retrieve list operators
		RealmResults<Operator> operators = event.getOperators();

		// Init and set adapter
		operatorAdapter = new OperatorAdapter(getActivity(),
				android.R.layout.simple_spinner_dropdown_item, operators);
		//spOperator.setAdapter(operatorAdapter);
	}

	@AfterViews
	void doAfterViews() {

		// Set container ID for text View containerID
		tvContainerCode.setText(containerID);

		// Begin to get operators from cache
		dataCenter.getOperators();
	}

    @Click(R.id.btn_camera)
    void buttonCameraClicked() {
        Intent cameraActivityIntent = new Intent(getActivity(), CameraActivity.class);
        cameraActivityIntent.putExtra("containerID", containerID);
        cameraActivityIntent.putExtra("imageType", CJayConstant.TYPE_IMPORT);
        cameraActivityIntent.putExtra("operatorCode", operatorCode);
        startActivity(cameraActivityIntent);
    }

    @Click(R.id.btn_continue)
    void buttonContinueClicked() {
        //Go to next fragment
        AuditFragment fragment = new AuditFragment_().builder().build();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.ll_main_process, fragment);
        transaction.commit();
    }

    @Touch(R.id.et_operator)
    void editTextOperatorTouched() {

    }

    private void showDialogSearchOperator(int mode) {
        FragmentManager fm = getActivity().getFragmentManager();
        SearchOperatorDialog searchOperatorDialog = new SearchOperatorDialog();
        searchOperatorDialog.show(fm, null);
    }

    /*@ItemSelect(R.id.sp_operator)
    void spinnerOperatorsItemClicked(boolean selected, Operator selectedOperator) {
        operatorCode = selectedOperator.getOperatorCode();
        long operatorId = selectedOperator.getId();

        if (!TextUtils.isEmpty(tvContainerCode.getText()) && !TextUtils.isEmpty(operatorCode)) {
            dataCenter.addSession(containerID, operatorCode, operatorId);
        }

    }*/
}
