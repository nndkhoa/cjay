package com.cloudjay.cjay.fragment.dialog;

import android.view.View;

import com.cloudjay.cjay.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;

import eu.inmite.android.lib.dialogs.SimpleDialogFragment;

/**
 * Created by nambv on 08/10/2014.
 */
@EFragment(R.layout.dialog_search_session_result)
public class AddInvalidContainerIsoDialog extends SimpleDialogFragment {

    @FragmentArg("containerId")
    String containerId;

    @Override
    protected Builder build(Builder builder) {
        builder.setTitle(R.string.dialog_search_container_title);
        builder.setPositiveButton("Tạo Container sai ISO", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        builder.setNegativeButton("Bỏ qua", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        builder.setMessage("ContainerID " + containerId + " này sai chuẩn ISO.");
        return builder;
    }

    @AfterViews
    void doAfterViews() {

    }
}
