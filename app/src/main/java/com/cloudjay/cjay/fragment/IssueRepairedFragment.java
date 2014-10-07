package com.cloudjay.cjay.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.cloudjay.cjay.R;

import org.androidannotations.annotations.EFragment;

/**
 * A simple {@link Fragment} subclass.
 */
@EFragment(R.layout.fragment_issue_repaired)
public class IssueRepairedFragment extends Fragment {

	private static final String ARG_SECTION_NUMBER = "section_number";

	public IssueRepairedFragment() {
		// Required empty public constructor
	}

	public static IssueRepairedFragment newInstance(int sectionNumber) {
		IssueRepairedFragment fragment = new IssueRepairedFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}

}
