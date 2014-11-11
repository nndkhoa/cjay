package com.cloudjay.cjay.fragment;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.activity.CameraActivity_;
import com.cloudjay.cjay.activity.DetailIssueActivity;
import com.cloudjay.cjay.activity.DetailIssueActivity_;
import com.cloudjay.cjay.activity.ReuseActivity_;
import com.cloudjay.cjay.adapter.AuditItemAdapter;
import com.cloudjay.cjay.event.image.ImageCapturedEvent;
import com.cloudjay.cjay.event.issue.AuditItemChangedEvent;
import com.cloudjay.cjay.event.issue.AuditItemsGotEvent;
import com.cloudjay.cjay.event.issue.IssueMergedEvent;
import com.cloudjay.cjay.event.session.ContainersGotEvent;
import com.cloudjay.cjay.event.upload.UploadStartedEvent;
import com.cloudjay.cjay.event.upload.UploadSucceededEvent;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.job.UploadImportJob;
import com.cloudjay.cjay.task.job.UploadSessionJob;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.enums.ImageType;
import com.cloudjay.cjay.util.enums.Status;
import com.cloudjay.cjay.util.enums.Step;
import com.cloudjay.cjay.util.enums.UploadStatus;
import com.cloudjay.cjay.util.enums.UploadType;
import com.path.android.jobqueue.JobManager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.Trace;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * A simple {@link Fragment} subclass.
 */
@EFragment(R.layout.fragment_issue_pending)
public class IssuePendingFragment extends Fragment {

	//region ATTR
	public final static String CONTAINER_ID_EXTRA = "com.cloudjay.wizard.containerId";

	@FragmentArg(CONTAINER_ID_EXTRA)
	public String containerId;

	@Bean
	DataCenter dataCenter;

	String operatorCode;
	long currentStatus;
	AuditItemAdapter mAdapter;

	Session mSession;
	//endregion

	//region VIEWS
	@ViewById(R.id.tv_container_code)
	TextView tvContainerId;

	@ViewById(R.id.tv_current_status)
	TextView tvCurrentStatus;

	@ViewById(R.id.btn_camera)
	LinearLayout btnCamera;

	@ViewById(R.id.btn_clean)
	Button btnClean;

	@ViewById(R.id.lv_audit_items)
	ListView lvAuditItems;
	//endregion

	public IssuePendingFragment() {
		// Required empty public constructor
	}

	@AfterViews
	void setUp() {
		dataCenter.getSessionInBackground(getActivity(), containerId);
	}

	//region VIEW INTERACTION

	@Click(R.id.btn_clean)
	void buttonCleanClicked() {

		// Add container session to upload queue
		JobManager jobManager = App.getJobManager();
		jobManager.addJobInBackground(new UploadImportJob(mSession));

		getActivity().finish();
	}

	@Click(R.id.btn_camera)
	void buttonCameraClicked() {
        showUseGateImageDialog();
	}

	@ItemClick(R.id.lv_audit_items)
	void auditItemClicked(int position) {

		AuditItem auditItem = mAdapter.getItem(position);
		Logger.Log("getUuid: " + auditItem.getUuid());

		if (auditItem.isAudited()) {
			Intent detailIssueActivity = new Intent(getActivity(), DetailIssueActivity_.class);
			detailIssueActivity.putExtra(DetailIssueActivity.CONTAINER_ID_EXTRA, containerId);
			detailIssueActivity.putExtra(DetailIssueActivity.AUDIT_ITEM_EXTRA, auditItem.getUuid());
			detailIssueActivity.putExtra(DetailIssueActivity.SELECTED_TAB, 0);
			startActivity(detailIssueActivity);
		}
	}
	//endregion

	/**
	 * Get list audit items of container
	 */
	void refresh() {
		if (mAdapter != null) {
			dataCenter.getAuditItemsInBackground(getActivity(), containerId);
		}
	}

//	<<<<<<< HEAD
//	@UiThread
//	void updatedData(List<AuditItem> auditItems) {
//
//		if (mAdapter == null) {
//			mAdapter = new AuditItemAdapter(getActivity(),
//					R.layout.item_issue_pending, containerId, operatorCode);
//		}
//
//		mAdapter.clear();
//		if (auditItems != null) {
//			for (AuditItem auditItem : auditItems) {
//				mAdapter.add(auditItem);
//			}
//		}
//
//		mAdapter.notifyDataSetChanged();
//
//		// If container has audit image(s), hide button Container Ve sinh - quet
//		if (mAdapter.getCount() > 0) {
//			btnClean.setVisibility(View.GONE);
//		}
//	}
//
//	//region EVENT HANDLER
//	@UiThread
//	public void onEvent(ContainersGotEvent event) {
//
//		if (event.getTargets() != null && event.getTargets().size() > 0) {
//
//			mSession = event.getTargets().get(0);
//			if (mSession != null) {
//
//				// Get operator code
//				containerId = mSession.getContainerId();
//				operatorCode = mSession.getOperatorCode();
//
//				// Set currentStatus to TextView
//				currentStatus = mSession.getStatus();
//				tvCurrentStatus.setText((Status.values()[(int) currentStatus]).toString());
//
//				// Set ContainerId to TextView
//				tvContainerId.setText(containerId);
//
//				mAdapter = new AuditItemAdapter(getActivity(), R.layout.item_issue_pending, containerId, operatorCode);
//				lvAuditItems.setAdapter(mAdapter);
//
//				refresh();
//			} else {
//				// Set ContainerId to TextView
//				tvContainerId.setText(containerId);
//			}
//		}
//	}
//
//	@UiThread
//	=======


    /**
     * Pick gate in image or take audit picture
     */
    void showUseGateImageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_alert_title);
        builder.setMessage(R.string.dialog_message_use_gate_in_image);
        builder.setPositiveButton("Không", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Open camera to take audit picture
                Intent cameraActivityIntent = new Intent(getActivity(), CameraActivity_.class);
                cameraActivityIntent.putExtra(CameraFragment.CONTAINER_ID_EXTRA, containerId);
                cameraActivityIntent.putExtra(CameraFragment.IMAGE_TYPE_EXTRA, ImageType.AUDIT.value);
                cameraActivityIntent.putExtra(CameraFragment.OPERATOR_CODE_EXTRA, operatorCode);
                cameraActivityIntent.putExtra(CameraFragment.CURRENT_STEP_EXTRA, Step.AUDIT.value);
                cameraActivityIntent.putExtra(CameraFragment.IS_OPENED, false);
                startActivity(cameraActivityIntent);

                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton("Có", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Open ReuseActivity to chose Gate Image
                Intent intent = new Intent(getActivity(), ReuseActivity_.class);
                intent.putExtra(ReuseActivity_.CONTAINER_ID_EXTRA, containerId);
                startActivityForResult(intent, 1);
                dialogInterface.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {

                // Set background and text color for use gate image
                ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(getResources().getColor(android.R.color.white));
                ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setBackgroundResource(R.drawable.btn_green_selector);

                // Set background and text color for open camera
                ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(getResources().getColor(android.R.color.white));
                ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE)
                        .setBackgroundResource(R.drawable.btn_red_selector);
            }
        });
        dialog.show();
    }

	@Trace
	public void onEvent(AuditItemsGotEvent event) {

		// Filter list audit items that was not repair
		List<AuditItem> list = new ArrayList<>();
		for (AuditItem auditItem : event.getAuditItems()) {
			if (!auditItem.isRepaired()) {
				list.add(auditItem);
			}
		}

		// Sort list audit
		Comparator<AuditItem> comparator = new Comparator<AuditItem>() {
			@Override
			public int compare(AuditItem auditItem, AuditItem auditItem2) {
				if (!auditItem.isAudited()) {
					if (auditItem2.isAudited()) {
						return 1;
					} else {
						return -1;
					}
				} else {
					return -1;
				}
			}
		};

		Collections.sort(list, comparator);
		updatedData(list);
	}

	@UiThread
	void updatedData(List<AuditItem> auditItems) {

		for (AuditItem auditItem : auditItems) {
			Logger.Log("uuid: " + auditItem.getUuid());
		}

		if (mAdapter == null) {
			mAdapter = new AuditItemAdapter(getActivity(),
					R.layout.item_issue_pending, mSession, operatorCode);
		}

		mAdapter.clear();
		if (auditItems != null) {
			for (AuditItem auditItem : auditItems) {
				mAdapter.add(auditItem);
			}
		}

		mAdapter.notifyDataSetChanged();

		// If container has audit image(s), hide button Container Ve sinh - quet
		if (mAdapter.getCount() > 0) {
			btnClean.setVisibility(View.GONE);
		}
	}

	//region EVENT HANDLER
	@UiThread
	@Trace
	void onEvent(ImageCapturedEvent event) {
		Logger.Log("on ImageCapturedEvent");

		ImageType imageType = ImageType.values()[event.getImageType()];
		switch (imageType) {
			case AUDIT:
				// Re-query container session with given containerId
//				dataCenter.getSessionInBackground(getActivity(), containerId);
				refresh();
				break;

			case REPAIRED:
			default:

				if (!event.isOpened()) {
					Logger.Log("Open AfterRepair Fragment");
					String auditItemUUID = event.getAuditItemUUID();
					AuditItem auditItem = dataCenter.getAuditItem(getActivity(), this.containerId, auditItemUUID);
					Intent detailIssueActivity = new Intent(getActivity(), DetailIssueActivity_.class);
					detailIssueActivity.putExtra(DetailIssueActivity.CONTAINER_ID_EXTRA, this.containerId);
					detailIssueActivity.putExtra(DetailIssueActivity.AUDIT_ITEM_EXTRA, auditItem.getUuid());
					detailIssueActivity.putExtra(DetailIssueActivity.SELECTED_TAB, 1);
					startActivity(detailIssueActivity);
					break;
				}
		}
	}

	@UiThread
	void onEvent(IssueMergedEvent event) {
		Logger.Log("on IssueMergedEvent");

		// Delete merged audit item containerId
		String containerId = event.getContainerId();
		String auditItemRemoveUUID = event.getAuditItemRemoveUUID();
		dataCenter.removeAuditItem(getActivity().getApplicationContext(),
				containerId, auditItemRemoveUUID);
		refresh();
	}

	@UiThread
	void onEvent(AuditItemChangedEvent event) {
		dataCenter.getSessionInBackground(getActivity(), event.getContainerId());
	}


	void onEvent(UploadSucceededEvent event) {

		if (event.uploadType == UploadType.AUDIT_ITEM) {

		}
		mSession = event.getSession();
		refresh();

	}

	@UiThread
	void onEvent(UploadStartedEvent event) {
		mSession = event.getSession();
		refresh();
	}

	@Override
	public void onDestroy() {
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EventBus.getDefault().register(this);
	}
	//endregion


}
