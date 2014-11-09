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
import com.cloudjay.cjay.event.image.ImageCapturedEvent;
import com.cloudjay.cjay.model.AuditImage;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.Utils;
import com.cloudjay.cjay.util.enums.ImageType;
import com.cloudjay.cjay.util.enums.Step;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
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

	public final static String CONTAINER_ID_EXTRA = "com.cloudjay.wizard.containerID";

	public final static String AUDIT_ITEM_EXTRA = "com.cloudjay.wizard.auditItemUUID";

	@FragmentArg(CONTAINER_ID_EXTRA)
	public String containerID;

	@FragmentArg(AUDIT_ITEM_EXTRA)
	public String auditItemUUID;

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
	AuditItem auditItem;
	Session mSession;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EventBus.getDefault().register(this);
	}

	@AfterViews
	void setup() {
		//get container operater code form containerId
		mSession = dataCenter.getSession(getActivity().getApplicationContext(), containerID);
		if (null == mSession) {
			Utils.showCrouton(getActivity(), "Không tìm thấy container trong dữ liệu");
		} else {
			operatorCode = mSession.getOperatorCode();
		}
		auditItem = dataCenter.getAuditItem(getActivity(), containerID, auditItemUUID);
		// parse Data to view
		tvCompCode.setText(auditItem.getComponentCode());
		tvLocaitonCode.setText(auditItem.getLocationCode());
		tvDamageCode.setText(auditItem.getDamageCode());
		tvRepairCode.setText(auditItem.getRepairCode());
		tvSize.setText("Dài " + auditItem.getHeight() + "," + " Rộng " + auditItem.getLength());
		tvNumber.setText(auditItem.getQuantity() + "");
		textViewBtnCamera.setText(R.string.button_add_new_repair_image);

		imageAdapter = new DetailIssuedImageAdapter(getActivity(), R.layout.item_gridview_photo_multi_select, ImageType.REPAIRED);
		lvImage.setAdapter(imageAdapter);

		refreshListImage();
	}

	@Click(R.id.btn_camera_repaired)
	void openCameraActivity() {

		// Notify user that audit item is not uploaded yet
		if (auditItem.getId() == 0) {
			Utils.showCrouton(getActivity(), "Loi nay chua duoc giam dinh");
			return;
		}
		if (auditItem.isAllowed() != null) {
			// Notify user that audit item is not allow to repare
			if (!auditItem.isAllowed()) {
				Utils.showCrouton(getActivity(), "Loi nay khong duoc phep sua");
				return;
			}
		}

		Intent cameraActivityIntent = new Intent(getActivity(), CameraActivity_.class);
		cameraActivityIntent.putExtra(CameraFragment.CONTAINER_ID_EXTRA, containerID);
		cameraActivityIntent.putExtra(CameraFragment.OPERATOR_CODE_EXTRA, operatorCode);
		cameraActivityIntent.putExtra(CameraFragment.IMAGE_TYPE_EXTRA, ImageType.REPAIRED.value);
		cameraActivityIntent.putExtra(CameraFragment.CURRENT_STEP_EXTRA, Step.REPAIR.value);
		cameraActivityIntent.putExtra(CameraFragment.AUDIT_ITEM_UUID_EXTRA, auditItemUUID);
		cameraActivityIntent.putExtra(CameraFragment.IS_OPENED, true);
		startActivity(cameraActivityIntent);
	}

	@Background
	void refreshListImage() {
		if (auditItem != null) {
			List<AuditImage> list = auditItem.getListRepairedImages();
			updatedData(list);
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

	@UiThread
	void onEvent(ImageCapturedEvent event) {
		Logger.Log("on ImageCapturedEvent");

		// Requery audit item by uuid to update listview
		auditItem = dataCenter.getAuditItem(getActivity().getApplicationContext(),
				containerID, auditItemUUID);
		refreshListImage();
	}

	@Override
	public void onDestroy() {
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}
}
