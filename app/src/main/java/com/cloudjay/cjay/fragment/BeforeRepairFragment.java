package com.cloudjay.cjay.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.activity.CameraActivity_;
import com.cloudjay.cjay.adapter.DetailIssuedImageAdapter;
import com.cloudjay.cjay.event.session.ContainerGotEvent;
import com.cloudjay.cjay.model.AuditImage;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.job.UploadAuditItemJob;
import com.cloudjay.cjay.util.Utils;
import com.cloudjay.cjay.util.enums.ImageType;
import com.cloudjay.cjay.util.enums.Step;
import com.path.android.jobqueue.JobManager;

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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EventBus.getDefault().register(this);
	}

	@AfterViews
    void setUp() {
        if (null == mSession) {
            dataCenter.getSessionInBackground(getActivity().getApplicationContext(),
                    containerID);
        }

        if (null == imageAdapter) {
            imageAdapter = new DetailIssuedImageAdapter(
                    getActivity(), R.layout.item_gridview_photo_multi_select, ImageType.AUDIT);
        }
        lvImage.setAdapter(imageAdapter);
    }

    @Click(R.id.btn_camera_repaired)
    void openCameraActivity() {
        Intent cameraActivityIntent = new Intent(getActivity(), CameraActivity_.class);
        cameraActivityIntent.putExtra(CameraFragment.CONTAINER_ID_EXTRA, containerID);
        cameraActivityIntent.putExtra(CameraFragment.OPERATOR_CODE_EXTRA, operatorCode);
        cameraActivityIntent.putExtra(CameraFragment.IMAGE_TYPE_EXTRA, ImageType.AUDIT.value);
        cameraActivityIntent.putExtra(CameraFragment.CURRENT_STEP_EXTRA, Step.AUDIT.value);
        cameraActivityIntent.putExtra(CameraFragment.AUDIT_ITEM_UUID_EXTRA, auditItemUUID);
        startActivity(cameraActivityIntent);
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
            if (hasImageToUpload) {
                addImageToJobqueue();
            }
        }
    }

//    @UiThread
//    void onEvent(ImageCapturedEvent event) {
//        Logger.Log("on ImageCapturedEvent");
//        if (event.getImageType() == ImageType.AUDIT.value) {
//            // Requery session to update data
//            dataCenter.getSessionInBackground(getActivity().getApplicationContext(),
//                    containerID);
//        }
//    }

    @Override
    public void onResume() {
        super.onResume();
        dataCenter.getSessionInBackground(getActivity().getApplicationContext(),
                    containerID);
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
            if (auditItem.getId() != 0) {
                for (AuditImage image : auditItem.getListAuditedImages()) {
                    if (image.getId() == 0) {
                        hasImageToUpload = true;
                        break;
                    }
                }
            }

            // parse Data to view
            tvCompCode.setText(auditItem.getComponentCode());
            tvLocationCode.setText(auditItem.getLocationCode());
            tvDamageCode.setText(auditItem.getDamageCode());
            tvRepairCode.setText(auditItem.getRepairCode());

            tvSize.setText("Dài " + auditItem.getHeight() + ",\t" + "Rộng " + auditItem.getLength());
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

    void addImageToJobqueue() {
        JobManager jobManager = App.getJobManager();
        jobManager.addJobInBackground(new UploadAuditItemJob(mSession.getId(),
                mSession.getAuditItem(auditItemUUID), containerID, true));
    }

	@Override
	public void onDestroy() {
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}
}
