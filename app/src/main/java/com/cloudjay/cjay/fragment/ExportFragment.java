package com.cloudjay.cjay.fragment;


import android.support.v4.app.Fragment;

import com.cloudjay.cjay.R;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;

/**
 * Màn hình xuất
 */
@EFragment(R.layout.fragment_export)
public class ExportFragment extends Fragment {

	public final static String CONTAINER_ID_EXTRA = "com.cloudjay.wizard.containerID";

	@FragmentArg(CONTAINER_ID_EXTRA)
	String containerID;

	public ExportFragment() {
	}

}
