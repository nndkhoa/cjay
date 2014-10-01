package com.cloudjay.cjay.fragment;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cloudjay.cjay.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class IssuePendingFragment extends Fragment {


	public IssuePendingFragment() {
		// Required empty public constructor
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_list_errors, container, false);
	}

}
