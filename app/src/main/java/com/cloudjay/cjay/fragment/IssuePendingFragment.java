package com.cloudjay.cjay.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cloudjay.cjay.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class IssuePendingFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

	public IssuePendingFragment() {
		// Required empty public constructor
	}

    public static IssuePendingFragment newInstance(int sectionNumber) {
        IssuePendingFragment fragment = new IssuePendingFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_issue_pending, container, false);
	}

}
