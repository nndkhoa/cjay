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
import com.cloudjay.cjay.event.ImageCapturedEvent;
import com.cloudjay.cjay.event.IssueDeletedEvent;
import com.cloudjay.cjay.event.IssueMergedEvent;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.jobqueue.UploadSessionHandCleaningJob;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.enums.ImageType;
import com.cloudjay.cjay.util.enums.Status;
import com.cloudjay.cjay.util.enums.Step;
import com.path.android.jobqueue.JobManager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ItemClick;
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

	public final static String CONTAINER_ID_EXTRA = "com.cloudjay.wizard.containerID";

	@FragmentArg(CONTAINER_ID_EXTRA)
	public String containerID;

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
	AuditItemAdapter auditItemAdapter;

	Session mSession;

	public IssuePendingFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EventBus.getDefault().register(this);
	}

	@AfterViews
	void setUp() {

		// Get session by containerId
		mSession = dataCenter.getSession(getActivity().getApplicationContext(), containerID);

		if (mSession != null) {
			// Get operator code
			containerID = mSession.getContainerId();
			operatorCode = mSession.getOperatorCode();

			// Set currentStatus to TextView
			currentStatus = mSession.getStatus();
			tvCurrentStatus.setText((Status.values()[(int) currentStatus]).toString());

			// Set ContainerId to TextView
			tvContainerId.setText(containerID);

			auditItemAdapter = new AuditItemAdapter(getActivity(),
					R.layout.item_issue_pending, containerID, operatorCode);
			lvAuditItems.setAdapter(auditItemAdapter);

			refresh();
		} else {
			// Set ContainerId to TextView
			tvContainerId.setText(containerID);
		}
	}

	@UiThread
	void onEvent(ImageCapturedEvent event) {

		ImageType imageType = ImageType.values()[event.getImageType()];
		switch (imageType) {
			case AUDIT:
				// Re-query container session with given containerId
				String containerId = event.getContainerId();
				mSession = dataCenter.getSession(getActivity().getApplicationContext(), containerId);
				refresh();
				break;

			case REPAIRED:
			default:
				Logger.Log("Open AfterRepair Fragment");
				AuditItem auditItem = event.getAuditItem();
				Intent detailIssueActivity = new Intent(getActivity(), DetailIssueActivity_.class);
				detailIssueActivity.putExtra(DetailIssueActivity.CONTAINER_ID_EXTRA, containerID);
				detailIssueActivity.putExtra(DetailIssueActivity.AUDIT_ITEM_EXTRA, auditItem);
				startActivity(detailIssueActivity);
				break;
		}
	}

	@Click(R.id.btn_clean)
	@Background
	void buttonCleanClicked() {
		// Add container session to upload queue
		JobManager jobManager = App.getJobManager();
		jobManager.addJob(new UploadSessionHandCleaningJob(mSession));

		getActivity().finish();
	}

	@Click(R.id.btn_camera)
	void buttonCameraClicked() {
		// Open camera activity
		Intent cameraActivityIntent = new Intent(getActivity(), CameraActivity_.class);
		cameraActivityIntent.putExtra(CameraFragment.CONTAINER_ID_EXTRA, containerID);
		cameraActivityIntent.putExtra(CameraFragment.IMAGE_TYPE_EXTRA, ImageType.AUDIT.value);
		cameraActivityIntent.putExtra(CameraFragment.OPERATOR_CODE_EXTRA, operatorCode);
		cameraActivityIntent.putExtra(CameraFragment.CURRENT_STEP_EXTRA, Step.AUDIT.value);
		startActivity(cameraActivityIntent);
	}

	@ItemClick(R.id.lv_audit_items)
	void switchToDetailIssueActivity(int position) {
		AuditItem auditItem = auditItemAdapter.getItem(position);
		Intent detailIssueActivity = new Intent(getActivity(), DetailIssueActivity_.class);
		detailIssueActivity.putExtra(DetailIssueActivity.CONTAINER_ID_EXTRA, containerID);
		detailIssueActivity.putExtra(DetailIssueActivity.AUDIT_ITEM_EXTRA, auditItem);
		startActivity(detailIssueActivity);
	}

	@Background
	void refresh() {
		if (mSession != null) {
			List<AuditItem> list = new ArrayList<AuditItem>();
			Logger.Log("AuditItems: " + mSession.getAuditItems().size());
			for (AuditItem auditItem : mSession.getAuditItems()) {
				Logger.Log("audited: " + auditItem.getAudited());
				list.add(auditItem);
			}
			Logger.Log("Size: " + list.size());

			//Sort list audit
			Comparator<AuditItem> comparator = new Comparator<AuditItem>() {
				@Override
				public int compare(AuditItem auditItem, AuditItem auditItem2) {
					if (!auditItem.getAudited()) {
						if (auditItem2.getAudited()) {
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
	}

	@UiThread
	void updatedData(List<AuditItem> auditItems) {
		auditItemAdapter.clear();

		if (auditItems != null) {
			for (AuditItem auditItem : auditItems) {
				auditItemAdapter.add(auditItem);
			}
		}

		auditItemAdapter.notifyDataSetChanged();

		// If container has audit image(s), hide button Container Ve sinh - quet
		if (auditItemAdapter.getCount() > 0) {
			btnClean.setVisibility(View.GONE);
		}
	}

	@UiThread
	void onEvent(IssueMergedEvent event) {
		Logger.Log("on IssueMergedEvent");

		// Delete merged audit item containerId
		String containerId = event.getContainerId();
		String auditItemRemoveUUID = event.getAuditItemRemoveUUID();
		dataCenter.deleteAuditItemAfterMerge(getActivity().getApplicationContext(),
				containerId, auditItemRemoveUUID);
		refresh();
	}

	@UiThread
	void onEvent(IssueDeletedEvent event) {
		Logger.Log("on IssueDeletedEvent");

		// Re-query container session with given containerId
		String containerId = event.getContainerId();
		mSession = dataCenter.getSession(getActivity().getApplicationContext(), containerId);
		refresh();
	}

	@Override
	public void onDestroy() {
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}
}
