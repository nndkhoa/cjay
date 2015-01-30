package com.cloudjay.cjay.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.ListView;
import android.widget.TextView;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.activity.DetailIssueActivity;
import com.cloudjay.cjay.activity.DetailIssueActivity_;
import com.cloudjay.cjay.adapter.RepairedItemAdapter;
import com.cloudjay.cjay.event.issue.RepairedItemsGotEvent;
import com.cloudjay.cjay.event.session.ContainerGotParentFragmentEvent;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.enums.Status;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * A simple {@link Fragment} subclass.
 */
@EFragment(R.layout.fragment_issue_repaired)
public class IssueRepairedFragment extends Fragment {

    public Session mSession;
    public String containerId;

    @ViewById(R.id.tv_container_code)
    TextView tvContainerId;

    @ViewById(R.id.tv_current_status)
    TextView tvCurrentStatus;

    @ViewById(R.id.lv_repaired_items)
    ListView lvRepairedItem;

    @Bean
    DataCenter dataCenter;

    //	Session mSession;
    long currentStatus;
    RepairedItemAdapter mAdapter;
    List<AuditItem> repairedList;


    public IssueRepairedFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @ItemClick(R.id.lv_repaired_items)
    void repairItemClicked(int position) {
        AuditItem auditItem = mAdapter.getItem(position);
        if (auditItem.isAudited()) {
            Intent detailIssueActivity = new Intent(getActivity(), DetailIssueActivity_.class);
            detailIssueActivity.putExtra(DetailIssueActivity.CONTAINER_ID_EXTRA, mSession.getContainerId());
            detailIssueActivity.putExtra(DetailIssueActivity.AUDIT_ITEM_EXTRA, auditItem.getUuid());
            detailIssueActivity.putExtra(DetailIssueActivity.SELECTED_TAB, 0);
            startActivity(detailIssueActivity);
        }
    }

    void refresh() {
        if (mSession != null) {
            repairedList = mSession.getListRepairedItem();
            updatedData(repairedList);
        }
    }

    @UiThread
    void updatedData(List<AuditItem> repairedItemLists) {

        if (mAdapter == null) {
            mAdapter = new RepairedItemAdapter(getActivity().getApplicationContext(),
                    R.layout.item_issue_repaired);
        }

        mAdapter.clear();
        if (repairedItemLists != null) {
            for (AuditItem item : repairedItemLists) {
                mAdapter.add(item);
            }
        }

        mAdapter.notifyDataSetChanged();
    }

    @UiThread
    public void onEvent(ContainerGotParentFragmentEvent event) {
        mSession = event.getSession();
        if (null == mSession) {
            Logger.Log("mSession is null");
        } else {
            updateViews();
        }
    }

    @UiThread
    public void onEvent(RepairedItemsGotEvent event) {
        repairedList = event.getAuditItems();
        updatedData(repairedList);
    }

    private void updateViews() {
        if (mSession != null) {
            // Set text ContainerId TextView
            tvContainerId.setText(mSession.getContainerId());

            // Set currentStatus to TextView
            currentStatus = mSession.getStatus();
            tvCurrentStatus.setText((Status.values()[(int) currentStatus]).toString());

            if (null == mAdapter) {
                mAdapter = new RepairedItemAdapter(getActivity().getApplicationContext(), R.layout.item_issue_repaired);
                lvRepairedItem.setAdapter(mAdapter);
            }
        }
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}