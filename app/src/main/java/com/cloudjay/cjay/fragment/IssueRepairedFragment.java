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
import com.cloudjay.cjay.event.image.ImageCapturedEvent;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.enums.Status;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
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

    public final static String CONTAINER_ID_EXTRA = "com.cloudjay.wizard.containerID";

    @FragmentArg(CONTAINER_ID_EXTRA)
    public String containerID;

    @ViewById(R.id.tv_container_code)
    TextView tvContainerId;

    @ViewById(R.id.tv_current_status)
    TextView tvCurrentStatus;

	@ViewById(R.id.lv_repaired_items)
	ListView lvRepairedItem;

    @Bean
    DataCenter dataCenter;

	Session mSession;
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

    @AfterViews
    void setUp() {

        // Get session by containerId
		mSession = dataCenter.getSession(getActivity().getApplicationContext(), containerID);

		if (mSession != null) {

			// Set text ContainerId TextView
			tvContainerId.setText(mSession.getContainerId());

			// Set currentStatus to TextView
			currentStatus = mSession.getStatus();
			tvCurrentStatus.setText((Status.values()[(int) currentStatus]).toString());

			mAdapter = new RepairedItemAdapter(getActivity().getApplicationContext(),
					R.layout.item_issue_repaired);
			lvRepairedItem.setAdapter(mAdapter);

			refresh();

		} else {
			// Set text ContainerId TextView
			tvContainerId.setText(containerID);
		}
    }

	@ItemClick(R.id.lv_repaired_items)
	void switchToDetailIssueActivity(int position) {
		AuditItem auditItem = mAdapter.getItem(position);
		if (auditItem.isAudited()) {
			Intent detailIssueActivity = new Intent(getActivity(), DetailIssueActivity_.class);
			detailIssueActivity.putExtra(DetailIssueActivity.CONTAINER_ID_EXTRA, containerID);
			detailIssueActivity.putExtra(DetailIssueActivity.AUDIT_ITEM_EXTRA, auditItem.getUuid());
			detailIssueActivity.putExtra(DetailIssueActivity.SELECTED_TAB, 0);
			startActivity(detailIssueActivity);
		}
	}

	@Background
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
	void onEvent(ImageCapturedEvent event) {
		Logger.Log("on ImageCapturedEvent");

		mSession = dataCenter.getSession(getActivity().getApplicationContext(), containerID);
		refresh();
	}

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

}
