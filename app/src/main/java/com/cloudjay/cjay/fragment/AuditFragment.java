package com.cloudjay.cjay.fragment;


import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.widget.Button;
import android.widget.ListView;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.activity.ReuseActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

/**
 * Màn hình giám định
 */
@EFragment(R.layout.fragment_audit)
public class AuditFragment extends Fragment {

	public final static String CONTAINER_ID_EXTRA = "com.cloudjay.wizard.containerID";

	@FragmentArg(CONTAINER_ID_EXTRA)
	String containerID;

	//region VIEW
	@ViewById(R.id.btn_continue)
	Button btnContinue;

	@ViewById(R.id.btn_reuse_gate_in_image)
	Button btnReuseGateInImage;

	@ViewById(R.id.lv_audit_images)
	ListView lvAuditImages;
	//endregion

	public AuditFragment() {
	}

	@Click(R.id.btn_continue)
	void buttonContinueClicked() {
		//Go to next fragment
		RepairFragment fragment = new RepairFragment_().builder().build();
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.setCustomAnimations(android.R.anim.slide_out_right, android.R.anim.slide_in_left);
		transaction.replace(R.id.ll_main, fragment);
		transaction.commit();
	}

	@Click(R.id.btn_reuse_gate_in_image)
	void buttonReuseGateInImageClicked() {
		// Open ReuseActivity
		Intent intent = new Intent(getActivity(), ReuseActivity.class);
		getActivity().startActivity(intent);
	}

    @AfterViews
    void setUp() {

        // Set ActionBar Title
        getActivity().getActionBar().setTitle(R.string.fragment_audit_title);
    }
}