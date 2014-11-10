package com.cloudjay.cjay.fragment;


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
import com.cloudjay.cjay.adapter.AuditItemAdapter;
import com.cloudjay.cjay.event.image.ImageCapturedEvent;
import com.cloudjay.cjay.event.issue.AuditItemsGotEvent;
import com.cloudjay.cjay.event.issue.IssueDeletedEvent;
import com.cloudjay.cjay.event.issue.IssueMergedEvent;
import com.cloudjay.cjay.event.issue.IssueUpdatedEvent;
import com.cloudjay.cjay.event.upload.UploadStartedEvent;
import com.cloudjay.cjay.event.upload.UploadSucceededEvent;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.job.UploadSessionJob;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.enums.ImageType;
import com.cloudjay.cjay.util.enums.Status;
import com.cloudjay.cjay.util.enums.Step;
import com.cloudjay.cjay.util.enums.UploadType;
import com.path.android.jobqueue.JobManager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
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

	public final static String CONTAINER_ID_EXTRA = "com.cloudjay.wizard.containerId";

	@FragmentArg(CONTAINER_ID_EXTRA)
	public String containerId;

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

	@Bean
	DataCenter dataCenter;

	String operatorCode;
	long currentStatus;
	AuditItemAdapter mAdapter;

	Session mSession;

	public IssuePendingFragment() {
		// Required empty public constructor
	}

	@AfterViews
	void setUp() {

		Logger.Log("on setUp");

		// Get session by containerId
		mSession = dataCenter.getSession(getActivity().getApplicationContext(), containerId);

		if (mSession != null) {

			// Get operator code
			containerId = mSession.getContainerId();
			operatorCode = mSession.getOperatorCode();

			// Set currentStatus to TextView
			currentStatus = mSession.getStatus();
			tvCurrentStatus.setText((Status.values()[(int) currentStatus]).toString());

			// Set ContainerId to TextView
			tvContainerId.setText(containerId);

			mAdapter = new AuditItemAdapter(getActivity(), R.layout.item_issue_pending, containerId, operatorCode);
			lvAuditItems.setAdapter(mAdapter);

			refresh();
		} else {
			// Set ContainerId to TextView
			tvContainerId.setText(containerId);
		}
	}

	@Click(R.id.btn_clean)
	void buttonCleanClicked() {

		// Add container session to upload queue
		JobManager jobManager = App.getJobManager();
		jobManager.addJobInBackground(new UploadSessionJob(mSession.getContainerId(), Step.HAND_CLEAN.value, true));

		getActivity().finish();
	}

	@Click(R.id.btn_camera)
	void buttonCameraClicked() {

		// Open camera activity
		Intent cameraActivityIntent = new Intent(getActivity(), CameraActivity_.class);
		cameraActivityIntent.putExtra(CameraFragment.CONTAINER_ID_EXTRA, containerId);
		cameraActivityIntent.putExtra(CameraFragment.IMAGE_TYPE_EXTRA, ImageType.AUDIT.value);
		cameraActivityIntent.putExtra(CameraFragment.OPERATOR_CODE_EXTRA, operatorCode);
		cameraActivityIntent.putExtra(CameraFragment.CURRENT_STEP_EXTRA, Step.AUDIT.value);
		cameraActivityIntent.putExtra(CameraFragment.IS_OPENED, false);
		startActivity(cameraActivityIntent);
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

	/**
	 * Get list audit items of container
	 */
	void refresh() {
		if (mAdapter != null) {
			dataCenter.getAuditItemsInBackground(getActivity(), containerId);
		}
	}

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

	@Trace
	@UiThread
	void updatedData(List<AuditItem> auditItems) {

		if (mAdapter == null) {
			mAdapter = new AuditItemAdapter(getActivity(),
					R.layout.item_issue_pending, containerId, operatorCode);
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

	@Trace
	@UiThread
	void onEvent(IssueDeletedEvent event) {
		Logger.Log("on IssueDeletedEvent");

		// Re-query container session with given containerId
		String containerId = event.getContainerId();
		mSession = dataCenter.getSession(getActivity().getApplicationContext(), containerId);
		refresh();
	}

	@UiThread
	void onEvent(IssueUpdatedEvent event) {
		Logger.Log("on IssueUpdatedEvent");

		// Re-query container session with given containerId
		String containerId = event.getContainerId();
		mSession = dataCenter.getSession(getActivity().getApplicationContext(), containerId);
		refresh();
	}

	void onEvent(UploadSucceededEvent event) {

		if (event.uploadType == UploadType.AUDIT_ITEM) {

		}
		// Re-query container session with given containerId
		String containerId = event.getContainerId();
		mSession = dataCenter.getSession(getActivity().getApplicationContext(), containerId);
		refresh();
	}

	void onEvent(UploadStartedEvent event) {
		Logger.Log("upload complete");
		// Re-query container session with given containerId
		String containerId = event.getContainerId();
		mSession = dataCenter.getSession(getActivity().getApplicationContext(), containerId);
		refresh();
	}
	//endregion

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
}
