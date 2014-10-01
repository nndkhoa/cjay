package com.cloudjay.cjay.fragment;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.cloudjay.cjay.R;

import butterknife.InjectView;

/**
 * Created by Thai on 9/30/2014.
 */

public class SearchFragment extends android.support.v4.app.Fragment {

	@InjectView(R.id.btn_search)
	Button btnSearch;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_search, container, false);
		btnSearch = (Button) rootView.findViewById(R.id.btn_search);
		btnSearch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showAddContainerDialog();
			}
		});

		return rootView;
	}


	private void showAddContainerDialog() {
		FragmentManager fragmentManager = getChildFragmentManager();
		AddContainerDialog addContainerDialog = new AddContainerDialog(getActivity());
		addContainerDialog.show(fragmentManager, "fragment_addcontainer");
	}


}
