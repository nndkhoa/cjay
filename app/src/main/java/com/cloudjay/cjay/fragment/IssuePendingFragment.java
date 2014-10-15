package com.cloudjay.cjay.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.TextView;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.event.ContainerSearchedEvent;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.enums.Status;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
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

    @Bean
    DataCenter dataCenter;

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
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
