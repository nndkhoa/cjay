package com.cloudjay.cjay.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.activity.CameraActivity;
import com.cloudjay.cjay.activity.PhotoExpandableListViewActivity_;
import com.cloudjay.cjay.adapter.GateImageAdapter;
import com.cloudjay.cjay.event.ContainerSearchedEvent;
import com.cloudjay.cjay.event.GateImagesGotEvent;
import com.cloudjay.cjay.event.ImageCapturedEvent;
import com.cloudjay.cjay.model.GateImage;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.snappydb.SnappydbException;

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
 * Màn hình xuất
 */
@EFragment(R.layout.fragment_export)
public class ExportFragment extends Fragment {

	public final static String CONTAINER_ID_EXTRA = "com.cloudjay.wizard.containerID";

	@FragmentArg(CONTAINER_ID_EXTRA)
	String containerID;

    @ViewById(R.id.tv_container_code)
    TextView tvContainerId;

    @ViewById(R.id.btn_take_export_picture)
    LinearLayout btnTakeExportPicture;

    @ViewById(R.id.gv_images)
    GridView gvExportImages;

    @ViewById(R.id.btn_view_previous_step)
    Button btnViewPreviousSteps;

    @Bean
    DataCenter dataCenter;

    GateImageAdapter gateImageAdapter = null;
    List<GateImage> gateImages = null;

    String operatorCode;

	public ExportFragment() {
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @AfterViews
    void setUp() {

        // Set ActionBar Title
        getActivity().getActionBar().setTitle(R.string.fragment_export_title);

        // Set ContainerId to TextView
        tvContainerId.setText(containerID);

        // Search session by containerId to get operatorCode
        dataCenter.getSessionByContainerId(containerID);
    }

    @Click(R.id.btn_take_export_picture)
    void buttonTakeExportPictureClicked() {
        // Open camera activity
        Intent cameraActivityIntent = new Intent(getActivity(), CameraActivity.class);
        cameraActivityIntent.putExtra("containerID", containerID);
        cameraActivityIntent.putExtra("imageType", CJayConstant.TYPE_EXPORT);
        cameraActivityIntent.putExtra("operatorCode", operatorCode);
        startActivity(cameraActivityIntent);
    }

    @UiThread
    void onEvent(ImageCapturedEvent event) {
        // Get gate images from realm
        try {
            dataCenter.getGateImages(CJayConstant.TYPE_EXPORT, containerID);
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    @UiThread
    void onEvent(GateImagesGotEvent event) {

        // Get gate image objects from event post back
        gateImages = event.getGateImages();
        Logger.Log("size: " + gateImages.size());

        //Init adapter if null and set adapter for listview
        if (gateImageAdapter == null) {
            gateImageAdapter = new GateImageAdapter(getActivity(), gateImages);
            gvExportImages.setAdapter(gateImageAdapter);
        }

        // Notify change
        gateImageAdapter.swapData(gateImages);

    }

    @UiThread
    void onEvent(ContainerSearchedEvent event) {
        List<Session> result = event.getSessions();
        operatorCode = result.get(0).getOperatorCode();
    }

    @Click(R.id.btn_view_previous_step)
    void buttonViewPreClicked() {
        Intent intent = new Intent(getActivity(), PhotoExpandableListViewActivity_.class);
        intent.putExtra(PhotoExpandableListViewActivity_.CONTAINER_ID_EXTRA, containerID);
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
