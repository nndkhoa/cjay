package com.cloudjay.cjay.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
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

    @ViewById(R.id.tv_containterid_search)
    TextView tvContainerIDSearch;

    @ViewById(R.id.tv_search_result)
    TextView tvSearchResult;

    @ViewById(R.id.btn_addContainer)
    Button btnAddContainer;

    @ViewById(R.id.et_containerid_diaglog)
    EditText etContainerID;

    // TODO: mismatch naming convention !! @thai please refactor those id
    // R.id.btn_cancelAddContainer --> (should be) R.id.btn_cancel or R.id.btn_cancel_add_container
    @ViewById(R.id.btn_cancelAddContainer)
    Button btnCancelAddContainer;

    public AddContainerDialog() {
    }

    @AfterViews
    void init() {
        //Remove title bar
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        tvSearchResult.setText(containerID);
        if (Utils.isContainerIdValid(containerID)) {
            btnAddContainer.setText(R.string.dialog_create_container_id_invalid_iso);
        }
        if (!Utils.simpleValid(containerID)) {
            tvContainerIDSearch.setVisibility(View.GONE);
            etContainerID.setVisibility(View.VISIBLE);
        }
    }

    @Click(R.id.btn_cancelAddContainer)
    void btnCancelClicked() {
        getDialog().dismiss();
    }

    @Click(R.id.btn_addContainer)
    void btnAddClicked() {
        if (etContainerID.getVisibility() == View.VISIBLE) {
            containerID = etContainerID.getText().toString();
            startWizadActivity(containerID);
        } else {
            startWizadActivity(containerID);
        }

    }

    private void startWizadActivity(String containerID) {
        Intent wizardActivityIntent = new Intent(getActivity(), WizardActivity_.class);
        wizardActivityIntent.putExtra("containerID", containerID);
        startActivity(wizardActivityIntent);
        getDialog().dismiss();
    }

}
