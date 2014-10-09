package com.cloudjay.cjay.fragment.dialog;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.activity.WizardActivity_;
import com.cloudjay.cjay.fragment.dialog.AddInvalidContainerIsoDialog_;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.PreferencesUtil;
import com.cloudjay.cjay.util.Utils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import eu.inmite.android.lib.dialogs.SimpleDialogFragment;

@EFragment(R.layout.fragment_addcontainer)
public class AddContainerDialog extends SimpleDialogFragment {

    @FragmentArg("containerId")
    String containerId;

    @ViewById(R.id.et_container_id)
    EditText etContainerID;

    @Override
    protected Builder build(final Builder builder) {
        builder.setTitle(R.string.dialog_search_container_title);
        builder.setView(LayoutInflater.from(getActivity()).inflate(R.layout.fragment_addcontainer, null));
        builder.setNegativeButton("Bỏ qua", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        builder.setPositiveButton("Tạo mới", new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Get Container Id from EditText
                containerId = etContainerID.getText().toString();

                // Check container id is valid or not
                if (TextUtils.isEmpty(containerId)) {
                    Logger.Log("empty");
                    etContainerID.setError(getString(R.string.dialog_container_id_required));
                } else if (!Utils.simpleValid(containerId)) {
                    Logger.Log("invalid");
                    etContainerID.setError(getString(R.string.dialog_container_id_invalid));
                } else {
                    Logger.Log("valid");
                    // Check invalid container ISO
                    if(!Utils.isContainerIdValid(containerId)) {
                        showAddInvalidContainerISODialog(containerId);
                    } else {
                        Intent intent = new Intent(getActivity(), WizardActivity_.class);
                        startActivity(intent);
                        dismiss();
                    }
                }

            }
        });

        return builder;
    }

    @AfterViews
    void doAfterViews() {
        // Set search keywotrd into edit text
        etContainerID.setText(containerId);
    }

    private void showAddInvalidContainerISODialog(String containerId) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        AddInvalidContainerIsoDialog addContainerDialog_ = AddInvalidContainerIsoDialog_
                .builder().containerId(containerId).build();
        addContainerDialog_.show(fragmentManager, "fragment_addcontainer");

    }
}
