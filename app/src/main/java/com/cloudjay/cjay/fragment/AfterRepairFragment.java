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
public class AfterRepairFragment extends Fragment {

	@Bean
	DataCenter dataCenter;

	public final static String CONTAINER_ID_EXTRA = "com.cloudjay.wizard.containerId";

	public final static String AUDIT_ITEM_EXTRA = "com.cloudjay.wizard.auditItemUUID";

	@FragmentArg(CONTAINER_ID_EXTRA)
	public String containerID;

	@FragmentArg(AUDIT_ITEM_EXTRA)
	public String auditItemUUID;

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
	AuditItem auditItem;
	Session mSession;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EventBus.getDefault().register(this);
	}

    @AfterViews
    void setUp() {

//        if (null == mSession) {
//            dataCenter.getSessionInBackground(getActivity().getApplicationContext(),
//                    containerID);
//        }

        if (null == imageAdapter) {
            imageAdapter = new DetailIssuedImageAdapter(getActivity(), R.layout.item_gridview_photo_multi_select, ImageType.REPAIRED);
        }

        lvImage.setAdapter(imageAdapter);
    }

    @Click(R.id.btn_camera_repaired)
    void openCameraActivity() {

        // Notify user that audit item is not uploaded yet
        if (auditItem.getId() == 0) {
            Utils.showCrouton(getActivity(), "Lỗi chưa được giám định");
            return;
        }
        if (auditItem.isAllowed() != null) {
            // Notify user that audit item is not allow to repare
            if (!auditItem.isAllowed()) {
                Utils.showCrouton(getActivity(), "Lỗi không được phép sửa");
                return;
            }
        }

        Intent cameraActivityIntent = new Intent(getActivity(), CameraActivity_.class);
        cameraActivityIntent.putExtra(CameraActivity_.CONTAINER_ID_EXTRA, containerID);
        cameraActivityIntent.putExtra(CameraActivity_.OPERATOR_CODE_EXTRA, operatorCode);
        cameraActivityIntent.putExtra(CameraActivity_.IMAGE_TYPE_EXTRA, ImageType.REPAIRED.value);
        cameraActivityIntent.putExtra(CameraActivity_.CURRENT_STEP_EXTRA, Step.REPAIR.value);
        cameraActivityIntent.putExtra(CameraActivity_.AUDIT_ITEM_UUID_EXTRA, auditItemUUID);
        cameraActivityIntent.putExtra(CameraActivity_.IS_OPENED, true);
        startActivity(cameraActivityIntent);
    }

    @Override
    public void onResume() {
        super.onResume();
	    dataCenter.add(new GetSessionCommand(getActivity(), containerID));
    }

    @UiThread
    public void onEvent(ContainerGotEvent event) {
        mSession = event.getSession();
        if (null == mSession) {
            Utils.showCrouton(getActivity(), "Không tìm thấy container trong dữ liệu");
        } else {
            operatorCode = mSession.getOperatorCode();
            refreshData();
            refreshListImage();
        }
    }

    void refreshListImage() {
        if (auditItem != null) {
            List<AuditImage> list = auditItem.getListRepairedImages();
            updatedData(list);
        }
    }

    void refreshData() {
        if (mSession != null) {
            auditItem = mSession.getAuditItem(auditItemUUID);

            // parse Data to view
            if (auditItem != null) {
                tvCompCode.setText(auditItem.getComponentCode());
                tvLocationCode.setText(auditItem.getLocationCode());
                tvDamageCode.setText(auditItem.getDamageCode());
                tvRepairCode.setText(auditItem.getRepairCode());

                tvSize.setText("Dài " + auditItem.getHeight() + ",\t" + "Rộng " + auditItem.getLength());
                textViewBtnCamera.setText(R.string.button_add_new_repair_image);
                tvNumber.setText(auditItem.getQuantity() + "");
            }
        }
    }

    @UiThread
    void updatedData(List<AuditImage> imageList) {

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
