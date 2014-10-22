package com.cloudjay.cjay.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.activity.CameraActivity_;
import com.cloudjay.cjay.activity.PhotoExpandableListViewActivity_;
import com.cloudjay.cjay.adapter.GateImageAdapter;
import com.cloudjay.cjay.adapter.PhotoExpandableListAdapter;
import com.cloudjay.cjay.event.AuditImagesGotEvent;
import com.cloudjay.cjay.event.ContainerSearchedEvent;
import com.cloudjay.cjay.event.GateImagesGotEvent;
import com.cloudjay.cjay.event.ImageCapturedEvent;
import com.cloudjay.cjay.model.AuditImage;
import com.cloudjay.cjay.model.GateImage;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.enums.Status;
import com.snappydb.SnappydbException;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
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

    @ViewById(R.id.tv_status_name)
    TextView tvPreStatus;

    @ViewById(R.id.tv_current_status)
    TextView tvCurrentStatus;

    @ViewById(R.id.lv_images_expandable)
    ExpandableListView lvImagesExpandable;

    @Bean
    DataCenter dataCenter;

    GateImageAdapter gateImageAdapter = null;
    List<GateImage> mGateImages = null;
    List<AuditImage> mAuditImages = null;

    PhotoExpandableListAdapter mListAdapter;
    int[] mImageTypes;

    List<GateImage> importImages = new ArrayList<GateImage>();
    List<AuditImage> auditImages = new ArrayList<AuditImage>();
    List<AuditImage> repairedImages = new ArrayList<AuditImage>();
    List<GateImage> exportImages = new ArrayList<GateImage>();

    String operatorCode;
    long preStatus;
    long currentStatus;

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

        // Init image types
        mImageTypes = new int[3];
        mImageTypes[0] = CJayConstant.TYPE_IMPORT;
        mImageTypes[1] = CJayConstant.TYPE_AUDIT;
        mImageTypes[2] = CJayConstant.TYPE_REPAIRED;

        // Get import images by containerId
        try {
            dataCenter.getGateImages(CJayConstant.TYPE_IMPORT, containerID);
        } catch (SnappydbException e) {
            e.printStackTrace();
        }

        // Get export images by containerId
        try {
            dataCenter.getGateImages(CJayConstant.TYPE_EXPORT, containerID);
        } catch (SnappydbException e) {
            e.printStackTrace();
        }

        // Get audit and repaired images by containerId
        dataCenter.getAuditImages(containerID);
    }

    @Click(R.id.btn_take_export_picture)
    void buttonTakeExportPictureClicked() {
        // Open camera activity
        Intent cameraActivityIntent = new Intent(getActivity(), CameraActivity_.class);
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
        mGateImages = event.getGateImages();
        Logger.Log("mGateImages: " + mGateImages.size());

        for (GateImage g : mGateImages) {
            // Create list import images
            if (g.getType() == CJayConstant.TYPE_IMPORT) {
                importImages.add(g);
            } else if (g.getType() == CJayConstant.TYPE_EXPORT) {
                exportImages.add(g);
            }
        }

        //Init adapter if null and set adapter for listview
        if (gateImageAdapter == null) {
            gateImageAdapter = new GateImageAdapter(getActivity(), exportImages, false);
            gvExportImages.setAdapter(gateImageAdapter);
        }

        // Notify change
        gateImageAdapter.swapData(mGateImages);

    }

    @UiThread
    void onEvent(AuditImagesGotEvent event) {

        // Get audit images from event
        mAuditImages = event.getAuditImages();
        Logger.Log("mAuditImages: " + mAuditImages.size());

        for (AuditImage a : mAuditImages) {
            // Create list audit images
            if (a.getType() == CJayConstant.TYPE_AUDIT) {
                Logger.Log("TYPE_AUDIT");
                auditImages.add(a);
            } else if (a.getType() == CJayConstant.TYPE_REPAIRED) {
                Logger.Log("TYPE_REPAIRED");
                repairedImages.add(a);
            }
        }

        if (mListAdapter == null) {
            mListAdapter = new PhotoExpandableListAdapter(getActivity(),
                    mImageTypes, importImages, auditImages, repairedImages);
            lvImagesExpandable.setAdapter(mListAdapter);

            for (int i = 0; i < mImageTypes.length; i++) {
                lvImagesExpandable.expandGroup(i);
            }
        }

    }

    @UiThread
    void onEvent(ContainerSearchedEvent event) {
        List<Session> result = event.getSessions();
        operatorCode = result.get(0).getOperatorCode();
        preStatus = result.get(0).getPreStatus();
        currentStatus = result.get(0).getStatus();

        // Set preStatus to TextView
        tvPreStatus.setText((Status.values()[(int)preStatus]).toString());

        // Set currentStatus to TextView
        tvCurrentStatus.setText((Status.values()[(int)currentStatus]).toString());
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
