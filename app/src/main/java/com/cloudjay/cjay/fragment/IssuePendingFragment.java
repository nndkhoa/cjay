package com.cloudjay.cjay.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.activity.CameraActivity;
import com.cloudjay.cjay.event.ContainerSearchedEvent;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.enums.Status;
import com.cloudjay.cjay.util.enums.Step;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

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
        Intent cameraActivityIntent = new Intent(getActivity(), CameraActivity.class);
        cameraActivityIntent.putExtra(CameraFragment.CONTAINER_ID_EXTRA, containerID);
        cameraActivityIntent.putExtra(CameraFragment.IMAGE_TYPE_EXTRA, CJayConstant.TYPE_AUDIT);
        cameraActivityIntent.putExtra(CameraFragment.OPERATOR_CODE_EXTRA, operatorCode);
        cameraActivityIntent.putExtra(CameraFragment.CURRENT_STEP_EXTRA, Step.AUDIT.value);
        startActivity(cameraActivityIntent);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
