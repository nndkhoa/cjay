package com.cloudjay.cjay.fragment;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.activity.CameraActivity_;
import com.cloudjay.cjay.adapter.AuditItemAdapter;
import com.cloudjay.cjay.event.ContainerSearchedEvent;
import com.cloudjay.cjay.event.ImageCapturedEvent;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.enums.Status;
import com.cloudjay.cjay.util.enums.Step;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * A simple {@link Fragment} subclass.
 */
@EFragment(R.layout.fragment_issue_pending)
public class IssuePendingFragment extends Fragment {

    public final static String CONTAINER_ID_EXTRA = "com.cloudjay.wizard.containerID";

    @FragmentArg(CONTAINER_ID_EXTRA)
    public String containerID;

    @ViewById(R.id.tv_container_code)
    TextView tvContainerId;

    @ViewById(R.id.tv_current_status)
    TextView tvCurrentStatus;

    @ViewById(R.id.btn_camera)
    ImageButton btnCamera;

    @ViewById(R.id.lv_issue_images)
    ListView lvIssueImages;

    @Bean
    DataCenter dataCenter;

    String operatorCode;
    AuditItemAdapter auditItemAdapter;
    List<AuditItem> auditItems;

	public IssuePendingFragment() {
		// Required empty public constructor
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @AfterViews
    void setUp() {
        Logger.Log("setUp");
        // Get session by containerId
        dataCenter.getSessionByContainerId(containerID);

        // Set text ContainerId TextView
        tvContainerId.setText(containerID);
    }

    @UiThread
    void onEvent(ContainerSearchedEvent event) {
        List<Session> result = event.getSessions();

        // Set currentStatus to TextView
        tvCurrentStatus.setText((Status.values()[(int)result.get(0).getStatus()]).toString());

        // Set operatorCode into variable
        operatorCode = result.get(0).getOperatorCode();

    }

    @Click(R.id.btn_camera)
    void buttonCameraClicked() {
        // Open camera activity
        Intent cameraActivityIntent = new Intent(getActivity(), CameraActivity_.class);
        cameraActivityIntent.putExtra(CameraFragment.CONTAINER_ID_EXTRA, containerID);
        cameraActivityIntent.putExtra(CameraFragment.IMAGE_TYPE_EXTRA, CJayConstant.TYPE_AUDIT);
        cameraActivityIntent.putExtra(CameraFragment.OPERATOR_CODE_EXTRA, operatorCode);
        cameraActivityIntent.putExtra(CameraFragment.CURRENT_STEP_EXTRA, Step.AUDIT.value);
        startActivity(cameraActivityIntent);
    }

    @ItemClick(R.id.lv_issue_images)
    void showApproveDiaglog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_search_container_title);
        builder.setMessage("Lỗi này đã chưa được. Sửa luôn?");

        builder.setPositiveButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //TODO show chon loi da giam dinh @Nam
                dialogInterface.dismiss();
            }
        });

        builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //TODO add to database
                dialogInterface.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {

                // Set background and text color for confirm button
                ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(getActivity().getResources().getColor(android.R.color.holo_green_dark));
                ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE)
                        .setBackgroundResource(getActivity().getResources().getColor(android.R.color.darker_gray));
            }
        });
        dialog.show();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();

        auditItems = dataCenter.getAuditItems(containerID);
        Logger.Log("auditItems: " + auditItems.size());
        if (auditItems == null) {
            auditItems = new ArrayList<AuditItem>();
        }

        if (auditItemAdapter == null) {
            auditItemAdapter = new AuditItemAdapter(getActivity(),
                    R.layout.item_issue_pending, auditItems);
            lvIssueImages.setAdapter(auditItemAdapter);
        }

        auditItemAdapter.swapData(auditItems);
    }
}
