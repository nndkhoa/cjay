package com.cloudjay.cjay.fragment;


import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.activity.CameraActivity_;
import com.cloudjay.cjay.activity.ReuseActivity_;
import com.cloudjay.cjay.adapter.GateImageAdapter;
import com.cloudjay.cjay.event.image.ImageCapturedEvent;
import com.cloudjay.cjay.model.AuditImage;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.enums.ImageType;
import com.cloudjay.cjay.util.enums.Status;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

/**
 * Màn hình giám định
 */
@EFragment(R.layout.fragment_audit)
public class AuditFragment extends Fragment {

	public final static String CONTAINER_ID_EXTRA = "com.cloudjay.wizard.containerId";

	@FragmentArg(CONTAINER_ID_EXTRA)
	String containerID;

	//region VIEW
	@ViewById(R.id.btn_done)
	Button btnContinue;

	@ViewById(R.id.btn_reuse_gate_in_image)
	Button btnReuseGateInImage;

	@ViewById(R.id.lv_audit_images)
	ListView lvAuditImages;

    @ViewById(R.id.tv_container_code)
    TextView tvContainerId;

    @ViewById(R.id.tv_current_status)
    TextView tvCurrentStatus;

    @ViewById(R.id.btn_camera)
    ImageButton btnCamera;
	//endregion

    @Bean
    DataCenter dataCenter;

    GateImageAdapter adapter;
    List<AuditImage> auditImages;
    String operatorCode;
    long currentStatus;
    Session mSession;

	public AuditFragment() {
	}

	@Click(R.id.btn_done)
	void buttonContinueClicked() {
		//Go to next fragment
		RepairFragment fragment = new RepairFragment_().builder().containerID(containerID).build();
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
		transaction.replace(R.id.ll_main, fragment);
		transaction.commit();
	}

	@Click(R.id.btn_reuse_gate_in_image)
	void buttonReuseGateInImageClicked() {
		// Open ReuseActivity
		Intent intent = new Intent(getActivity(), ReuseActivity_.class);
        intent.putExtra(ReuseActivity_.CONTAINER_ID_EXTRA, containerID);
		startActivityForResult(intent, 1);
	}

    @Click(R.id.btn_camera)
    void buttonCameraClicked() {
        // Open camera activity
        Intent cameraActivityIntent = new Intent(getActivity(), CameraActivity_.class);
        cameraActivityIntent.putExtra("containerId", containerID);
        cameraActivityIntent.putExtra("imageType", ImageType.EXPORT.value);
        cameraActivityIntent.putExtra("operatorCode", operatorCode);
        startActivity(cameraActivityIntent);
    }

    @AfterViews
    void setUp() {

        // Set ActionBar Title
        getActivity().getActionBar().setTitle(R.string.fragment_audit_title);

        // Get session by containerId
        mSession = dataCenter.getSession(getActivity().getApplicationContext(), containerID);

        if (mSession != null) {
            // Get operator code
            containerID = mSession.getContainerId();
            operatorCode = mSession.getOperatorCode();

            // Set currentStatus to TextView
            currentStatus = mSession.getStatus();
            tvCurrentStatus.setText((Status.values()[(int) currentStatus]).toString());

            // Set ContainerId to TextView
            tvContainerId.setText(containerID);

            refresh();
        } else {
            // Set ContainerId to TextView
            tvContainerId.setText(containerID);
        }

    }

    @UiThread
    void onEvent(ImageCapturedEvent event) {

    }

    void refresh() {
        adapter.clear();
    }
}
