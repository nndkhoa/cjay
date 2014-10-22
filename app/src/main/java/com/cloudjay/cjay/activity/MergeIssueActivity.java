package com.cloudjay.cjay.activity;

import android.app.Activity;
import android.widget.ListView;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.adapter.AuditMergeIssueAdapter;
import com.cloudjay.cjay.model.AuditImage;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.Utils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

/**
 * Created by thai on 21/10/2014.
 */
@EActivity(R.layout.activity_mergeissue)
public class MergeIssueActivity extends BaseActivity {

    @Bean
    DataCenter dataCenter;

    public final static String CONTAINER_ID_EXTRA = "com.cloudjay.wizard.containerID";
    public final static String AUDIT_IMAGE = "com.cloudjay.wizard.auditImage";

    @Extra(CONTAINER_ID_EXTRA)
    public String containerID;

    @Extra(AUDIT_IMAGE)
    AuditImage auditImage;

    @ViewById(R.id.lv_merge_issue)
    ListView lvIssues;

    List<AuditItem> auditItems;

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
        //TODO get un-uploaded audit item
        auditItems = currentSession.getAuditItems();
        mAdapter.setData(auditItems);
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
