package com.cloudjay.cjay.activity;

import android.widget.ListView;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.adapter.AuditMergeIssueAdapter;
import com.cloudjay.cjay.model.AuditImage;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.model.Session;
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

import java.util.List;

/**
 * Created by thai on 21/10/2014.
 */
@EActivity(R.layout.activity_merge_issue)
public class MergeIssueActivity extends BaseActivity {

    @Bean
    DataCenter dataCenter;

    public final static String CONTAINER_ID_EXTRA = "com.cloudjay.wizard.containerID";
    public final static String AUDIT_IMAGE_EXTRA = "com.cloudjay.wizard.auditImage";

    @Extra(CONTAINER_ID_EXTRA)
    public String containerID;

    @Extra(AUDIT_IMAGE_EXTRA)
    AuditImage auditImage;

    @ViewById(R.id.lv_merge_issue)
    ListView lvIssues;

    List<AuditItem> auditItems;

    Session currentSession;

    AuditMergeIssueAdapter mAdapter;

    boolean isInserted = false;

    @AfterViews
    void setup() {
        //get container operater code form containerId
        currentSession = dataCenter.getSession(this.getApplicationContext(), containerID);
        if (null == currentSession) {
            Utils.showCrouton(this, "Không tìm thấy container trong dữ liệu");
        }
        mAdapter = new AuditMergeIssueAdapter(this, R.layout.item_merge_issue);
        lvIssues.setAdapter(mAdapter);
        //TODO: get un-uploaded audit item

        for (AuditItem auditItem : currentSession.getAuditItems()) {
            if (auditItem.getComponentCode() != null) {
                isInserted = true;
                return;
            }
        }

        Logger.Log("isInserted: " + isInserted);
        if (isInserted == false) {
            // Create static issue
            AuditItem auditItem = new AuditItem();
            auditItem.setComponentCode("LBG");
            auditItem.setLocationCode("LTON");
            auditItem.setDamageCode("BR");
            auditItem.setRepairCode("RP");
            auditItem.setHeight(40);
            auditItem.setLength(20);
            auditItem.setQuantity(2);

            // Insert static issue into database
            for (int i = 0; i < 5; i++) {
                dataCenter.addIssue(getApplicationContext(), auditItem, containerID);
            }

            refresh();
        }

    }

    @ItemClick(R.id.lv_merge_issue)
    void lvIssuesItemClicked(int position) {
        auditItems.get(position).getAuditImages().add(auditImage);
        currentSession.setAuditItems(auditItems);
        dataCenter.addSession(currentSession);
        refresh();
    }

    @Background
    void refresh() {
        List<AuditItem> list = dataCenter.getListAuditItems(this,containerID);
        Logger.Log("Size: " + list.size());
        updatedData(list);
    }

    @UiThread
    public void updatedData(List<AuditItem> auditList) {
        mAdapter.clear();
        if (auditList != null) {
            for (AuditItem object : auditList) {
                mAdapter.insert(object, mAdapter.getCount());
            }
        }
        mAdapter.notifyDataSetChanged();
    }
}
