package com.cloudjay.cjay.activity;

import android.app.Activity;
import android.widget.ListView;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.adapter.AuditMergeIssueAdapter;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.Utils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.util.List;

/**
 * Created by thai on 21/10/2014.
 */
@EActivity(R.layout.activity_mergeissue)
public class MergeIssueActivity extends Activity {

    @Bean
    DataCenter dataCenter;

    public final static String CONTAINER_ID_EXTRA = "com.cloudjay.wizard.containerID";

    @FragmentArg(CONTAINER_ID_EXTRA)
    public String containerID;

    @ViewById(R.id.lv_merge_issue)
    ListView lvIssues;

    AuditMergeIssueAdapter mergeIssueAdapter;

    @AfterViews
    void setup() {
        //get container operater code form containerId
        Session tmp = dataCenter.getSession(this.getApplicationContext(), containerID);
        if (null == tmp) {
            Utils.showCrouton(this, "Không tìm thấy container trong dữ liệu");
        }
        mergeIssueAdapter = new AuditMergeIssueAdapter(this, R.layout.item_merge_issue);
        //TODO get un-uploaded audit item
        List<AuditItem> auditItems = tmp.getAuditItems();
        mergeIssueAdapter.setData(auditItems);
    }
}
