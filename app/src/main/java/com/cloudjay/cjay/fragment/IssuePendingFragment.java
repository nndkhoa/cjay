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
import com.cloudjay.cjay.event.issue.AuditItemChangedEvent;
import com.cloudjay.cjay.event.issue.AuditItemsGotEvent;
import com.cloudjay.cjay.event.issue.IssueMergedEvent;
import com.cloudjay.cjay.event.session.ContainerGotEvent;
import com.cloudjay.cjay.event.upload.UploadStartedEvent;
import com.cloudjay.cjay.event.upload.UploadSucceededEvent;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.command.issue.GetListAuditItemsCommand;
import com.cloudjay.cjay.task.command.issue.RemoveAuditItemCommand;
import com.cloudjay.cjay.task.command.session.get.GetSessionCommand;
import com.cloudjay.cjay.task.command.session.remove.RemoveWorkingSessionCommand;
import com.cloudjay.cjay.task.command.session.update.ForceExportCommand;
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
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
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
@OptionsMenu(R.menu.issue)
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
		dataCenter.add(new GetSessionCommand(getActivity(), containerId));
	}

	//region VIEW INTERACTION

	@Click(R.id.btn_clean)
	void buttonCleanClicked() {

		//Remove from working
		dataCenter.add(new RemoveWorkingSessionCommand(getActivity(), containerId));

		//Change step to Clean
		mSession.setUploadStatus(UploadStatus.UPLOADING);
		mSession.setLocalStep(Step.HAND_CLEAN.value);

		// Add container session to upload queue
		JobManager jobManager = App.getJobManager();
		jobManager.addJobInBackground(new UploadSessionJob(mSession));

//		mSession.prepareForUploading();
//		dataCenter.add(new SaveSessionCommand(getActivity(), mSession));

		getActivity().finish();
	}

	@Click(R.id.btn_camera)
	void buttonCameraClicked() {
		if (mAdapter == null || mAdapter.getCount() == 0) {
			showUseGateImageDialog();
		} else {
			openCamera();
		}
	}

	@ItemClick(R.id.lv_audit_items)
	void auditItemClicked(int position) {
		AuditItem auditItem = mAdapter.getItem(position);

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
			dataCenter.add(new GetListAuditItemsCommand(getActivity(), containerId));
		}
	}

	@UiThread
	public void onEvent(ContainerGotEvent event) {
		mSession = event.getSession();

		if (mSession != null) {

			// Get operator code
			containerId = mSession.getContainerId();
			operatorCode = mSession.getOperatorCode();

			// Set ContainerId to TextView
			tvContainerId.setText(containerId);

			// Set currentStatus to TextView
			currentStatus = mSession.getStatus();
			tvCurrentStatus.setText((Status.values()[(int) currentStatus]).toString());

			mAdapter = new AuditItemAdapter(getActivity(), R.layout.item_issue_pending,
					mSession, operatorCode);
			lvAuditItems.setAdapter(mAdapter);

			refresh();
		} else {
			// Set ContainerId to TextView
			tvContainerId.setText(containerId);
		}
	}

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

				openCamera();
				dialogInterface.dismiss();
			}
		});
		builder.setNegativeButton("Có", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				openReuseActivity();
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

	/**
	 * Open camera to take audit picture
	 */
	void openCamera() {
		Intent cameraActivityIntent = new Intent(getActivity(), CameraActivity_.class);
		cameraActivityIntent.putExtra(CameraActivity_.CONTAINER_ID_EXTRA, containerId);
		cameraActivityIntent.putExtra(CameraActivity_.IMAGE_TYPE_EXTRA, ImageType.AUDIT.value);
		cameraActivityIntent.putExtra(CameraActivity_.OPERATOR_CODE_EXTRA, operatorCode);
		cameraActivityIntent.putExtra(CameraActivity_.CURRENT_STEP_EXTRA, Step.AUDIT.value);
		cameraActivityIntent.putExtra(CameraActivity_.IS_OPENED, false);
		startActivity(cameraActivityIntent);
	}

	/**
	 * Open ReuseActivity to chose Gate Image
	 */
	void openReuseActivity() {
		Intent intent = new Intent(getActivity(), ReuseActivity_.class);
		intent.putExtra(ReuseActivity_.CONTAINER_ID_EXTRA, containerId);
		startActivityForResult(intent, 1);
	}

	@UiThread
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


	@OptionsItem(R.id.menu_export)
	void exportMenuItemClicked() {
		dataCenter.add(new ForceExportCommand(getActivity(), containerId));
		getActivity().finish();
	}

	@UiThread
	void updatedData(List<AuditItem> auditItems) {

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

	@Override
	public void onResume() {
        super.onResume();
		refresh();
	}

	@UiThread
	void onEvent(IssueMergedEvent event) {

		// Delete merged audit item containerId
		String containerId = event.getContainerId();
		String itemUuid = event.getItemUuid();
		dataCenter.add(new RemoveAuditItemCommand(getActivity(), containerId, itemUuid));
	}

	@UiThread
	void onEvent(AuditItemChangedEvent event) {
        Logger.Log("change event");
		refresh();
	}

	@UiThread
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
