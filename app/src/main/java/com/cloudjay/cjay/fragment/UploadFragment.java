package com.cloudjay.cjay.fragment;



import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cloudjay.cjay.R;

/**
 * A simple {@link Fragment} subclass.
 *
 */
public class UploadFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    public UploadFragment() {
        // Required empty public constructor
    }

    public static UploadFragment newInstance(int sectionNumber) {
        UploadFragment fragment = new UploadFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
	    setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_upload, container, false);
    }

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.actionbar_upload, menu);
		super.onCreateOptionsMenu(menu, inflater);

	}


}
