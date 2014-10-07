package com.cloudjay.cjay.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.cloudjay.cjay.R;

import org.androidannotations.annotations.EFragment;

/**
 * A simple {@link Fragment} subclass.
 */
@EFragment(R.layout.fragment_issue_pending)
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

}
