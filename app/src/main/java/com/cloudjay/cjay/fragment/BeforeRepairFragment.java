package com.cloudjay.cjay.fragment;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.activity.CameraActivity;
import com.cloudjay.cjay.activity.CameraActivity_;
import com.cloudjay.cjay.adapter.DetailIssuedImageAdapter;
import com.cloudjay.cjay.model.AuditImage;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.Utils;
import com.cloudjay.cjay.util.enums.ImageType;
import com.cloudjay.cjay.util.enums.Step;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.util.List;

/**
 * Need 2 argument:String containerId, Audit item to init View.
 */

@EFragment(R.layout.fragment_before_after_repaierd)
public class BeforeRepairFragment extends Fragment {
    @Bean
    DataCenter dataCenter;

    public final static String CONTAINER_ID_EXTRA = "com.cloudjay.wizard.containerID";

    public final static String AUDIT_ITEM_EXTRA = "com.cloudjay.wizard.auditItem";

    @FragmentArg(CONTAINER_ID_EXTRA)
    public String containerID;

    @FragmentArg(AUDIT_ITEM_EXTRA)
    public AuditItem auditItem;

    @ViewById(R.id.tv_code_comp_repaired)
    TextView tvCompCode;

    @ViewById(R.id.tv_code_location_repaired)
    TextView tvLocaitonCode;

    @ViewById(R.id.tv_code_damaged_repaired)
    TextView tvDamageCode;

    @ViewById(R.id.tv_code_repair_repaired)
    TextView tvRepairCode;

    @ViewById(R.id.tv_size_repaired)
    TextView tvSize;

    @ViewById(R.id.tv_number_repaired)
    TextView tvNumber;

    @ViewById(R.id.lv_image_repaired)
    ListView lvImage;

    @ViewById(R.id.btn_camera_repaired)
    LinearLayout btnCamera;

    @ViewById(R.id.image_button_2_text)
    TextView textViewBtnCamera;

    DetailIssuedImageAdapter imageAdapter;
    String operatorCode;

    @AfterViews
    void setup() {
        //get container operater code form containerId
        Session tmp = dataCenter.getSession(getActivity().getApplicationContext(), containerID);
        if (null == tmp) {
            Utils.showCrouton(getActivity(), "Không tìm thấy container trong dữ liệu");
        } else {
            operatorCode = tmp.getOperatorCode();
        }
        // parse Data to view
        tvCompCode.setText(auditItem.getComponentCode());
        tvLocaitonCode.setText(auditItem.getLocationCode());
        tvDamageCode.setText(auditItem.getDamageCode());
        tvRepairCode.setText(auditItem.getRepairCode());
        tvSize.setText("Dài " + auditItem.getHeight() + "," + " Rộng " + auditItem.getLength());
        textViewBtnCamera.setText(R.string.button_add_new_audit_image);

        //TODO add fiel number to audit item model @Nam
        imageAdapter = new DetailIssuedImageAdapter(getActivity(), R.layout.item_gridview_photo_multi_select, ImageType.AUDIT);
        List<AuditImage> auditImages = auditItem.getAuditImages();
        imageAdapter.setData(auditImages);
        lvImage.setAdapter(imageAdapter);


    }

    @Click(R.id.btn_camera_repaired)
    void openCameraActivity() {
        //get container operater code form containerId
        String operatorCode = null;
        Session tmp = dataCenter.getSession(getActivity().getApplicationContext(), containerID);
        if (null == tmp) {
            Utils.showCrouton(getActivity(), "Không tìm thấy container trong dữ liệu");
        } else {
            operatorCode = tmp.getOperatorCode();
        }
        Intent cameraActivityIntent = new Intent(getActivity(), CameraActivity_.class);
        cameraActivityIntent.putExtra(CameraFragment.CONTAINER_ID_EXTRA, containerID);
        cameraActivityIntent.putExtra(CameraFragment.OPERATOR_CODE_EXTRA, operatorCode);
        cameraActivityIntent.putExtra(CameraFragment.IMAGE_TYPE_EXTRA, ImageType.AUDIT.value);
        cameraActivityIntent.putExtra(CameraFragment.CURRENT_STEP_EXTRA, Step.AUDIT.value);
        startActivity(cameraActivityIntent);
    }

    @Override
    public void onResume() {
        super.onResume();

        refreshListImage();
    }

    private void refreshListImage() {
        Session tmp = dataCenter.getSession(getActivity().getApplicationContext(), containerID);
        for (AuditItem currentAuditItem : tmp.getAuditItems()) {
            if (currentAuditItem.getId() == auditItem.getId()) {
                auditItem = currentAuditItem;
            }
        }
        List<AuditImage> auditImages = auditItem.getAuditImages();
        imageAdapter.setData(auditImages);
        imageAdapter.notifyDataSetChanged();
    }
}
