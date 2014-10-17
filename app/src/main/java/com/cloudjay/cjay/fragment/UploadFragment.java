package com.cloudjay.cjay.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.ListView;
import android.widget.TextView;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.adapter.UploadSessionAdapter;
import com.cloudjay.cjay.event.UpLoadingEvent;
import com.cloudjay.cjay.event.StartUpLoadEvent;
import com.cloudjay.cjay.event.StopUpLoadEvent;
import com.cloudjay.cjay.event.UploadedEvent;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.CJayConstant;
import com.snappydb.SnappydbException;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;


@EFragment(R.layout.fragment_upload)
public class UploadFragment extends Fragment {

    private static final int LOADER_ID = 1;

    @ViewById(R.id.lv_uploading_container)
    ListView lvUploading;

    @ViewById(R.id.tv_emptylist_uploading)
    TextView tvEmpty;

    private UploadSessionAdapter mAdapter;

    List<Session> uploadingSessionList;


    public UploadFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    /**
     * Initial loader and set adapter for list view
     */
    @AfterViews
    void initLoader() {
        uploadingSessionList = new ArrayList<Session>();

        try {
            String[] listUploadingId = App.getSnappyDB(getActivity()).findKeys(CJayConstant.UPLOADING_DB);
            for (String uploadingId : listUploadingId) {
                Session session = App.getSnappyDB(getActivity()).getObject(uploadingId, Session.class);
                uploadingSessionList.add(session);
            }
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
        mAdapter = new UploadSessionAdapter(getActivity(), R.layout.item_upload);
        mAdapter.setData(uploadingSessionList);
        lvUploading.setAdapter(mAdapter);
        lvUploading.setEmptyView(tvEmpty);
    }

    public void onEvent(StartUpLoadEvent event) {
        Session session = null;
        try {
            Session oldSession = null;
            session = App.getSnappyDB(getActivity()).getObject(CJayConstant.UPLOADING_DB + event.getContainerId(), Session.class);
            for (Session session1 : uploadingSessionList) {
                if (session1.getContainerId().equals(event.getContainerId())) {
                    oldSession = session1;
                }
            }
            uploadingSessionList.remove(oldSession);
            uploadingSessionList.add(session);
            mAdapter.setData(uploadingSessionList);
            mAdapter.notifyDataSetChanged();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }

    }

    @UiThread
    public void onEvent(UploadedEvent event) {
        Session session = null;
        try {
            Session oldSession = null;
            session = App.getSnappyDB(getActivity()).getObject(CJayConstant.UPLOADING_DB + event.getContainerId(), Session.class);
            for (Session session1 : uploadingSessionList) {
                if (session1.getContainerId().equals(event.getContainerId())) {
                    oldSession = session1;
                }
            }
            uploadingSessionList.remove(oldSession);
            uploadingSessionList.add(session);
            mAdapter.setData(uploadingSessionList);
            mAdapter.notifyDataSetChanged();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    public void onEvent(StopUpLoadEvent event) {
    }

    public void onEvent(UpLoadingEvent event) {

    }
}
