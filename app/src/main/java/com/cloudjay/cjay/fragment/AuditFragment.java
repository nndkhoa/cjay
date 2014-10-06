package com.cloudjay.cjay.fragment;



import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.activity.ReuseActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

/**
 * Màn hình giám định
 */
@EFragment(R.layout.fragment_audit)
public class AuditFragment extends Fragment {

    //region Control_View_Declare
    @ViewById(R.id.btn_continue)
	Button btnContinue;

    @ViewById(R.id.btn_reuse_gate_in_image)
    Button btnReuseGateInImage;

    @ViewById(R.id.lv_audit_images)
    ListView lvAuditImages;
    //endregion

	public AuditFragment() {
		// Required empty public constructor
	}

    @Click(R.id.btn_continue)
    void buttonContinueClicked() {
        //Go to next fragment
        RepairFragment fragment = new RepairFragment_.builder().build();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.ll_main_process, fragment);
        transaction.commit();
    }

    @Click(R.id.btn_reuse_gate_in_image)
    void buttonReuseGateInImageClicked() {
        // Open ReuseActivity
        Intent intent = new Intent(getActivity(), ReuseActivity.class);
        getActivity().startActivity(intent);
    }

}