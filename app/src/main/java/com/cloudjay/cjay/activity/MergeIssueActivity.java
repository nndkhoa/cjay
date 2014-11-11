package com.cloudjay.cjay.activity;

import android.widget.ListView;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.adapter.AuditMergeIssueAdapter;
import com.cloudjay.cjay.event.issue.AuditItemsGotEvent;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.model.Session;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình chữa các lỗi đã giám định và chưa được upload
 */
@EActivity(R.layout.activity_merge_issue)
public class MergeIssueActivity extends BaseActivity {

	@Bean
	DataCenter dataCenter;

	public final static String CONTAINER_ID_EXTRA = "com.cloudjay.wizard.containerId";
	public final static String AUDIT_IMAGE_EXTRA = "com.cloudjay.wizard.auditImage";
	public final static String AUDIT_ITEM_REMOVE_UUID = "com.cloudjay.wizard.auditItemRemoveUUID";

	@Extra(CONTAINER_ID_EXTRA)
	public String containerID;

	@Extra(AUDIT_IMAGE_EXTRA)
	String auditImageUUID;

	@Extra(AUDIT_ITEM_REMOVE_UUID)
	String auditItemRemoveUUID;

	@ViewById(R.id.lv_merge_issue)
	ListView lvIssues;

	AuditMergeIssueAdapter mAdapter;

	@AfterViews
	void setup() {
		mAdapter = new AuditMergeIssueAdapter(this, R.layout.item_merge_issue);
		lvIssues.setAdapter(mAdapter);
		refresh();
	}

	@UiThread
	public void onEvent(AuditItemsGotEvent event) {

		// Filter list audit items that was not repair
		List<AuditItem> list = new ArrayList<>();
		for (AuditItem auditItem : event.getAuditItems()) {
			if (auditItem.isAudited() == true && auditItem.getId() == 0) {
				list.add(auditItem);
			}
		}
		updatedData(list);
	}

	@ItemClick(R.id.lv_merge_issue)
	void lvIssuesItemClicked(int position) {

		AuditItem auditItem = mAdapter.getItem(position);
		String uuid = auditItem.getUuid();
		dataCenter.addAuditImageToAuditedItem(getApplicationContext(), containerID,
				uuid, auditItemRemoveUUID, auditImageUUID);
		refresh();

		this.finish();
	}

	void refresh() {
		if (mAdapter != null) {
			dataCenter.getAuditItemsInBackground(this, containerID);
		}
	}

	@UiThread
	public void updatedData(List<AuditItem> auditItems) {
		mAdapter.clear();
		if (auditItems != null) {
			for (AuditItem object : auditItems) {
				mAdapter.add(object);
			}
		}
		mAdapter.notifyDataSetChanged();
	}
}
