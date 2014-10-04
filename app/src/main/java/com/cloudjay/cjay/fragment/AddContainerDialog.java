package com.cloudjay.cjay.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.activity.WizardActivity;
import com.cloudjay.cjay.util.Utils;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

@EFragment
public class AddContainerDialog extends android.support.v4.app.DialogFragment {

	@FragmentArg("containerID")
	String containerID;

	@ViewById(R.id.tv_containterIDSearch)
	TextView tvContainerIDSearch;
	@ViewById(R.id.tv_searchresult)
	TextView tvSearchResult;
	@ViewById(R.id.btn_addContainer)
	Button btnAddContainer;
	@ViewById(R.id.btn_cancelAddContainer)
	Button btnCancelAddContainer;

	public AddContainerDialog() {}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_addcontainer, container, false);

		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		initController(rootView);
		tvContainerIDSearch.setText(containerID);

		if (Utils.isContainerIdValid(containerID)) {
			btnAddContainer.setText(R.string.dialog_create_container_id_invalid_iso);
		}

		btnAddContainer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent wizardActivityIntent = new Intent(getActivity(), WizardActivity.class);
				wizardActivityIntent.putExtra("containerID", containerID);
				startActivity(wizardActivityIntent);
				getDialog().dismiss();
			}
		});
		btnCancelAddContainer = (Button) rootView.findViewById(R.id.btn_cancelAddContainer);
		btnCancelAddContainer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getDialog().dismiss();
			}
		});

		return rootView;
	}

	private void initController(View rootView) {
		tvContainerIDSearch = (TextView) rootView.findViewById(R.id.tv_containterIDSearch);
		btnAddContainer = (Button) rootView.findViewById(R.id.btn_addContainer);
	}

}
