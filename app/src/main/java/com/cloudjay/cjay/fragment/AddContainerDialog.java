package com.cloudjay.cjay.fragment;

import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.activity.WizardActivity_;
import com.cloudjay.cjay.util.Utils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

@EFragment(R.layout.fragment_addcontainer)
public class AddContainerDialog extends android.support.v4.app.DialogFragment {

	@FragmentArg("containerID")
	String containerID;

	@ViewById(R.id.tv_containterIDSearch)
	TextView tvContainerIDSearch;

	@ViewById(R.id.tv_searchresult)
	TextView tvSearchResult;

	@ViewById(R.id.btn_addContainer)
	Button btnAddContainer;

	// TODO: mismatch naming convention !! @thai please refactor those id
	// R.id.btn_cancelAddContainer --> (should be) R.id.btn_cancel or R.id.btn_cancel_add_container
	@ViewById(R.id.btn_cancelAddContainer)
	Button btnCancelAddContainer;

	public AddContainerDialog() {
	}

	@AfterViews
	void init() {
		tvContainerIDSearch.setText(containerID);
		if (Utils.isContainerIdValid(containerID)) {
			btnAddContainer.setText(R.string.dialog_create_container_id_invalid_iso);
		}
	}

	@Click(R.id.btn_cancelAddContainer)
	void btnCancelClicked() {
		getDialog().dismiss();
	}

	@Click(R.id.btn_addContainer)
	void btnAddClicked() {
		Intent wizardActivityIntent = new Intent(getActivity(), WizardActivity_.class);
		wizardActivityIntent.putExtra("containerID", containerID);
		startActivity(wizardActivityIntent);
		getDialog().dismiss();
	}

}
