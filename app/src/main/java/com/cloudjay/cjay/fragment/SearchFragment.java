package com.cloudjay.cjay.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.Utils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment {

	private static final String ARG_SECTION_NUMBER = "section_number";
	View rootView;
	Button btnSearch;
	EditText etSearch;

	Pattern pattern = Pattern.compile("^[a-zA-Z]{4}");


	public SearchFragment() {
		// Required empty public constructor
	}

	public static SearchFragment newInstance(int sectionNumber) {
		SearchFragment fragment = new SearchFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, final ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		rootView = inflater.inflate(R.layout.fragment_search, container, false);
		initController();
		//Show soft key when show this fragment
		etSearch.requestFocus();
		InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

		//Set action for btnSearch
		btnSearch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String containeriD = etSearch.getText().toString();
				if (TextUtils.isEmpty(containeriD)) {
					etSearch.setError(getString(R.string.dialog_container_id_required));

				} else if (!Utils.simpleValid(containeriD)) {
					etSearch.setError(getString(R.string.dialog_container_id_invalid));
					return;
				} else {
					List<Session> result = searchSession(containeriD);
					if (result.size() != 0) {
						refreshListView();
					} else {
						showAddContainerDialog(containeriD);
					}
					etSearch.setText("");
				}

			}
		});
		//Set action change soft key when text in etSearch
		etSearch.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				if (s.length() == 0) {
					etSearch.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
				}

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				Matcher matcher = pattern.matcher(s);
				if (s.length() < 4) {
					if (etSearch.getInputType() != InputType.TYPE_CLASS_TEXT) {
						etSearch.setInputType(InputType.TYPE_CLASS_TEXT
								| InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
					}

				} else if (matcher.matches()) {

					if (etSearch.getInputType() != InputType.TYPE_CLASS_NUMBER) {
						etSearch.setInputType(InputType.TYPE_CLASS_NUMBER
								| InputType.TYPE_NUMBER_VARIATION_NORMAL);
					}
				}

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		return rootView;
	}

	//TODO refresh list view after search
	private void refreshListView() {
	}

	//TODO add logic search
	private List<Session> searchSession(String containeriD) {
		return null;
	}

	private void initController() {
		etSearch = (EditText) rootView.findViewById(R.id.et_search);
		btnSearch = (Button) rootView.findViewById(R.id.btn_search);
	}


	private void showAddContainerDialog(String containerID) {
		FragmentManager fragmentManager = getChildFragmentManager();
		AddContainerDialog addContainerDialog = new AddContainerDialog(getActivity(), containerID);
		addContainerDialog.show(fragmentManager, "fragment_addcontainer");
	}
}
