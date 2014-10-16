package com.cloudjay.cjay.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.ListView;
import android.widget.TextView;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.adapter.SessionAdapter;
import com.cloudjay.cjay.event.ImageCapturedEvent;
import com.cloudjay.cjay.event.WorkingSessionCreatedEvent;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.snappydb.SnappydbException;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Danh sách các container đang thao tác
 */
@EFragment(R.layout.fragment_working)
public class WorkingFragment extends Fragment {


    @ViewById(R.id.lv_working_container)
    ListView lvWorking;

    @ViewById(R.id.tv_emptylist_working)
    TextView tvEmpty;

    private SessionAdapter mAdapter;

    List<Session> workingSessionList;

    public WorkingFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        try {
            App.getSnappyDB(getActivity()).close();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    /**
     * Initial loader and set adapter for list view
     */
    @AfterViews
    void init() {
        workingSessionList = new ArrayList<Session>();

        try {
            String[] listWorkingId = App.getSnappyDB(getActivity()).findKeys(CJayConstant.WORKING_DB);
            for (String workingId : listWorkingId) {
                Session session = App.getSnappyDB(getActivity()).getObject(workingId, Session.class);
                workingSessionList.add(session);
            }
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
        mAdapter = new SessionAdapter(getActivity(), R.layout.item_container_working);
        mAdapter.setData(workingSessionList);
        lvWorking.setAdapter(mAdapter);
        lvWorking.setEmptyView(tvEmpty);
    }

    @UiThread
    public void onEvent(WorkingSessionCreatedEvent event) {
        Session session = null;
        try {
            session = App.getSnappyDB(getActivity()).getObject(CJayConstant.WORKING_DB+event.getWorkingSession().getContainerId(), Session.class);
            workingSessionList.add(session);
            mAdapter.setData(workingSessionList);
            mAdapter.notifyDataSetChanged();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }

    }
    @UiThread
    public void onEvent(ImageCapturedEvent event) {
        Session session = null;
        try {
            session = App.getSnappyDB(getActivity()).getObject(CJayConstant.WORKING_DB+event.getContainerId(), Session.class);
            workingSessionList.remove(session);
            workingSessionList.add(session);
            mAdapter.setData(workingSessionList);
            mAdapter.notifyDataSetChanged();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }

    }

}
