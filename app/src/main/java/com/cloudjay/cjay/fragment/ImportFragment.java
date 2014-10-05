package com.cloudjay.cjay.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.activity.CameraActivity;
import com.cloudjay.cjay.event.OperatorsGotEvent;
import com.cloudjay.cjay.model.Operator;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import de.greenrobot.event.EventBus;
import io.realm.RealmResults;

/**
 * A simple {@link Fragment} subclass.
 * Fragment in MainProcessActivity
 * =======
 * <p/>
 * /**
 * Màn hình nhập
 */
@EFragment(R.layout.fragment_import)
public class ImportFragment extends Fragment {

    //region Declare Controls and Views
    @ViewById(R.id.btn_camera)
	Button btnCamera;
    @ViewById(R.id.btn_continue)
    Button btnContinue;
    @ViewById(R.id.btn_complete)
    Button btnComplete;
    @ViewById(R.id.sp_operator)
    Spinner spOperator;
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

    //Declare adapter operator
    ArrayAdapter<Operator> operatorAdapter;

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

    public void onEvent(OperatorsGotEvent event) {
        // retrieve list operators
        RealmResults<Operator> operators = event.getOperators();
        // Init and set adapter
        operatorAdapter.addAll(operators);
        spOperator.setAdapter(operatorAdapter);
    }

    @AfterViews
	void doAfterViews() {
		//Set container ID for text View containerID
		tvContainerCode.setText(containerID);
        //Query operators from database

        //TODO: Set adapter
	}

    @Click(R.id.btn_camera)
    void buttonCameraClicked() {
        Intent cameraActivityIntent = new Intent(getActivity(), CameraActivity.class);
        cameraActivityIntent.putExtra("containerID", containerID);
        startActivity(cameraActivityIntent);
    }

    @Click(R.id.btn_continue)
    void buttonContinueClicked() {
        //Go to next fragment
        AuditFragment fragment = new AuditFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.ll_main_process, fragment);
        transaction.commit();
    }
}
