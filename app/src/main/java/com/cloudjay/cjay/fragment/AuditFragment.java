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
public class AuditFragment extends Fragment {

	Button btnContinue;
    Button btnReuseGateInImage;
    ListView lvAuditImages;

	public AuditFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_audit, container, false);

        btnContinue = (Button) v.findViewById(R.id.btn_continue);
        btnReuseGateInImage = (Button) v.findViewById(R.id.btn_reuse_gate_in_image);
        lvAuditImages = (ListView) v.findViewById(R.id.lv_audit_images);

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doContinueClick();
            }
        });
        btnReuseGateInImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doReuseGateInImageClick();
            }
        });

        return v;
	}

    void doContinueClick() {
        //Go to next fragment
        RepairFragment fragment = new RepairFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.ll_main_process, fragment);
        transaction.commit();
    }

    void doReuseGateInImageClick() {
        // Open ReuseActivity
        Intent intent = new Intent(getActivity(), ReuseActivity.class);
        getActivity().startActivity(intent);
    }

}