package com.cloudjay.cjay.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.activity.CameraActivity_;
import com.cloudjay.cjay.adapter.DetailIssuedImageAdapter;
import com.cloudjay.cjay.event.session.ContainerGotEvent;
import com.cloudjay.cjay.model.AuditImage;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.command.session.get.GetSessionCommand;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.Utils;
import com.cloudjay.cjay.util.enums.ImageType;
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
 * Need 2 argument:String containerId, Audit item to init View.
 */

@EFragment(R.layout.fragment_before_after_repaierd)
public class BeforeRepairFragment extends Fragment {
	@Bean
	DataCenter dataCenter;

	public final static String CONTAINER_ID_EXTRA = "com.cloudjay.wizard.containerId";

	public final static String AUDIT_ITEM_EXTRA = "com.cloudjay.wizard.auditItemUUID";

	@FragmentArg(CONTAINER_ID_EXTRA)
	public String containerID;

	@FragmentArg(AUDIT_ITEM_EXTRA)
	public String auditItemUUID;
	;

	@ViewById(R.id.tv_code_comp_repaired)
	TextView tvCompCode;

	@ViewById(R.id.tv_code_location_repaired)
	TextView tvLocationCode;

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
	Session mSession;

    boolean hasImageToUpload = false;
    boolean updateData = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EventBus.getDefault().register(this);
	}

	@AfterViews
    void setUp() {
        if (null == imageAdapter) {
            imageAdapter = new DetailIssuedImageAdapter(
                    getActivity(), R.layout.item_gridview_photo_multi_select, ImageType.AUDIT);
        }
        lvImage.setAdapter(imageAdapter);
    }

    @Click(R.id.btn_camera_repaired)
    void openCameraActivity() {
        Intent cameraActivityIntent = new Intent(getActivity(), CameraActivity_.class);
        cameraActivityIntent.putExtra(CameraActivity_.CONTAINER_ID_EXTRA, containerID);
        cameraActivityIntent.putExtra(CameraActivity_.OPERATOR_CODE_EXTRA, operatorCode);
        cameraActivityIntent.putExtra(CameraActivity_.IMAGE_TYPE_EXTRA, ImageType.AUDIT.value);
        cameraActivityIntent.putExtra(CameraActivity_.CURRENT_STEP_EXTRA, Step.AUDIT.value);
        cameraActivityIntent.putExtra(CameraActivity_.AUDIT_ITEM_UUID_EXTRA, auditItemUUID);
        startActivity(cameraActivityIntent);

        // Set update data = true to refresh data after taken picture
        updateData = true;
    }

    @UiThread
    void onEvent(ContainerGotEvent event) {
        mSession = event.getSession();
        if (null == mSession) {
            Utils.showCrouton(getActivity(), "Không tìm thấy container trong dữ liệu");
        } else {
            operatorCode = mSession.getOperatorCode();
            refreshData();
            refreshListImage();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if (updateData) {
            updateData = false;
            dataCenter.add(new GetSessionCommand(getActivity(), containerID));
        }
    }

    void refreshListImage() {
        if (mSession != null) {
            List<AuditImage> list = mSession.getAuditItem(auditItemUUID).getListAuditedImages();
            updatedData(list);
        }
    }

    void refreshData() {
        if (mSession != null) {
            AuditItem auditItem = mSession.getAuditItem(auditItemUUID);
            Logger.Log("is audited: " + auditItem.isAudited());
            if (auditItem.getId() != 0) {
                for (AuditImage image : auditItem.getListAuditedImages()) {
                    if (image.getId() == 0) {
                        hasImageToUpload = true;
                        break;
                    }
                }
            }

            // parse Data to view
            int height = (int) auditItem.getHeight();
            int length = (int) auditItem.getLength();

            tvCompCode.setText(auditItem.getComponentCode());
            tvLocationCode.setText(auditItem.getLocationCode());
            tvDamageCode.setText(auditItem.getDamageCode());
            tvRepairCode.setText(auditItem.getRepairCode());

            tvSize.setText("Dài " + String.valueOf(length) + ",\t" + "Rộng " + String.valueOf(height));
            textViewBtnCamera.setText(R.string.button_add_new_audit_image);
            tvNumber.setText(auditItem.getQuantity() + "");
        }
    }

    @UiThread
    public void updatedData(List<AuditImage> imageList) {

        imageAdapter.clear();
        if (imageList != null) {
            for (AuditImage object : imageList) {
                imageAdapter.add(object);
            }
        }

        imageAdapter.notifyDataSetChanged();
    }


	@Override
	public void onDestroy() {
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}
}
