package com.cloudjay.cjay.fragment.dialog;

import android.support.v4.app.FragmentManager;
import android.view.View;

import com.cloudjay.cjay.R;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;

import eu.inmite.android.lib.dialogs.SimpleDialogFragment;

/**
 * Created by nambv on 08/10/2014.
 */
@EFragment(R.layout.dialog_search_session_result)
public class SearchResultContainerDialog extends SimpleDialogFragment {

    @FragmentArg("containerId")
    String containerId;

    @Override
    protected Builder build(Builder builder) {
        builder.setTitle(R.string.dialog_search_container_title);
        builder.setPositiveButton("Tạo mới", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddContainerDialog(containerId);
                dismiss();
            }
        });
        builder.setNegativeButton("Bỏ qua", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        builder.setMessage("Container ID với từ khóa " + containerId + " chưa được nhập vào hệ thống");
        return builder;
    }

    private void showAddContainerDialog(String containerId) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        AddContainerDialog addContainerDialog_ = AddContainerDialog_
                .builder().containerId(containerId).build();
        addContainerDialog_.show(fragmentManager, "fragment_addcontainer");

    }
}
