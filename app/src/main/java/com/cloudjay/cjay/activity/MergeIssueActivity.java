package com.cloudjay.cjay.activity;

import android.widget.ListView;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.adapter.AuditMergeIssueAdapter;
import com.cloudjay.cjay.model.AuditImage;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.model.IsoCode;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.Utils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Màn hình chữa các lỗi đã giám định và chưa được upload
 */
@EActivity(R.layout.activity_merge_issue)
public class MergeIssueActivity extends BaseActivity {

    @Bean
    DataCenter dataCenter;

    public final static String CONTAINER_ID_EXTRA = "com.cloudjay.wizard.containerID";
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

	Session currentSession;

    AuditMergeIssueAdapter mAdapter;

    @AfterViews
    void setup() {

        //get container operater code form containerId
        currentSession = dataCenter.getSession(this.getApplicationContext(), containerID);
        if (null == currentSession) {
            Utils.showCrouton(this, "Không tìm thấy container trong dữ liệu");
        }
        mAdapter = new AuditMergeIssueAdapter(this, R.layout.item_merge_issue);
        lvIssues.setAdapter(mAdapter);

        refresh();

    }

    @ItemClick(R.id.lv_merge_issue)
    void lvIssuesItemClicked(int position) {

        AuditItem auditItem = mAdapter.getItem(position);
        String uuid = auditItem.getAuditItemUUID();
		Logger.Log("auditImageUUID: " + auditImageUUID);
		Logger.Log("auditItemRemoveUUID: " + auditItemRemoveUUID);
		Logger.Log("uuid: " + uuid);
        dataCenter.addAuditImageToAuditedIssue(getApplicationContext(), containerID,
                uuid, auditItemRemoveUUID, auditImageUUID);
        refresh();

        this.finish();
    }

    @Background
    void refresh() {

		//get container operater code form containerId
		currentSession = dataCenter.getSession(this.getApplicationContext(), containerID);

        List<AuditItem> list = new ArrayList<AuditItem>();
        for (AuditItem auditItem : currentSession.getAuditItems()) {
            if (auditItem.getAudited() == true && auditItem.getId() == 0) {
                list.add(auditItem);
            }
        }
        Logger.Log("Size: " + list.size());
        updatedData(list);
    }

    @UiThread
    public void updatedData(List<AuditItem> auditList) {
        mAdapter.clear();
        if (auditList != null) {
            for (AuditItem object : auditList) {
                //mAdapter.insert(object, mAdapter.getCount());
                mAdapter.add(object);
            }
        }
        mAdapter.notifyDataSetChanged();
    }
}
